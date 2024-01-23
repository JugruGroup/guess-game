package guess.dto.statistics.olap.metrics;

import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.domain.statistics.olap.OlapEntityMetrics;
import guess.dto.statistics.speaker.SpeakerMetricsDegreesDto;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * OLAP speaker metrics DTO.
 */
public class OlapSpeakerMetricsDto extends OlapEntityMetricsDto {
    private final String photoFileName;
    private final SpeakerMetricsDegreesDto degrees;

    public OlapSpeakerMetricsDto(long id, String name, String photoFileName, SpeakerMetricsDegreesDto degrees,
                                 List<Long> measureValues, List<Long> cumulativeMeasureValues, Long total) {
        super(id, name, measureValues, cumulativeMeasureValues, total);

        this.photoFileName = photoFileName;
        this.degrees = degrees;
    }

    public boolean isJavaChampion() {
        return degrees.isJavaChampion();
    }

    public boolean isMvp() {
        return degrees.isMvp();
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public boolean isMvpReconnect() {
        return degrees.isMvpReconnect();
    }

    public boolean isAnyMvp() {
        return degrees.isAnyMvp();
    }

    public static OlapSpeakerMetricsDto convertToDto(OlapEntityMetrics<Speaker> speakerMetrics, Language language, Set<Speaker> speakerDuplicates) {
        var speaker = speakerMetrics.entity();
        String name = LocalizationUtils.getSpeakerNameWithLastNameFirst(speaker, language, speakerDuplicates);

        return new OlapSpeakerMetricsDto(
                speaker.getId(),
                name,
                speaker.getPhotoFileName(),
                new SpeakerMetricsDegreesDto(
                        speaker.isJavaChampion(),
                        speaker.isMvp(),
                        speaker.isMvpReconnect(),
                        speaker.isAnyMvp()
                ),
                speakerMetrics.measureValues(),
                speakerMetrics.cumulativeMeasureValues(),
                speakerMetrics.total());
    }

    public static List<OlapSpeakerMetricsDto> convertToDto(List<OlapEntityMetrics<Speaker>> speakerMetricsList, Language language) {
        List<Speaker> speakers = speakerMetricsList.stream()
                .map(OlapEntityMetrics::entity)
                .toList();
        Set<Speaker> speakerDuplicates = LocalizationUtils.getSpeakerDuplicates(
                speakers,
                s -> LocalizationUtils.getString(s.getName(), language),
                s -> true);

        return speakerMetricsList.stream()
                .map(sm -> convertToDto(sm, language, speakerDuplicates))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OlapSpeakerMetricsDto that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getPhotoFileName(), that.getPhotoFileName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPhotoFileName());
    }

    @Override
    public String toString() {
        return "OlapSpeakerMetricsDto{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", photoFileName='" + photoFileName + '\'' +
                '}';
    }
}
