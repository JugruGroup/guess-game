package guess.dto.statistics.speaker;

import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.domain.statistics.AbstractSpeakerCompanyMetrics;
import guess.domain.statistics.speaker.SpeakerMetrics;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Speaker metrics DTO.
 */
public class SpeakerMetricsDto extends AbstractSpeakerCompanyMetrics {
    private final long id;
    private final String name;
    private final String photoFileName;
    private final SpeakerMetricsDegreesDto degrees;

    public SpeakerMetricsDto(long id, String name, String photoFileName, SpeakerMetricsDegreesDto degrees, AbstractSpeakerCompanyMetrics speakerMetrics) {
        super(speakerMetrics.getTalksQuantity(), speakerMetrics.getEventsQuantity(), speakerMetrics.getEventTypesQuantity(),
                speakerMetrics.getJavaChampionsQuantity(), speakerMetrics.getMvpsQuantity());

        this.id = id;
        this.name = name;
        this.photoFileName = photoFileName;
        this.degrees = degrees;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public boolean isJavaChampion() {
        return degrees.isJavaChampion();
    }

    public boolean isMvp() {
        return degrees.isMvp();
    }

    public boolean isMvpReconnect() {
        return degrees.isMvpReconnect();
    }

    public boolean isAnyMvp() {
        return degrees.isAnyMvp();
    }

    public static SpeakerMetricsDto convertToDto(SpeakerMetrics speakerMetrics, Language language, Set<Speaker> speakerDuplicates) {
        var speaker = speakerMetrics.getSpeaker();
        String name = LocalizationUtils.getSpeakerNameWithLastNameFirst(speaker, language, speakerDuplicates);

        return new SpeakerMetricsDto(
                speaker.getId(),
                name,
                speaker.getPhotoFileName(),
                new SpeakerMetricsDegreesDto(
                        speaker.isJavaChampion(),
                        speaker.isMvp(),
                        speaker.isMvpReconnect(),
                        speaker.isAnyMvp()
                ),
                speakerMetrics);
    }

    public static List<SpeakerMetricsDto> convertToDto(List<SpeakerMetrics> speakerMetricsList, Language language) {
        List<Speaker> speakers = speakerMetricsList.stream()
                .map(SpeakerMetrics::getSpeaker)
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
        if (!(o instanceof SpeakerMetricsDto that)) return false;
        if (!super.equals(o)) return false;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(photoFileName, that.photoFileName) &&
                Objects.equals(degrees, that.degrees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, photoFileName, degrees);
    }
}
