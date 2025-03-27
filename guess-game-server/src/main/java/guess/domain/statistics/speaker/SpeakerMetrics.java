package guess.domain.statistics.speaker;

import guess.domain.source.Speaker;
import guess.domain.statistics.AbstractSpeakerCompanyMetrics;

import java.util.Objects;

/**
 * Speaker metrics.
 */
public class SpeakerMetrics extends AbstractSpeakerCompanyMetrics {
    private final Speaker speaker;

    public SpeakerMetrics(Speaker speaker, long talksQuantity, long eventsQuantity, long eventTypesQuantity,
                          long javaChampionsQuantity, long mvpsQuantity) {
        super(talksQuantity, eventsQuantity, eventTypesQuantity, javaChampionsQuantity, mvpsQuantity);

        this.speaker = speaker;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpeakerMetrics that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(speaker, that.speaker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), speaker);
    }

    @Override
    public String toString() {
        return "SpeakerMetrics{" +
                "speaker=" + speaker +
                '}';
    }
}
