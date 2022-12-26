package guess.controller;

import guess.domain.source.Topic;
import guess.dto.topic.TopicDto;
import guess.service.LocaleService;
import guess.service.TopicService;
import jakarta.servlet.http.HttpSession;
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
    private final LocaleService localeService;

    @Autowired
    public TopicController(TopicService topicService, LocaleService localeService) {
        this.topicService = topicService;
        this.localeService = localeService;
    }

    @GetMapping("/filter-topics")
    List<TopicDto> getFilterTopics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                   @RequestParam(required = false) Long organizerId, HttpSession httpSession) {
        var language = localeService.getLanguage(httpSession);
        List<Topic> topics = topicService.getTopics(conferences, meetups, organizerId, false);
        List<Topic> sortedTopics = topics.stream()
                .sorted(Comparator.comparing(Topic::getOrderNumber))
                .toList();

        return TopicDto.convertToDto(sortedTopics, language);
    }
}
