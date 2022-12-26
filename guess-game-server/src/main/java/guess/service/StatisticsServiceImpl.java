package guess.service;

import guess.dao.EventDao;
import guess.dao.EventTypeDao;
import guess.domain.source.*;
import guess.domain.statistics.EventTypeEventMetrics;
import guess.domain.statistics.company.CompanyMetrics;
import guess.domain.statistics.company.CompanyMetricsInternal;
import guess.domain.statistics.company.CompanyStatistics;
import guess.domain.statistics.event.EventMetrics;
import guess.domain.statistics.event.EventStatistics;
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
        long totalsAge = 0;
        long totalsDuration = 0;
        long totalsEventsQuantity = 0;
        long totalsTalksQuantity = 0;
        Set<Speaker> totalsSpeakers = new HashSet<>();
        Set<Company> totalsCompanies = new HashSet<>();

        for (EventType eventType : eventTypes) {
            // Event type metrics
            LocalDate eventTypeStartDate = null;
            ZoneId eventTypeZoneId = null;
            long eventTypeDuration = 0;
            long eventTypeTalksQuantity = 0;
            Set<Speaker> eventTypeSpeakers = new HashSet<>();
            Set<Company> eventTypeCompanies = new HashSet<>();

            for (Event event : eventType.getEvents()) {
                LocalDate eventStartDate = event.getFirstStartDate();

                if ((eventTypeStartDate == null) || eventStartDate.isBefore(eventTypeStartDate)) {
                    eventTypeStartDate = eventStartDate;
                    eventTypeZoneId = event.getFinalTimeZoneId();
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
                    eventTypeStartDate,
                    eventTypeAge,
                    eventTypeDuration,
                    eventType.getEvents().size(),
                    new EventTypeEventMetrics(
                            eventTypeTalksQuantity,
                            eventTypeSpeakers.size(),
                            eventTypeCompanies.size(),
                            eventTypeJavaChampionsQuantity,
                            eventTypeMvpsQuantity)
            ));

            // Totals metrics
            if ((eventTypeStartDate != null) &&
                    ((totalsStartDate == null) || eventTypeStartDate.isBefore(totalsStartDate))) {
                totalsStartDate = eventTypeStartDate;
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
                        totalsStartDate,
                        totalsAge,
                        totalsDuration,
                        totalsEventsQuantity,
                        new EventTypeEventMetrics(
                                totalsTalksQuantity,
                                totalsSpeakers.size(),
                                totalsCompanies.size(),
                                totalsJavaChampionsQuantity,
                                totalsMvpsQuantity)
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

            eventMetricsList.add(new EventMetrics(
                    event,
                    eventStartDate,
                    eventDuration,
                    new EventTypeEventMetrics(eventTalksQuantity,
                            eventSpeakers.size(),
                            eventCompanies.size(),
                            eventJavaChampionsQuantity,
                            eventMvpsQuantity)
            ));

            // Totals metrics
            if ((totalsStartDate == null) || eventStartDate.isBefore(totalsStartDate)) {
                totalsStartDate = eventStartDate;
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
                        totalsStartDate,
                        totalsDuration,
                        new EventTypeEventMetrics(totalsTalksQuantity,
                                totalsSpeakers.size(),
                                totalsCompanies.size(),
                                totalsJavaChampionsQuantity,
                                totalsMvpsQuantity)
                ));
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
