package guess.dao;

import guess.domain.source.*;
import guess.domain.statistics.olap.Cube;
import guess.domain.statistics.olap.CubeType;
import guess.domain.statistics.olap.DimensionType;
import guess.domain.statistics.olap.MeasureType;
import guess.domain.statistics.olap.dimension.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * OLAP DAO implementation.
 */
@Repository
public class OlapDaoImpl implements OlapDao {
    static class Cubes {
        private final Cube eventTypesCube;
        private final Cube speakersCube;
        private final Cube companiesCube;

        public Cubes(Cube eventTypesCube, Cube speakersCube, Cube companiesCube) {
            this.eventTypesCube = eventTypesCube;
            this.speakersCube = speakersCube;
            this.companiesCube = companiesCube;
        }
    }

    private final Map<CubeType, Cube> cubeMap = new EnumMap<>(CubeType.class);

    private final TopicDao topicDao;
    private final EventTypeDao eventTypeDao;

    @Autowired
    public OlapDaoImpl(TopicDao topicDao, EventTypeDao eventTypeDao) {
        this.topicDao = topicDao;
        this.eventTypeDao = eventTypeDao;

        Cube eventTypesCube = new Cube(
                new LinkedHashSet<>(Arrays.asList(DimensionType.EVENT_TYPE, DimensionType.CITY, DimensionType.YEAR, DimensionType.TOPIC)),
                new LinkedHashSet<>(Arrays.asList(MeasureType.EVENTS_QUANTITY, MeasureType.DURATION, MeasureType.TALKS_QUANTITY,
                        MeasureType.SPEAKERS_QUANTITY, MeasureType.COMPANIES_QUANTITY, MeasureType.JAVA_CHAMPIONS_QUANTITY,
                        MeasureType.MVPS_QUANTITY)));
        Cube speakersCube = new Cube(
                new LinkedHashSet<>(Arrays.asList(DimensionType.EVENT_TYPE, DimensionType.SPEAKER, DimensionType.YEAR, DimensionType.TOPIC)),
                new LinkedHashSet<>(Arrays.asList(MeasureType.TALKS_QUANTITY, MeasureType.EVENTS_QUANTITY,
                        MeasureType.EVENT_TYPES_QUANTITY)));
        Cube companiesCube = new Cube(
                new LinkedHashSet<>(Arrays.asList(DimensionType.EVENT_TYPE, DimensionType.COMPANY, DimensionType.SPEAKER,
                        DimensionType.YEAR, DimensionType.TOPIC)),
                new LinkedHashSet<>(Arrays.asList(MeasureType.TALKS_QUANTITY, MeasureType.EVENTS_QUANTITY,
                        MeasureType.EVENT_TYPES_QUANTITY, MeasureType.SPEAKERS_QUANTITY, MeasureType.JAVA_CHAMPIONS_QUANTITY,
                        MeasureType.MVPS_QUANTITY)));

        cubeMap.put(CubeType.EVENT_TYPES, eventTypesCube);
        cubeMap.put(CubeType.SPEAKERS, speakersCube);
        cubeMap.put(CubeType.COMPANIES, companiesCube);

        fillDimensions(eventTypesCube, speakersCube, companiesCube);
        fillMeasures(eventTypesCube, speakersCube, companiesCube);
    }

    @Override
    public Cube getCube(CubeType cubeType) {
        return Objects.requireNonNull(cubeMap.get(cubeType), () -> String.format("Cube type %s not found", cubeType));
    }

    @Override
    public List<MeasureType> getMeasureTypes(CubeType cubeType) {
        return List.copyOf(getCube(cubeType).getMeasureTypes());
    }

