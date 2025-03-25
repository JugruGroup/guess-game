package guess.domain.statistics.company;

import guess.domain.statistics.AbstractSpeakerCompanyMetrics;

import java.util.Objects;

/**
 * Abstract company metrics.
 */
public class AbstractCompanyMetrics extends AbstractSpeakerCompanyMetrics {
    private final long speakersQuantity;

    public AbstractCompanyMetrics(long talksQuantity, long eventsQuantity, long eventTypesQuantity,
                                  long javaChampionsQuantity, long mvpsQuantity, long speakersQuantity) {
        super(talksQuantity, eventsQuantity, eventTypesQuantity, javaChampionsQuantity, mvpsQuantity);

        this.speakersQuantity = speakersQuantity;
    }

    public long getSpeakersQuantity() {
        return speakersQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCompanyMetrics that)) return false;
        if (!super.equals(o)) return false;
        return getSpeakersQuantity() == that.getSpeakersQuantity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSpeakersQuantity());
    }
}
