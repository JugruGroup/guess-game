package guess.service;

import guess.domain.source.City;
import guess.domain.source.EventType;
import guess.domain.source.Speaker;
import guess.domain.statistics.olap.CubeType;
import guess.domain.statistics.olap.MeasureType;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.statistics.olap.parameters.OlapCityParametersDto;
import guess.dto.statistics.olap.parameters.OlapEventTypeParametersDto;
import guess.dto.statistics.olap.parameters.OlapParametersDto;
import guess.dto.statistics.olap.parameters.OlapSpeakerParametersDto;

import java.util.List;

/**
 * OLAP service.
 */
public interface OlapService {
    List<MeasureType> getMeasureTypes(CubeType cubeType);

    OlapStatistics getOlapStatistics(OlapParametersDto olapParameters);

    OlapEntityStatistics<Integer, Void, EventType> getOlapEventTypeStatistics(OlapEventTypeParametersDto olapParameters);

    OlapEntityStatistics<Integer, Void, Speaker> getOlapSpeakerStatistics(OlapSpeakerParametersDto olapParameters);

    OlapEntityStatistics<Integer, Void, City> getOlapCityStatistics(OlapCityParametersDto olapParameters);
}