    void fillDimensions(Cube eventTypesCube, Cube speakersCube, Cube companiesCube) {
        // Event type dimension values
        Set<EventType> eventTypes = new HashSet<>(eventTypeDao.getEventTypes());

        // City dimension values
        var id = new AtomicLong(-1);
        Set<City> cities = eventTypeDao.getEventTypes().stream()
                .flatMap(et -> et.getEvents().stream())
                .flatMap(e -> e.getDays().stream())
                .map(ed -> ed.getPlace().getCity())
                .distinct()
                .map(li -> new City(id.incrementAndGet(), li))
                .collect(Collectors.toSet());

        // Speaker dimension values
        Set<Speaker> speakers = eventTypeDao.getEventTypes().stream()
                .flatMap(et -> et.getEvents().stream())
                .flatMap(e -> e.getTalks().stream())
                .flatMap(t -> t.getSpeakers().stream())
                .collect(Collectors.toSet());
        Set<Speaker> companySpeakers = speakers.stream()
                .filter(s -> !s.getCompanies().isEmpty())
                .collect(Collectors.toSet());

        // Company dimension values
        Set<Company> companies = eventTypeDao.getEventTypes().stream()
                .flatMap(et -> et.getEvents().stream())
                .flatMap(e -> e.getTalks().stream())
                .flatMap(t -> t.getSpeakers().stream())
                .flatMap(s -> s.getCompanies().stream())
                .collect(Collectors.toSet());

        // Year dimension values
        IntSummaryStatistics summaryStatistics = eventTypeDao.getEventTypes().stream()
                .flatMap(et -> et.getEvents().stream())
                .map(e -> e.getFirstStartDate().getYear())
                .mapToInt(y -> y)
                .summaryStatistics();
        Set<Integer> years = IntStream.rangeClosed(summaryStatistics.getMin(), summaryStatistics.getMax())
                .boxed()
                .collect(Collectors.toSet());

        // Topic dimension values
        Set<Topic> topics = new HashSet<>(topicDao.getTopics());

        eventTypesCube.addDimensions(DimensionType.EVENT_TYPE, eventTypes);
        eventTypesCube.addDimensions(DimensionType.CITY, cities);
        eventTypesCube.addDimensions(DimensionType.YEAR, years);
        eventTypesCube.addDimensions(DimensionType.TOPIC, topics);

        speakersCube.addDimensions(DimensionType.EVENT_TYPE, eventTypes);
        speakersCube.addDimensions(DimensionType.SPEAKER, speakers);
        speakersCube.addDimensions(DimensionType.YEAR, years);
        speakersCube.addDimensions(DimensionType.TOPIC, topics);

        companiesCube.addDimensions(DimensionType.EVENT_TYPE, eventTypes);
        companiesCube.addDimensions(DimensionType.COMPANY, companies);
        companiesCube.addDimensions(DimensionType.SPEAKER, companySpeakers);
        companiesCube.addDimensions(DimensionType.YEAR, years);
        companiesCube.addDimensions(DimensionType.TOPIC, topics);
    }

