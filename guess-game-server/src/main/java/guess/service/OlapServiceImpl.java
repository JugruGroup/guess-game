package guess.service;

import guess.dao.OlapDao;
import guess.domain.source.*;
import guess.domain.statistics.olap.*;
import guess.domain.statistics.olap.metrics.OlapEntityMetrics;
import guess.domain.statistics.olap.metrics.OlapEntitySubMetrics;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.statistics.olap.parameters.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * OLAP service implementation.
 */
@Service
public class OlapServiceImpl implements OlapService {
    private final OlapDao olapDao;

    @Autowired
    public OlapServiceImpl(OlapDao olapDao) {
        this.olapDao = olapDao;
    }

    @Override
    public List<MeasureType> getMeasureTypes(CubeType cubeType) {
        return olapDao.getMeasureTypes(cubeType);
    }

    private Predicate<EventType> createEventTypePredicate(OlapCommonParametersDto op) {
        return et -> ((op.isConferences() && et.isEventTypeConference()) || (op.isMeetups() && !et.isEventTypeConference())) &&
                ((op.getOrganizerId() == null) || (et.getOrganizer().getId() == op.getOrganizerId())) &&
                ((op.getEventTypeIds() == null) || op.getEventTypeIds().isEmpty() || op.getEventTypeIds().contains(et.getId()));
    }

    @Override
    public OlapStatistics getOlapStatistics(OlapParametersDto op) {
        Predicate<EventType> eventTypePredicate = createEventTypePredicate(op);
        Predicate<Speaker> speakerPredicate = s -> (op.getSpeakerIds() == null) || op.getSpeakerIds().isEmpty() || op.getSpeakerIds().contains(s.getId());
        Predicate<Company> companyPredicate = c -> (op.getCompanyIds() == null) || op.getCompanyIds().isEmpty() || op.getCompanyIds().contains(c.getId());
        OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics = null;
        OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics = null;
        OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics = null;
        OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics = null;
        OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics = null;
        OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics = null;

        if (CubeType.EVENT_TYPES.equals(op.getCubeType())) {
            yearEventTypeStatistics = getOlapEntityStatistics(op, DimensionType.EVENT_TYPE, eventTypePredicate,
                    DimensionType.YEAR, DimensionType.CITY, DimensionType.EVENT_TYPE, eventTypePredicate);
            topicEventTypeStatistics = getOlapEntityStatistics(op, DimensionType.EVENT_TYPE, eventTypePredicate,
                    DimensionType.TOPIC, null, DimensionType.EVENT_TYPE, eventTypePredicate);
        }

        if (CubeType.SPEAKERS.equals(op.getCubeType())) {
            yearSpeakerStatistics = getOlapEntityStatistics(op, DimensionType.SPEAKER, speakerPredicate,
                    DimensionType.YEAR, DimensionType.EVENT_TYPE, DimensionType.EVENT_TYPE, eventTypePredicate);
            topicSpeakerStatistics = getOlapEntityStatistics(op, DimensionType.SPEAKER, speakerPredicate,
                    DimensionType.TOPIC, null, DimensionType.EVENT_TYPE, eventTypePredicate);
        }

        if (CubeType.COMPANIES.equals(op.getCubeType())) {
            yearCompanyStatistics = getOlapEntityStatistics(op, DimensionType.COMPANY, companyPredicate,
                    DimensionType.YEAR, DimensionType.EVENT_TYPE, DimensionType.EVENT_TYPE, eventTypePredicate);
            topicCompanyStatistics = getOlapEntityStatistics(op, DimensionType.COMPANY, companyPredicate,
                    DimensionType.TOPIC, null, DimensionType.EVENT_TYPE, eventTypePredicate);
        }

        return new OlapStatistics(yearEventTypeStatistics, yearSpeakerStatistics, yearCompanyStatistics,
                topicEventTypeStatistics, topicSpeakerStatistics, topicCompanyStatistics);
    }

