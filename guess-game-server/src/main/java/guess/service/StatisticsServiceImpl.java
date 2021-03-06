package guess.service;

import guess.dao.EventDao;
import guess.dao.EventTypeDao;
import guess.domain.source.*;
import guess.domain.statistics.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistics service implementation.
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final EventTypeDao eventTypeDao;
    private final EventDao eventDao;

    @Autowired
    public StatisticsServiceImpl(EventTypeDao eventTypeDao, EventDao eventDao) {
        this.eventTypeDao = eventTypeDao;
        this.eventDao = eventDao;
    }

    @Override
    public List<EventType> getStatisticsEventTypes(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        return eventTypeDao.getEventTypes().stream()
                .filter(et -> ((isConferences && et.isEventTypeConference()) || (isMeetups && !et.isEventTypeConference())) &&
                        ((organizerId == null) || (et.getOrganizer().getId() == organizerId)) &&
                        ((eventTypeId == null) || (et.getId() == eventTypeId)))
                .collect(Collectors.toList());
    }

    @Override
    public EventTypeStatistics getEventTypeStatistics(boolean isConferences, boolean isMeetups, Long organizerId) {
        List<EventType> eventTypes = getStatisticsEventTypes(isConferences, isMeetups, organizerId, null);
        List<EventTypeMetrics> eventTypeMetricsList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate totalsStartDate = currentDate;
        long totalsDuration = 0;
        long totalsEventsQuantity = 0;
        long totalsTalksQuantity = 0;
        Set<Speaker> totalsSpeakers = new HashSet<>();

        for (EventType eventType : eventTypes) {
            // Event type metrics
            LocalDate eventTypeStartDate = currentDate;
            long eventTypeDuration = 0;
            long eventTypeTalksQuantity = 0;
            Set<Speaker> eventTypeSpeakers = new HashSet<>();

            for (Event event : eventType.getEvents()) {
                if (event.getStartDate().isBefore(eventTypeStartDate)) {
                    eventTypeStartDate = event.getStartDate();
                }

                eventTypeDuration += (ChronoUnit.DAYS.between(event.getStartDate(), event.getEndDate()) + 1);
                eventTypeTalksQuantity += event.getTalks().size();
                event.getTalks().forEach(t -> eventTypeSpeakers.addAll(t.getSpeakers()));
            }

            long eventTypeJavaChampionsQuantity = eventTypeSpeakers.stream()
                    .filter(Speaker::isJavaChampion)
                    .count();
            long eventTypeMvpsQuantity = eventTypeSpeakers.stream()
                    .filter(Speaker::isAnyMvp)
                    .count();

            eventTypeMetricsList.add(new EventTypeMetrics(
                    eventType,
                    eventTypeStartDate,
                    ChronoUnit.YEARS.between(eventTypeStartDate, currentDate),
                    eventTypeDuration,
                    eventType.getEvents().size(),
                    eventTypeSpeakers.size(),
                    new Metrics(eventTypeTalksQuantity, eventTypeJavaChampionsQuantity, eventTypeMvpsQuantity)
            ));

            // Totals metrics
            if (eventTypeStartDate.isBefore(totalsStartDate)) {
                totalsStartDate = eventTypeStartDate;
            }

            totalsDuration += eventTypeDuration;
            totalsEventsQuantity += eventType.getEvents().size();
            totalsTalksQuantity += eventTypeTalksQuantity;
            totalsSpeakers.addAll(eventTypeSpeakers);
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
                        totalsStartDate,
                        ChronoUnit.YEARS.between(totalsStartDate, currentDate),
                        totalsDuration,
                        totalsEventsQuantity,
                        totalsSpeakers.size(),
                        new Metrics(totalsTalksQuantity, totalsJavaChampionsQuantity, totalsMvpsQuantity)
                ));
    }

    @Override
    public EventStatistics getEventStatistics(Long eventTypeId) {
        List<Event> events = eventDao.getEvents().stream()
                .filter(e ->
                        (e.getEventType().isEventTypeConference() &&
                                ((eventTypeId == null) || (e.getEventType().getId() == eventTypeId))))
                .collect(Collectors.toList());
        List<EventMetrics> eventMetricsList = new ArrayList<>();
        LocalDate totalsStartDate = LocalDate.now();
        long totalsDuration = 0;
        long totalsTalksQuantity = 0;
        Set<Speaker> totalsSpeakers = new HashSet<>();

        for (Event event : events) {
            // Event metrics
            long eventDuration = (ChronoUnit.DAYS.between(event.getStartDate(), event.getEndDate()) + 1);
            long eventTalksQuantity = event.getTalks().size();
            Set<Speaker> eventSpeakers = new HashSet<>();

            event.getTalks().forEach(t -> eventSpeakers.addAll(t.getSpeakers()));

            long eventJavaChampionsQuantity = eventSpeakers.stream()
                    .filter(Speaker::isJavaChampion)
                    .count();
            long eventMvpsQuantity = eventSpeakers.stream()
                    .filter(Speaker::isAnyMvp)
                    .count();

            eventMetricsList.add(new EventMetrics(
                    event,
                    event.getStartDate(),
                    eventDuration,
                    eventTalksQuantity,
                    eventSpeakers.size(),
                    eventJavaChampionsQuantity,
                    eventMvpsQuantity));

            // Totals metrics
            if (event.getStartDate().isBefore(totalsStartDate)) {
                totalsStartDate = event.getStartDate();
            }

            totalsDuration += eventDuration;
            totalsTalksQuantity += eventTalksQuantity;
            totalsSpeakers.addAll(eventSpeakers);
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
                        totalsStartDate,
                        totalsDuration,
                        totalsTalksQuantity,
                        totalsSpeakers.size(),
                        totalsJavaChampionsQuantity,
                        totalsMvpsQuantity));
    }

    @Override
    public SpeakerStatistics getSpeakerStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        List<EventType> eventTypes = getStatisticsEventTypes(isConferences, isMeetups, organizerId, eventTypeId);
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
            Speaker speaker = entry.getKey();
            SpeakerMetricsInternal speakerMetricsInternal = entry.getValue();

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
        List<EventType> eventTypes = getStatisticsEventTypes(isConferences, isMeetups, organizerId, eventTypeId);
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
            Company company = entry.getKey();
            CompanyMetricsInternal companyMetricsInternal = entry.getValue();
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

    @Override
    public List<EventType> getConferences() {
        return eventTypeDao.getEventTypes().stream()
                .filter(EventType::isEventTypeConference)
                .collect(Collectors.toList());
    }
}
