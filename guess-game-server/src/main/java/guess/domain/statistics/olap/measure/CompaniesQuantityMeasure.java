package guess.domain.statistics.olap.measure;

import guess.domain.source.Company;

import java.util.Set;

/**
 * Companies quantity measure.
 */
public class CompaniesQuantityMeasure extends Measure<Company> {
    public CompaniesQuantityMeasure(Set<Object> entities) {
        super(Company.class, entities);
    }

    @Override
    public long calculateValue() {
        return entities.size();
    }
}
