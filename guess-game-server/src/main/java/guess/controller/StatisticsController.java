package guess.controller;

import guess.domain.Language;
import guess.domain.statistics.olap.CubeType;
import guess.domain.statistics.olap.MeasureType;
import guess.dto.statistics.company.CompanyMetricsDto;
import guess.dto.statistics.company.CompanyStatisticsDto;
import guess.dto.statistics.event.EventMetricsDto;
import guess.dto.statistics.event.EventStatisticsDto;
import guess.dto.statistics.eventplace.EventPlaceMetricsDto;
import guess.dto.statistics.eventplace.EventPlaceStatisticsDto;
import guess.dto.statistics.eventtype.EventTypeMetricsDto;
import guess.dto.statistics.eventtype.EventTypeStatisticsDto;
import guess.dto.statistics.olap.metrics.OlapCityMetricsDto;
import guess.dto.statistics.olap.metrics.OlapCompanyMetricsDto;
import guess.dto.statistics.olap.metrics.OlapEventTypeMetricsDto;
import guess.dto.statistics.olap.metrics.OlapSpeakerMetricsDto;
import guess.dto.statistics.olap.parameters.OlapCityParametersDto;
import guess.dto.statistics.olap.parameters.OlapEventTypeParametersDto;
import guess.dto.statistics.olap.parameters.OlapParametersDto;
import guess.dto.statistics.olap.parameters.OlapSpeakerParametersDto;
import guess.dto.statistics.olap.statistics.*;
import guess.dto.statistics.speaker.SpeakerMetricsDto;
import guess.dto.statistics.speaker.SpeakerStatisticsDto;
import guess.service.OlapService;
import guess.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Statistics controller.
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final OlapService olapService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService, OlapService olapService) {
        this.statisticsService = statisticsService;
        this.olapService = olapService;
    }

    @GetMapping("/event-type-statistics")
    public EventTypeStatisticsDto getEventTypeStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                         @RequestParam(required = false) Long organizerId,
                                                         @RequestParam(required = false) Long topicId,
                                                         @RequestParam String language) {
        var eventTypeStatistics = statisticsService.getEventTypeStatistics(conferences, meetups, organizerId, topicId);
        var eventTypeStatisticsDto = EventTypeStatisticsDto.convertToDto(eventTypeStatistics, Language.getLanguageByCode(language));
        Comparator<EventTypeMetricsDto> comparatorByIsConference = Comparator.comparing(EventTypeMetricsDto::isConference).reversed();
        Comparator<EventTypeMetricsDto> comparatorByOrganizerName = Comparator.comparing(EventTypeMetricsDto::getOrganizerName, String.CASE_INSENSITIVE_ORDER);
        Comparator<EventTypeMetricsDto> comparatorByName = Comparator.comparing(EventTypeMetricsDto::getDisplayName, String.CASE_INSENSITIVE_ORDER);

        List<EventTypeMetricsDto> sortedEventTypeMetricsList = eventTypeStatisticsDto.eventTypeMetricsList().stream()
                .sorted(comparatorByIsConference.thenComparing(comparatorByOrganizerName).thenComparing(comparatorByName))
                .toList();

        return new EventTypeStatisticsDto(sortedEventTypeMetricsList, eventTypeStatisticsDto.totals());
    }

    @GetMapping("/event-statistics")
    public EventStatisticsDto getEventStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                 @RequestParam(required = false) Long organizerId,
                                                 @RequestParam(required = false) Long eventTypeId,
                                                 @RequestParam String language) {
        var eventStatistics = statisticsService.getEventStatistics(conferences, meetups, organizerId, eventTypeId);
        var eventStatisticsDto = EventStatisticsDto.convertToDto(eventStatistics, Language.getLanguageByCode(language));

        List<EventMetricsDto> sortedEventMetricsList = eventStatisticsDto.eventMetricsList().stream()
                .sorted(Comparator.comparing(EventMetricsDto::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new EventStatisticsDto(sortedEventMetricsList, eventStatisticsDto.totals());
    }

    @GetMapping("/event-place-statistics")
    public EventPlaceStatisticsDto getgetEventPlaceStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                              @RequestParam(required = false) Long organizerId,
                                                              @RequestParam(required = false) Long eventTypeId,
                                                              @RequestParam String language) {
        var eventPlaceStatistics = statisticsService.getEventPlaceStatistics(conferences, meetups, organizerId, eventTypeId);
        var eventPlaceStatisticsDto = EventPlaceStatisticsDto.convertToDto(eventPlaceStatistics, Language.getLanguageByCode(language));
        Comparator<EventPlaceMetricsDto> comparatorByEventsQuantity = Comparator.comparing(EventPlaceMetricsDto::getEventsQuantity).reversed();
        Comparator<EventPlaceMetricsDto> comparatorByEventTypesQuantity = Comparator.comparing(EventPlaceMetricsDto::getEventTypesQuantity).reversed();

        List<EventPlaceMetricsDto> sortedEventPlaceMetricsList = eventPlaceStatisticsDto.eventPlaceMetricsList().stream()
                .sorted(comparatorByEventsQuantity.thenComparing(comparatorByEventTypesQuantity))
                .toList();

        return new EventPlaceStatisticsDto(sortedEventPlaceMetricsList, eventPlaceStatisticsDto.totals());
    }

    @GetMapping("/speaker-statistics")
    public SpeakerStatisticsDto getSpeakerStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                     @RequestParam(required = false) Long organizerId,
                                                     @RequestParam(required = false) Long eventTypeId,
                                                     @RequestParam String language) {
        var speakerStatistics = statisticsService.getSpeakerStatistics(conferences, meetups, organizerId, eventTypeId);
        var speakerStatisticsDto = SpeakerStatisticsDto.convertToDto(speakerStatistics, Language.getLanguageByCode(language));
        Comparator<SpeakerMetricsDto> comparatorByTalksQuantity = Comparator.comparing(SpeakerMetricsDto::getTalksQuantity).reversed();
        Comparator<SpeakerMetricsDto> comparatorByEventsQuantity = Comparator.comparing(SpeakerMetricsDto::getEventsQuantity).reversed();
        Comparator<SpeakerMetricsDto> comparatorByEventTypesQuantity = Comparator.comparing(SpeakerMetricsDto::getEventTypesQuantity).reversed();

        List<SpeakerMetricsDto> sortedSpeakerMetricsList = speakerStatisticsDto.speakerMetricsList().stream()
                .sorted(comparatorByTalksQuantity.thenComparing(comparatorByEventsQuantity).thenComparing(comparatorByEventTypesQuantity))
                .toList();

        return new SpeakerStatisticsDto(sortedSpeakerMetricsList, speakerStatisticsDto.totals());
    }

    @GetMapping("/company-statistics")
    public CompanyStatisticsDto getCompanyStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                     @RequestParam(required = false) Long organizerId,
                                                     @RequestParam(required = false) Long eventTypeId,
                                                     @RequestParam String language) {
        var companyStatistics = statisticsService.getCompanyStatistics(conferences, meetups, organizerId, eventTypeId);
        var companyStatisticsDto = CompanyStatisticsDto.convertToDto(companyStatistics, Language.getLanguageByCode(language));
        Comparator<CompanyMetricsDto> comparatorByTalksQuantity = Comparator.comparing(CompanyMetricsDto::getTalksQuantity).reversed();
        Comparator<CompanyMetricsDto> comparatorByEventsQuantity = Comparator.comparing(CompanyMetricsDto::getEventsQuantity).reversed();
        Comparator<CompanyMetricsDto> comparatorByEventTypesQuantity = Comparator.comparing(CompanyMetricsDto::getEventTypesQuantity).reversed();

        List<CompanyMetricsDto> sortedCompanyMetricsList = companyStatisticsDto.companyMetricsList().stream()
                .sorted(comparatorByTalksQuantity.thenComparing(comparatorByEventsQuantity).thenComparing(comparatorByEventTypesQuantity))
                .toList();

        return new CompanyStatisticsDto(sortedCompanyMetricsList, companyStatisticsDto.totals());
    }

    @GetMapping("/cube-types")
    public List<CubeType> getCubeTypes() {
        return List.of(CubeType.values());
    }

    @GetMapping("/measure-types")
    public List<MeasureType> getMeasureTypes(@RequestParam String cubeType) {
        if (cubeType.isEmpty()) {
            return Collections.emptyList();
        } else {
            return olapService.getMeasureTypes(CubeType.valueOf(cubeType));
        }
    }

    @PostMapping("/olap-statistics")
    public OlapStatisticsDto getOlapStatistics(@RequestBody OlapParametersDto olapParameters, @RequestParam String language) {
        var olapStatistics = olapService.getOlapStatistics(olapParameters);
        var olapStatisticsDto = OlapStatisticsDto.convertToDto(olapStatistics, Language.getLanguageByCode(language));

        if (olapStatisticsDto.eventTypeStatistics() != null) {
            Comparator<OlapEventTypeMetricsDto> comparatorByIsConference = Comparator.comparing(OlapEventTypeMetricsDto::isConference).reversed();
            Comparator<OlapEventTypeMetricsDto> comparatorByOrganizerName = Comparator.comparing(OlapEventTypeMetricsDto::getOrganizerName, String.CASE_INSENSITIVE_ORDER);
            Comparator<OlapEventTypeMetricsDto> comparatorByName = Comparator.comparing(OlapEventTypeMetricsDto::getName, String.CASE_INSENSITIVE_ORDER);

            List<OlapEventTypeMetricsDto> sortedMetricsList = olapStatisticsDto.eventTypeStatistics().getMetricsList().stream()
                    .sorted(comparatorByIsConference.thenComparing(comparatorByOrganizerName).thenComparing(comparatorByName))
                    .toList();

            olapStatisticsDto.eventTypeStatistics().setMetricsList(sortedMetricsList);
        }

        if (olapStatisticsDto.speakerStatistics() != null) {
            Comparator<OlapSpeakerMetricsDto> comparatorByTotal = Comparator.comparing(OlapSpeakerMetricsDto::getTotal).reversed();
            Comparator<OlapSpeakerMetricsDto> comparatorByName = Comparator.comparing(OlapSpeakerMetricsDto::getName, String.CASE_INSENSITIVE_ORDER);

            List<OlapSpeakerMetricsDto> sortedMetricsList = olapStatisticsDto.speakerStatistics().getMetricsList().stream()
                    .sorted(comparatorByTotal.thenComparing(comparatorByName))
                    .toList();

            olapStatisticsDto.speakerStatistics().setMetricsList(sortedMetricsList);
        }

        if (olapStatisticsDto.companyStatistics() != null) {
            Comparator<OlapCompanyMetricsDto> comparatorByTotal = Comparator.comparing(OlapCompanyMetricsDto::getTotal).reversed();
            Comparator<OlapCompanyMetricsDto> comparatorByName = Comparator.comparing(OlapCompanyMetricsDto::getName, String.CASE_INSENSITIVE_ORDER);

            List<OlapCompanyMetricsDto> sortedMetricsList = olapStatisticsDto.companyStatistics().getMetricsList().stream()
                    .sorted(comparatorByTotal.thenComparing(comparatorByName))
                    .toList();

            olapStatisticsDto.companyStatistics().setMetricsList(sortedMetricsList);
        }

        return olapStatisticsDto;
    }

    @PostMapping("/olap-event-type-statistics")
    public OlapEntityStatisticsDto<Integer, OlapEventTypeMetricsDto> getOlapEventTypeStatistics(
            @RequestBody OlapEventTypeParametersDto olapParameters, @RequestParam String language) {
        var eventTypeStatistics = olapService.getOlapEventTypeStatistics(olapParameters);
        var olapEventTypeStatisticsDto = OlapEventTypeStatisticsDto.convertToDto(eventTypeStatistics, Language.getLanguageByCode(language));

        Comparator<OlapEventTypeMetricsDto> comparatorByIsConference = Comparator.comparing(OlapEventTypeMetricsDto::isConference).reversed();
        Comparator<OlapEventTypeMetricsDto> comparatorByOrganizerName = Comparator.comparing(OlapEventTypeMetricsDto::getOrganizerName, String.CASE_INSENSITIVE_ORDER);
        Comparator<OlapEventTypeMetricsDto> comparatorByName = Comparator.comparing(OlapEventTypeMetricsDto::getName, String.CASE_INSENSITIVE_ORDER);

        List<OlapEventTypeMetricsDto> sortedMetricsList = olapEventTypeStatisticsDto.getMetricsList().stream()
                .sorted(comparatorByIsConference.thenComparing(comparatorByOrganizerName).thenComparing(comparatorByName))
                .toList();
        olapEventTypeStatisticsDto.setMetricsList(sortedMetricsList);

        return olapEventTypeStatisticsDto;
    }

    @PostMapping("/olap-speaker-statistics")
    public OlapEntityStatisticsDto<Integer, OlapSpeakerMetricsDto> getOlapSpeakerStatistics(
            @RequestBody OlapSpeakerParametersDto olapParameters, @RequestParam String language) {
        var speakerStatistics = olapService.getOlapSpeakerStatistics(olapParameters);
        var olapSpeakerStatisticsDto = OlapSpeakerStatisticsDto.convertToDto(speakerStatistics, Language.getLanguageByCode(language));

        Comparator<OlapSpeakerMetricsDto> comparatorByTotal = Comparator.comparing(OlapSpeakerMetricsDto::getTotal).reversed();
        Comparator<OlapSpeakerMetricsDto> comparatorByName = Comparator.comparing(OlapSpeakerMetricsDto::getName, String.CASE_INSENSITIVE_ORDER);

        List<OlapSpeakerMetricsDto> sortedMetricsList = olapSpeakerStatisticsDto.getMetricsList().stream()
                .sorted(comparatorByTotal.thenComparing(comparatorByName))
                .toList();
        olapSpeakerStatisticsDto.setMetricsList(sortedMetricsList);

        return olapSpeakerStatisticsDto;
    }

    @PostMapping("/olap-city-statistics")
    public OlapEntityStatisticsDto<Integer, OlapCityMetricsDto> getOlapCityStatistics(
            @RequestBody OlapCityParametersDto olapParameters, @RequestParam String language) {
        var cityStatistics = olapService.getOlapCityStatistics(olapParameters);
        var olapCityStatisticsDto = OlapCityStatisticsDto.convertToDto(cityStatistics, Language.getLanguageByCode(language));

        List<OlapCityMetricsDto> sortedMetricsList = olapCityStatisticsDto.getMetricsList().stream()
                .sorted(Comparator.comparing(OlapCityMetricsDto::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        olapCityStatisticsDto.setMetricsList(sortedMetricsList);

        return olapCityStatisticsDto;
    }
}
