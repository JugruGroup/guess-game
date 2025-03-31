package guess.service;

import guess.dao.EventDao;
import guess.dao.EventTypeDao;
import guess.dao.PlaceDao;
import guess.domain.source.*;
import guess.domain.statistics.EventTypeEventMetrics;
import guess.domain.statistics.Metrics;
import guess.domain.statistics.company.CompanyMetrics;
import guess.domain.statistics.company.CompanyMetricsInternal;
import guess.domain.statistics.company.CompanyStatistics;
import guess.domain.statistics.event.EventMetrics;
import guess.domain.statistics.event.EventStatistics;
import guess.domain.statistics.eventplace.EventPlaceMetrics;
import guess.domain.statistics.eventplace.EventPlaceStatistics;
import guess.domain.statistics.eventtype.EventTypeMetrics;
import guess.domain.statistics.eventtype.EventTypeStatistics;
import guess.domain.statistics.speaker.SpeakerMetrics;
import guess.domain.statistics.speaker.SpeakerMetricsInternal;
import guess.domain.statistics.speaker.SpeakerStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Statistics service implementation.
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final EventTypeDao eventTypeDao;
    private final EventDao eventDao;
    private final PlaceDao placeDao;

    @Autowired
    public StatisticsServiceImpl(EventTypeDao eventTypeDao, EventDao eventDao, PlaceDao placeDao) {
        this.eventTypeDao = eventTypeDao;
        this.eventDao = eventDao;
        this.placeDao = placeDao;
    }

    @Override
    public List<EventType> getStatisticsEventTypes(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId, Long eventTypeId) {
        return eventTypeDao.getEventTypes().stream()
                .filter(et -> ((isConferences && et.isEventTypeConference()) || (isMeetups && !et.isEventTypeConference())) &&
                        ((organizerId == null) || (et.getOrganizer().getId() == organizerId)) &&
                        ((topicId == null) || ((et.getTopic() != null) && (et.getTopic().getId() == topicId))) &&
                        ((eventTypeId == null) || (et.getId() == eventTypeId)))
                .toList();
    }

    @Override
    public EventTypeStatistics getEventTypeStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId) {
        List<EventType> eventTypes = getStatisticsEventTypes(isConferences, isMeetups, organizerId, topicId, null);
        List<EventTypeMetrics> eventTypeMetricsList = new ArrayList<>();
        var currentLocalDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDate totalsStartDate = null;
        LocalDate totalsEndDate = null;
        long totalsAge = 0;
        long totalsDuration = 0;
        long totalsEventsQuantity = 0;
        long totalsTalksQuantity = 0;
        Set<Speaker> totalsSpeakers = new HashSet<>();
        Set<Company> totalsCompanies = new HashSet<>();
        BiPredicate<LocalDate, LocalDate> targetNullPredicate = (targetLocalDate, sourceLocalDate) -> (targetLocalDate == null);
        BiPredicate<LocalDate, LocalDate> targetNullOrBeforePredicate = targetNullPredicate
                .or((targetLocalDate, sourceLocalDate) -> sourceLocalDate.isBefore(targetLocalDate));
        BiPredicate<LocalDate, LocalDate> targetNullOrAfterPredicate = targetNullPredicate
                .or((targetLocalDate, sourceLocalDate) -> sourceLocalDate.isAfter(targetLocalDate));
        BiPredicate<LocalDate, LocalDate> sourceNotNullPredicate = (targetLocalDate, sourceLocalDate) -> (sourceLocalDate != null);
        BiPredicate<LocalDate, LocalDate> sourceNotNullAndBeforePredicate = sourceNotNullPredicate
                .and(targetNullOrBeforePredicate);
        BiPredicate<LocalDate, LocalDate> sourceNotNullAndAfterPredicate = sourceNotNullPredicate
                .and(targetNullOrAfterPredicate);

        for (EventType eventType : eventTypes) {
            // Event type metrics
            LocalDate eventTypeStartDate = null;
            LocalDate eventTypeEndDate = null;
            ZoneId eventTypeZoneId = null;
            long eventTypeDuration = 0;
            long eventTypeTalksQuantity = 0;
            Set<Speaker> eventTypeSpeakers = new HashSet<>();
            Set<Company> eventTypeCompanies = new HashSet<>();

            for (Event event : eventType.getEvents()) {
                LocalDate eventStartDate = event.getFirstStartDate();
                LocalDate eventEndDate = event.getLastEndDate();

                if (targetNullOrBeforePredicate.test(eventTypeStartDate, eventStartDate)) {
                    eventTypeStartDate = eventStartDate;
                    eventTypeZoneId = event.getFinalTimeZoneId();
                }

                if (targetNullOrAfterPredicate.test(eventTypeEndDate, eventEndDate)) {
                    eventTypeEndDate = eventEndDate;
                }

                eventTypeDuration += event.getDuration();
                eventTypeTalksQuantity += event.getTalks().size();

                event.getTalks().forEach(t -> {
                    eventTypeSpeakers.addAll(t.getSpeakers());

                    Set<Company> companies = t.getSpeakers().stream()
                            .flatMap(s -> s.getCompanies().stream())
                            .collect(Collectors.toSet());

                    eventTypeCompanies.addAll(companies);
                });
            }

            long eventTypeAge = getEventTypeAge(eventTypeStartDate, eventTypeZoneId, currentLocalDateTime);
            long eventTypeJavaChampionsQuantity = eventTypeSpeakers.stream()
                    .filter(Speaker::isJavaChampion)
                    .count();
            long eventTypeMvpsQuantity = eventTypeSpeakers.stream()
                    .filter(Speaker::isAnyMvp)
                    .count();

            eventTypeMetricsList.add(new EventTypeMetrics(
                    eventType,
                    eventTypeAge,
                    eventType.getEvents().size(),
                    new EventTypeEventMetrics(
                            eventTypeStartDate,
                            eventTypeEndDate,
                            eventTypeDuration,
                            eventTypeSpeakers.size(),
                            eventTypeCompanies.size(),
                            new Metrics(
                                    eventTypeTalksQuantity,
                                    eventTypeJavaChampionsQuantity,
                                    eventTypeMvpsQuantity
                            )
                    )
            ));

            // Totals metrics
            if (sourceNotNullAndBeforePredicate.test(totalsStartDate, eventTypeStartDate)) {
                totalsStartDate = eventTypeStartDate;
            }

            if (sourceNotNullAndAfterPredicate.test(totalsEndDate, eventTypeEndDate)) {
                totalsEndDate = eventTypeEndDate;
            }

            if (totalsAge < eventTypeAge) {
                totalsAge = eventTypeAge;
            }

            totalsDuration += eventTypeDuration;
            totalsEventsQuantity += eventType.getEvents().size();
            totalsTalksQuantity += eventTypeTalksQuantity;
            totalsSpeakers.addAll(eventTypeSpeakers);
            totalsCompanies.addAll(eventTypeCompanies);
        }

        long totalsJavaChampionsQuantity = totalsSpeakers.stream()
                .filter(Speaker::isJavaChampion)
                .count();
        long totalsMvpsQuantity = totalsSpeakers.stream()
                .filter(Speaker::isAnyMvp)
                .count();

        return new EventTypeStatistics(
                eventTypeMetricsList,
                new EventTypeMetrics(
                        new EventType(),
                        totalsAge,
                        totalsEventsQuantity,
                        new EventTypeEventMetrics(
                                totalsStartDate,
                                totalsEndDate,
                                totalsDuration,
                                totalsSpeakers.size(),
                                totalsCompanies.size(),
                                new Metrics(
                                        totalsTalksQuantity,
                                        totalsJavaChampionsQuantity,
                                        totalsMvpsQuantity
                                )
                        )
                ));
    }

    /**
     * Gets event type age.
     *
     * @param eventTypeStartDate   start date of event type
     * @param zoneId               time-zone ID of event type
     * @param currentLocalDateTime current local date time in UTC
     * @return event type age
     */
    static long getEventTypeAge(LocalDate eventTypeStartDate, ZoneId zoneId, LocalDateTime currentLocalDateTime) {
        if (eventTypeStartDate == null) {
            return 0;
        } else {
            LocalDateTime eventTypeStartLocalDateTime = ZonedDateTime.of(
                            eventTypeStartDate,
                            LocalTime.of(0, 0, 0),
                            zoneId)
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime();

            return Math.max(0, ChronoUnit.YEARS.between(eventTypeStartLocalDateTime, currentLocalDateTime));
        }
    }

    @Override
    public EventStatistics getEventStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        List<Event> events = eventDao.getEvents().stream()
                .filter(e ->
                        ((isConferences && e.getEventType().isEventTypeConference()) || (isMeetups && !e.getEventType().isEventTypeConference())) &&
                                ((organizerId == null) || (e.getEventType().getOrganizer().getId() == organizerId)) &&
                                ((eventTypeId == null) || (e.getEventType().getId() == eventTypeId)))
                .toList();
        List<EventMetrics> eventMetricsList = new ArrayList<>();
        LocalDate totalsStartDate = null;
        LocalDate totalsEndDate = null;
        long totalsDuration = 0;
        long totalsTalksQuantity = 0;
        Set<Speaker> totalsSpeakers = new HashSet<>();
        Set<Company> totalsCompanies = new HashSet<>();

        for (Event event : events) {
            // Event metrics
            long eventDuration = event.getDuration();
            long eventTalksQuantity = event.getTalks().size();
            Set<Speaker> eventSpeakers = new HashSet<>();
            Set<Company> eventCompanies = new HashSet<>();

            event.getTalks().forEach(t -> {
                eventSpeakers.addAll(t.getSpeakers());

                Set<Company> companies = t.getSpeakers().stream()
                        .flatMap(s -> s.getCompanies().stream())
                        .collect(Collectors.toSet());

                eventCompanies.addAll(companies);
            });

            long eventJavaChampionsQuantity = eventSpeakers.stream()
                    .filter(Speaker::isJavaChampion)
                    .count();
            long eventMvpsQuantity = eventSpeakers.stream()
                    .filter(Speaker::isAnyMvp)
                    .count();
            LocalDate eventStartDate = event.getFirstStartDate();
            LocalDate eventEndDate = event.getLastEndDate();

            eventMetricsList.add(new EventMetrics(
                    event,
                    new EventTypeEventMetrics(
                            eventStartDate,
                            eventEndDate,
                            eventDuration,
                            eventSpeakers.size(),
                            eventCompanies.size(),
                            new Metrics(
                                    eventTalksQuantity,
                                    eventJavaChampionsQuantity,
                                    eventMvpsQuantity
                            )
                    )
            ));

            // Totals metrics
            if ((totalsStartDate == null) || eventStartDate.isBefore(totalsStartDate)) {
                totalsStartDate = eventStartDate;
            }

            if ((totalsEndDate == null) || eventEndDate.isAfter(totalsEndDate)) {
                totalsEndDate = eventEndDate;
            }

            totalsDuration += eventDuration;
            totalsTalksQuantity += eventTalksQuantity;
            totalsSpeakers.addAll(eventSpeakers);
            totalsCompanies.addAll(eventCompanies);
        }

        long totalsJavaChampionsQuantity = totalsSpeakers.stream()
                .filter(Speaker::isJavaChampion)
                .count();
        long totalsMvpsQuantity = totalsSpeakers.stream()
                .filter(Speaker::isAnyMvp)
                .count();

        return new EventStatistics(
                eventMetricsList,
                new EventMetrics(
                        new Event(),
                        new EventTypeEventMetrics(
                                totalsStartDate,
                                totalsEndDate,
                                totalsDuration,
                                totalsSpeakers.size(),
                                totalsCompanies.size(),
                                new Metrics(
                                        totalsTalksQuantity,
                                        totalsJavaChampionsQuantity,
                                        totalsMvpsQuantity
                                )
                        )
                ));
    }

    static Map<Long, Place> getTalkDayPlaces(List<EventDays> eventDaysList) {
        Map<Long, Place> talkDayDates = new HashMap<>();
        long previousDays = 0;

        for (EventDays eventDays : eventDaysList) {
            long days = ChronoUnit.DAYS.between(eventDays.getStartDate(), eventDays.getEndDate()) + 1;

            for (long i = 1; i <= days; i++) {
                talkDayDates.put(previousDays + i, eventDays.getPlace());
            }

            previousDays += days;
        }

        return talkDayDates;
    }

    @Override
    public EventPlaceStatistics getEventPlaceStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        List<Event> events = eventDao.getEvents().stream()
                .filter(e ->
                        ((isConferences && e.getEventType().isEventTypeConference()) || (isMeetups && !e.getEventType().isEventTypeConference())) &&
                                ((organizerId == null) || (e.getEventType().getOrganizer().getId() == organizerId)) &&
                                ((eventTypeId == null) || (e.getEventType().getId() == eventTypeId)))
                .toList();
        Map<Place, Set<Event>> placeEvents = new HashMap<>();
        Map<Place, Set<EventType>> placeEventTypes = new HashMap<>();
        Map<Place, LocalDate> placeStartDates = new HashMap<>();
        Map<Place, LocalDate> placeEndDates = new HashMap<>();
        Map<Place, AtomicLong> placeDurations = new HashMap<>();
        Map<Place, AtomicLong> placeTalksQuantities = new HashMap<>();
        Map<Place, Set<Speaker>> placeSpeakers = new HashMap<>();
        Map<Place, Set<Company>> placeCompanies = new HashMap<>();
        BiPredicate<LocalDate, LocalDate> targetNullPredicate = (targetLocalDate, sourceLocalDate) -> (targetLocalDate == null);
        BiPredicate<LocalDate, LocalDate> targetNullOrBeforePredicate = targetNullPredicate
                .or((targetLocalDate, sourceLocalDate) -> sourceLocalDate.isBefore(targetLocalDate));
        BiPredicate<LocalDate, LocalDate> targetNullOrAfterPredicate = targetNullPredicate
                .or((targetLocalDate, sourceLocalDate) -> sourceLocalDate.isAfter(targetLocalDate));

        placeDao.getPlaces().forEach(place -> {
            placeEvents.put(place, new HashSet<>());
            placeEventTypes.put(place, new HashSet<>());
            placeStartDates.put(place, null);
            placeEndDates.put(place, null);
            placeDurations.put(place, new AtomicLong(0));
            placeTalksQuantities.put(place, new AtomicLong(0));
            placeSpeakers.put(place, new HashSet<>());
            placeCompanies.put(place, new HashSet<>());
        });

        for (Event event : events) {
            Map<Long, Place> talkDayPlaces = getTalkDayPlaces(event.getDays());

            for (EventDays eventDays : event.getDays()) {
                // Event place metrics
                Place place = eventDays.getPlace();
                placeEvents.get(place).add(event);
                placeEventTypes.get(place).add(event.getEventType());

                LocalDate placeStartDate = placeStartDates.get(place);
                LocalDate placeEndDate = placeEndDates.get(place);
                LocalDate eventPartStartDate = eventDays.getStartDate();
                LocalDate eventPartEndDate = eventDays.getEndDate();

                if (targetNullOrBeforePredicate.test(placeStartDate, eventPartStartDate)) {
                    placeStartDates.put(place, eventPartStartDate);
                }

                if (targetNullOrAfterPredicate.test(placeEndDate, eventPartEndDate)) {
                    placeEndDates.put(place, eventPartEndDate);
                }

                placeDurations.get(place).addAndGet(ChronoUnit.DAYS.between(eventPartStartDate, eventPartEndDate) + 1);
            }

            event.getTalks().forEach(t -> {
                Place place = talkDayPlaces.get(t.getTalkDay());

                placeTalksQuantities.get(place).incrementAndGet();
                placeSpeakers.get(place).addAll(t.getSpeakers());

                Set<Company> companies = t.getSpeakers().stream()
                        .flatMap(s -> s.getCompanies().stream())
                        .collect(Collectors.toSet());

                placeCompanies.get(place).addAll(companies);
            });
        }

        // Event place metrics
        List<EventPlaceMetrics> eventPlaceMetricsList = placeDao.getPlaces().stream()
                .filter(place -> !placeEvents.get(place).isEmpty())
                .map(place -> {
                    Set<Speaker> speakers = placeSpeakers.get(place);
                    long placeJavaChampionsQuantity = speakers.stream()
                            .filter(Speaker::isJavaChampion)
                            .count();
                    long placeMvpsQuantity = speakers.stream()
                            .filter(Speaker::isAnyMvp)
                            .count();

                    return new EventPlaceMetrics(
                            place,
                            placeEvents.get(place).size(),
                            placeEventTypes.get(place).size(),
                            new EventTypeEventMetrics(
                                    placeStartDates.get(place),
                                    placeEndDates.get(place),
                                    placeDurations.get(place).get(),
                                    speakers.size(),
                                    placeCompanies.get(place).size(),
                                    new Metrics(
                                            placeTalksQuantities.get(place).get(),
                                            placeJavaChampionsQuantity,
                                            placeMvpsQuantity
                                    )
                            )
                    );
                })
                .toList();

        // Totals metrics
        long totalsEventsQuantity = placeEvents.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .size();

        long totalsEventTypesQuantity = placeEventTypes.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .size();

        List<LocalDate> startDates = placeStartDates.values().stream()
                .filter(Objects::nonNull)
                .toList();
        LocalDate totalsStartDate = !startDates.isEmpty() ? Collections.min(startDates) : null;

        List<LocalDate> endDates = placeEndDates.values().stream()
                .filter(Objects::nonNull)
                .toList();
        LocalDate totalsEndDate = !endDates.isEmpty() ? Collections.max(endDates) : null;

        long totalsDuration = placeDurations.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        long totalsTalksQuantity = placeTalksQuantities.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        Set<Speaker> totalsSpeakers = placeSpeakers.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Company> totalsCompanies = placeCompanies.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        long totalsJavaChampionsQuantity = totalsSpeakers.stream()
                .filter(Speaker::isJavaChampion)
                .count();
        long totalsMvpsQuantity = totalsSpeakers.stream()
                .filter(Speaker::isAnyMvp)
                .count();

        return new EventPlaceStatistics(
                eventPlaceMetricsList,
                new EventPlaceMetrics(
                        new Place(),
                        totalsEventsQuantity,
                        totalsEventTypesQuantity,
                        new EventTypeEventMetrics(
                                totalsStartDate,
                                totalsEndDate,
                                totalsDuration,
                                totalsSpeakers.size(),
                                totalsCompanies.size(),
                                new Metrics(
                                        totalsTalksQuantity,
                                        totalsJavaChampionsQuantity,
                                        totalsMvpsQuantity
                                )
                        )
                )
        );
    }

    @Override
    public SpeakerStatistics getSpeakerStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        List<EventType> eventTypes = getStatisticsEventTypes(isConferences, isMeetups, organizerId, null, eventTypeId);
        Map<Speaker, SpeakerMetricsInternal> speakerSpeakerMetricsMap = new LinkedHashMap<>();
        long totalsTalksQuantity = 0;
        long totalsEventsQuantity = 0;

        for (EventType eventType : eventTypes) {
            for (Event event : eventType.getEvents()) {
                for (Talk talk : event.getTalks()) {
                    // Speaker metrics
                    talk.getSpeakers().stream()
                            .map(s -> speakerSpeakerMetricsMap.computeIfAbsent(s, k -> new SpeakerMetricsInternal()))
                            .forEach(smi -> {
                                smi.getTalks().add(talk);
                                smi.getEvents().add(event);
                                smi.getEventTypes().add(eventType);
                            });
                }

                totalsTalksQuantity += event.getTalks().size();
            }

            totalsEventsQuantity += eventType.getEvents().size();
        }

        List<SpeakerMetrics> speakerMetricsList = new ArrayList<>();

        for (Map.Entry<Speaker, SpeakerMetricsInternal> entry : speakerSpeakerMetricsMap.entrySet()) {
            var speaker = entry.getKey();
            var speakerMetricsInternal = entry.getValue();

            speakerMetricsList.add(new SpeakerMetrics(
                    speaker,
                    speakerMetricsInternal.getTalks().size(),
                    speakerMetricsInternal.getEvents().size(),
                    speakerMetricsInternal.getEventTypes().size(),
                    speaker.isJavaChampion() ? 1 : 0,
                    speaker.isAnyMvp() ? 1 : 0));
        }

        // Totals metrics
        long totalsJavaChampionsQuantity = speakerSpeakerMetricsMap.keySet().stream()
                .filter(Speaker::isJavaChampion)
                .count();
        long totalsMvpsQuantity = speakerSpeakerMetricsMap.keySet().stream()
                .filter(Speaker::isAnyMvp)
                .count();

        return new SpeakerStatistics(
                speakerMetricsList,
                new SpeakerMetrics(
                        new Speaker(),
                        totalsTalksQuantity,
                        totalsEventsQuantity,
                        eventTypes.size(),
                        totalsJavaChampionsQuantity,
                        totalsMvpsQuantity)
        );
    }

    @Override
    public CompanyStatistics getCompanyStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        List<EventType> eventTypes = getStatisticsEventTypes(isConferences, isMeetups, organizerId, null, eventTypeId);
        Map<Company, CompanyMetricsInternal> companySpeakerMetricsMap = new LinkedHashMap<>();

        for (EventType eventType : eventTypes) {
            for (Event event : eventType.getEvents()) {
                for (Talk talk : event.getTalks()) {
                    for (Speaker speaker : talk.getSpeakers()) {
                        // Company metrics
                        speaker.getCompanies().stream()
                                .map(c -> companySpeakerMetricsMap.computeIfAbsent(c, k -> new CompanyMetricsInternal()))
                                .forEach(cmi -> {
                                    cmi.getSpeakers().add(speaker);
                                    cmi.getTalks().add(talk);
                                    cmi.getEvents().add(event);
                                    cmi.getEventTypes().add(eventType);
                                });
                    }
                }
            }
        }

        List<CompanyMetrics> companyMetricsList = new ArrayList<>();
        Set<Speaker> speakerMap = new HashSet<>();
        Set<Talk> talkMap = new HashSet<>();
        Set<Event> eventMap = new HashSet<>();
        Set<EventType> eventTypeMap = new HashSet<>();

        for (Map.Entry<Company, CompanyMetricsInternal> entry : companySpeakerMetricsMap.entrySet()) {
            var company = entry.getKey();
            var companyMetricsInternal = entry.getValue();
            long javaChampionsQuantity = companyMetricsInternal.getSpeakers().stream()
                    .filter(Speaker::isJavaChampion)
                    .count();
            long mvpsQuantity = companyMetricsInternal.getSpeakers().stream()
                    .filter(Speaker::isAnyMvp)
                    .count();

            companyMetricsList.add(new CompanyMetrics(
                    company,
                    companyMetricsInternal.getSpeakers().size(),
                    companyMetricsInternal.getTalks().size(),
                    companyMetricsInternal.getEvents().size(),
                    companyMetricsInternal.getEventTypes().size(),
                    javaChampionsQuantity,
                    mvpsQuantity
            ));

            speakerMap.addAll(companyMetricsInternal.getSpeakers());
            talkMap.addAll(companyMetricsInternal.getTalks());
            eventMap.addAll(companyMetricsInternal.getEvents());
            eventTypeMap.addAll(companyMetricsInternal.getEventTypes());
        }

        // Totals metrics
        long totalsJavaChampionsQuantity = speakerMap.stream()
                .filter(Speaker::isJavaChampion)
                .count();
        long totalsMvpsQuantity = speakerMap.stream()
                .filter(Speaker::isAnyMvp)
                .count();

        return new CompanyStatistics(
                companyMetricsList,
                new CompanyMetrics(
                        new Company(),
                        speakerMap.size(),
                        talkMap.size(),
                        eventMap.size(),
                        eventTypeMap.size(),
                        totalsJavaChampionsQuantity,
                        totalsMvpsQuantity
                )
        );
    }
}