    void fillMeasures(Cube eventTypesCube, Cube speakersCube, Cube companiesCube) {
        List<EventType> eventTypes = eventTypeDao.getEventTypes();
        Map<List<LocaleItem>, City> cityMap = eventTypesCube.getDimensionValues(DimensionType.CITY).stream()
                .map(City.class::cast)
                .collect(Collectors.toMap(Nameable::getName, v -> v));
        Topic defaultTopic = topicDao.getTopics().stream()
                .filter(Topic::isDefaultTopic)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Default topic not found"));
        Cubes cubes = new Cubes(eventTypesCube, speakersCube, companiesCube);

        for (EventType eventType : eventTypes) {
            // Event type dimension
            EventTypeDimension eventTypeDimension = new EventTypeDimension(eventType);

            // Topic dimension
            Topic eventTypeTopic = (eventType.getTopic() != null) ? eventType.getTopic() : defaultTopic;
            TopicDimension eventTypeTopicDimension = new TopicDimension(eventTypeTopic);

            for (Event event : eventType.getEvents()) {
                long previousDays = 0;

                // Iterate event parts
                for (EventDays eventDays : event.getDays()) {
                    long firstDayNumber = previousDays + 1;
                    long days = ChronoUnit.DAYS.between(eventDays.getStartDate(), eventDays.getEndDate()) + 1;
                    long lastDayNumber = previousDays + days;

                    // Year dimension
                    YearDimension yearDimension = new YearDimension(eventDays.getStartDate().getYear());

                    // City dimension
                    CityDimension cityDimension = new CityDimension(cityMap.get(eventDays.getPlace().getCity()));

                    // Event type, city, year and topic dimensions
                    Set<Dimension<?>> eventTypeAndCityAndYearAndTopicDimensions = Set.of(eventTypeDimension, cityDimension, yearDimension, eventTypeTopicDimension);

                    // Event measure values
                    eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.DURATION, EventPart.of(event, eventDays));
                    eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.EVENTS_QUANTITY, event);

                    // Iterate event part talks
                    iterateTalks(cubes, eventTypeDimension, yearDimension, eventTypeTopicDimension, eventTypeAndCityAndYearAndTopicDimensions,
                            event, firstDayNumber, lastDayNumber);

                    previousDays += days;
                }
            }
        }
    }

    void iterateTalks(Cubes cubes, EventTypeDimension eventTypeDimension, YearDimension yearDimension, TopicDimension eventTypeTopicDimension,
                      Set<Dimension<?>> eventTypeAndCityAndYearAndTopicDimensions, Event event,
                      long firstDayNumber, long lastDayNumber) {
        for (Talk talk : event.getTalks()) {
            long safeTalkDay = (talk.getTalkDay() != null) ? talk.getTalkDay() : 1;
            TopicDimension talkTopicDimension = (talk.getTopic() != null) ? new TopicDimension(talk.getTopic()) : eventTypeTopicDimension;

            if ((safeTalkDay >= firstDayNumber) && (safeTalkDay <= lastDayNumber)) {
                // Talk measure values
                cubes.eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.TALKS_QUANTITY, talk);

                iterateSpeakers(cubes, eventTypeDimension, yearDimension, talkTopicDimension, eventTypeAndCityAndYearAndTopicDimensions, event, talk);
            }
        }
    }

    void iterateSpeakers(Cubes cubes, EventTypeDimension eventTypeDimension, YearDimension yearDimension,
                         TopicDimension talkTopicDimension, Set<Dimension<?>> eventTypeAndCityAndYearAndTopicDimensions,
                         Event event, Talk talk) {
        EventType eventType = eventTypeDimension.getValue();

        for (Speaker speaker : talk.getSpeakers()) {
            // Speaker dimension
            SpeakerDimension speakerDimension = new SpeakerDimension(speaker);

            // Event type, speaker, year and topic dimension
            Set<Dimension<?>> eventTypeAndSpeakerAndYearAndTopicDimensions = Set.of(
                    eventTypeDimension, speakerDimension, yearDimension, talkTopicDimension);

            // Speaker measure values
            cubes.eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.SPEAKERS_QUANTITY, speaker);

            cubes.speakersCube.addMeasureEntity(eventTypeAndSpeakerAndYearAndTopicDimensions, MeasureType.TALKS_QUANTITY, talk);
            cubes.speakersCube.addMeasureEntity(eventTypeAndSpeakerAndYearAndTopicDimensions, MeasureType.EVENTS_QUANTITY, event);
            cubes.speakersCube.addMeasureEntity(eventTypeAndSpeakerAndYearAndTopicDimensions, MeasureType.EVENT_TYPES_QUANTITY, eventType);

            if (speaker.isJavaChampion()) {
                cubes.eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.JAVA_CHAMPIONS_QUANTITY, speaker);
            }

            if (speaker.isAnyMvp()) {
                cubes.eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.MVPS_QUANTITY, speaker);
            }

            iterateCompanies(cubes, eventTypeDimension, yearDimension, speakerDimension, talkTopicDimension, eventTypeAndCityAndYearAndTopicDimensions, event, talk);
        }
    }

    void iterateCompanies(Cubes cubes, EventTypeDimension eventTypeDimension, YearDimension yearDimension,
                          SpeakerDimension speakerDimension, TopicDimension talkTopicDimension,
                          Set<Dimension<?>> eventTypeAndCityAndYearAndTopicDimensions, Event event, Talk talk) {
        EventType eventType = eventTypeDimension.getValue();
        Speaker speaker = speakerDimension.getValue();

        for (Company company : speaker.getCompanies()) {
            // Event type, company, speaker, year and topic dimension
            Set<Dimension<?>> eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions = Set.of(
                    eventTypeDimension, new CompanyDimension(company), speakerDimension, yearDimension, talkTopicDimension);

            // Company measure values
            cubes.eventTypesCube.addMeasureEntity(eventTypeAndCityAndYearAndTopicDimensions, MeasureType.COMPANIES_QUANTITY, company);

            cubes.companiesCube.addMeasureEntity(eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions, MeasureType.SPEAKERS_QUANTITY, speaker);
            cubes.companiesCube.addMeasureEntity(eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions, MeasureType.TALKS_QUANTITY, talk);
            cubes.companiesCube.addMeasureEntity(eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions, MeasureType.EVENTS_QUANTITY, event);
            cubes.companiesCube.addMeasureEntity(eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions, MeasureType.EVENT_TYPES_QUANTITY, eventType);

            if (speaker.isJavaChampion()) {
                cubes.companiesCube.addMeasureEntity(eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions, MeasureType.JAVA_CHAMPIONS_QUANTITY, speaker);
            }

            if (speaker.isAnyMvp()) {
                cubes.companiesCube.addMeasureEntity(eventTypeAndCompanyAndSpeakerAndYearAndTopicDimensions, MeasureType.MVPS_QUANTITY, speaker);
            }
        }
    }
}
