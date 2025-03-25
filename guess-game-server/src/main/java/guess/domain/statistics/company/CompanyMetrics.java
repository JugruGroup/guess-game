package guess.domain.statistics.company;

import guess.domain.source.Company;

import java.util.Objects;

/**
 * Company metrics.
 */
public class CompanyMetrics extends AbstractCompanyMetrics {
    private final Company company;

    public CompanyMetrics(Company company, long speakersQuantity, long talksQuantity, long eventsQuantity,
                          long eventTypesQuantity, long javaChampionsQuantity, long mvpsQuantity) {
        super(talksQuantity, eventsQuantity, eventTypesQuantity, javaChampionsQuantity, mvpsQuantity, speakersQuantity);

        this.company = company;
    }

    public Company getCompany() {
        return company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyMetrics that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(company, that.company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), company);
    }

    @Override
    public String toString() {
        return "CompanyMetrics{" +
                "company=" + company +
                ", speakersQuantity=" + getSpeakersQuantity() +
                '}';
    }
}