    @Override
    public OlapEntityStatistics<Integer, Void, EventType> getOlapEventTypeStatistics(OlapEventTypeParametersDto op) {
        Predicate<EventType> eventTypePredicate = createEventTypePredicate(op);
        OlapEntityStatistics<Integer, Void, EventType> olapEventTypeStatistics;

        switch (op.getCubeType()) {
            case SPEAKERS -> {
                Predicate<Speaker> speakerPredicate = s -> (op.getSpeakerId() != null) && (s.getId() == op.getSpeakerId());
                olapEventTypeStatistics = getOlapEntityStatistics(op, DimensionType.EVENT_TYPE, eventTypePredicate,
                        DimensionType.YEAR, null, DimensionType.SPEAKER, speakerPredicate);
            }
            case COMPANIES -> {
                Predicate<Company> companyPredicate = c -> (op.getCompanyId() != null) && (c.getId() == op.getCompanyId());
                olapEventTypeStatistics = getOlapEntityStatistics(op, DimensionType.EVENT_TYPE, eventTypePredicate,
                        DimensionType.YEAR, null, DimensionType.COMPANY, companyPredicate);
            }
            default -> throw new IllegalArgumentException(String.format("Invalid cube type %s", op.getCubeType()));
        }

        return olapEventTypeStatistics;
    }

    @Override
    public OlapEntityStatistics<Integer, Void, Speaker> getOlapSpeakerStatistics(OlapSpeakerParametersDto op) {
        Predicate<Speaker> speakerPredicate = s -> (op.getCompanyId() != null) && (s.getCompanyIds().contains(op.getCompanyId()));
        Predicate<EventType> eventTypePredicate = et -> (op.getEventTypeId() != null) && (et.getId() == op.getEventTypeId());

        return getOlapEntityStatistics(op, DimensionType.SPEAKER, speakerPredicate, DimensionType.YEAR,
                null, DimensionType.EVENT_TYPE, eventTypePredicate);
    }

    @Override
    public OlapEntityStatistics<Integer, Void, City> getOlapCityStatistics(OlapCityParametersDto op) {
        Predicate<City> cityPredicate = c -> true;
        Predicate<EventType> eventTypePredicate = et -> (op.getEventTypeId() != null) && (et.getId() == op.getEventTypeId());

        return getOlapEntityStatistics(op, DimensionType.CITY, cityPredicate, DimensionType.YEAR,
                null, DimensionType.EVENT_TYPE, eventTypePredicate);
    }

    @SuppressWarnings("unchecked")
    <T, S, U, V> OlapEntityStatistics<S, U, T> getOlapEntityStatistics(OlapBasicParametersDto op,
                                                                       DimensionType dimensionType1,
                                                                       Predicate<T> dimensionPredicate1,
                                                                       DimensionType dimensionType2,
                                                                       DimensionType dimensionType3,
                                                                       DimensionType filterDimensionType,
                                                                       Predicate<V> filterDimensionPredicate) {
        Cube cube = olapDao.getCube(op.getCubeType());
        List<T> dimensionValues1 = cube.getDimensionValues(dimensionType1).stream()
                .map(v -> (T) v)
                .filter(dimensionPredicate1)
                .toList();
        List<S> dimensionValues2 = cube.getDimensionValues(dimensionType2).stream()
                .map(v -> (S) v)
                .sorted()
                .toList();
        List<U> dimensionValues3 = (dimensionType3 != null) ? cube.getDimensionValues(dimensionType3).stream()
                .map(v -> (U) v)
                .toList() :
                Collections.emptyList();
        List<V> filterDimensionValues = cube.getDimensionValues(filterDimensionType).stream()
                .map(v -> (V) v)
                .filter(filterDimensionPredicate)
                .toList();

        return cube.getMeasureValueEntities(
                new DimensionTypeValues<>(dimensionType1, dimensionValues1),
                new DimensionTypeValues<>(dimensionType2, dimensionValues2),
                new DimensionTypeValues<>(dimensionType3, dimensionValues3),
                new DimensionTypeValues<>(filterDimensionType, filterDimensionValues),
                op.getMeasureType(),
                new ResultFunctions<>(
                        OlapEntitySubMetrics::new,
                        OlapEntityMetrics::new,
                        (measureValues, cumulativeMeasureValues, total) ->
                                new OlapEntityMetrics<Void>(null, measureValues, cumulativeMeasureValues, total),
                        OlapEntityStatistics::new
                ));
    }
}
