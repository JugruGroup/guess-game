package guess.domain.source.load;

import guess.domain.source.Event;
import guess.domain.source.NameCompany;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Load settings.
 */
public record LoadSettings(Map<NameCompany, Long> knownSpeakerIdsMap, Set<String> invalidTalksSet,
                           boolean ignoreDemoStage, Event eventTemplate) {
    public static LoadSettings defaultSettings() {
        return new LoadSettings(
                Collections.emptyMap(),
                Collections.emptySet(),
                true,
                null);
    }

    public static LoadSettings knownSpeakerIdsMap(Map<NameCompany, Long> knownSpeakerIdsMap) {
        return new LoadSettings(
                knownSpeakerIdsMap,
                Collections.emptySet(),
                true,
                null);
    }

    public static LoadSettings invalidTalksSet(Set<String> invalidTalksSet) {
        return new LoadSettings(
                Collections.emptyMap(),
                invalidTalksSet,
                true,
                null);
    }

    public static LoadSettings ignoreDemoStage(boolean ignoreDemoStage) {
        return new LoadSettings(
                Collections.emptyMap(),
                Collections.emptySet(),
                ignoreDemoStage,
                null);
    }

    public static LoadSettings eventTemplate(Event eventTemplate) {
        return new LoadSettings(
                Collections.emptyMap(),
                Collections.emptySet(),
                true,
                eventTemplate);
    }

    public static LoadSettings eventTemplateAndInvalidTalksSet(Event eventTemplate, Set<String> invalidTalksSet) {
        return new LoadSettings(
                Collections.emptyMap(),
                invalidTalksSet,
                true,
                eventTemplate);
    }

    public static LoadSettings eventTemplateAndKnownSpeakerIdsMapAndInvalidTalksSet(Event eventTemplate,
                                                                                    Map<NameCompany, Long> knownSpeakerIdsMap,
                                                                                    Set<String> invalidTalksSet) {
        return new LoadSettings(
                knownSpeakerIdsMap,
                invalidTalksSet,
                true,
                eventTemplate);
    }
}
