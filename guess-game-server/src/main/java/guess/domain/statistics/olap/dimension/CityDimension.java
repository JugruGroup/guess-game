package guess.domain.statistics.olap.dimension;

import guess.domain.source.City;

/**
 * City dimension.
 */
public class CityDimension extends Dimension<City> {
    public CityDimension(Object value) {
        super(City.class, value);
    }
}
