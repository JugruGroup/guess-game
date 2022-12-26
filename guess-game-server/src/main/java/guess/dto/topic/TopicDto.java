package guess.dto.topic;

import guess.domain.Language;
import guess.domain.source.Topic;
import guess.util.LocalizationUtils;

import java.util.List;

/**
 * Topic DTO.
 */
public class TopicDto {
    private final long id;
    private final String name;

    public TopicDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TopicDto convertToDto(Topic topic, Language language) {
        var name = LocalizationUtils.getString(topic.getName(), language);

        return new TopicDto(
                topic.getId(),
                name);
    }

    public static List<TopicDto> convertToDto(List<Topic> topics, Language language) {
        return topics.stream()
                .map(t -> convertToDto(t, language))
                .toList();
    }
}
