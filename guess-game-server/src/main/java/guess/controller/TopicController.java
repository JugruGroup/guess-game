package guess.controller;

import guess.domain.Language;
import guess.domain.source.Topic;
import guess.dto.topic.TopicDto;
import guess.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Topic controller.
 */
@RestController
@RequestMapping("/api/topic")
public class TopicController {
    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/topics")
    public List<TopicDto> getTopics(@RequestParam String language) {
        List<Topic> topics = topicService.getTopics();
        List<Topic> sortedTopics = topics.stream()
                .sorted(Comparator.comparing(Topic::getOrderNumber))
                .toList();

        return TopicDto.convertToDto(sortedTopics, Language.getLanguageByCode(language));
    }

    @GetMapping("/filter-topics")
    public List<TopicDto> getFilterTopics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                          @RequestParam(required = false) Long organizerId, @RequestParam String language) {
        List<Topic> topics = topicService.getTopics(conferences, meetups, organizerId, false);
        List<Topic> sortedTopics = topics.stream()
                .sorted(Comparator.comparing(Topic::getOrderNumber))
                .toList();

        return TopicDto.convertToDto(sortedTopics, Language.getLanguageByCode(language));
    }
}
