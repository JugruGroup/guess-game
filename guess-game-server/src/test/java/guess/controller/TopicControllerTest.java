package guess.controller;

import guess.domain.Language;
import guess.domain.source.Topic;
import guess.service.LocaleService;
import guess.service.TopicService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TopicController class tests")
@WebMvcTest(TopicController.class)
class TopicControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private TopicService topicService;

    @MockBean
    private LocaleService localeService;

    @Test
    void getFilterTopics() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        Topic topic0 = new Topic();
        topic0.setId(0);

        Topic topic1 = new Topic();
        topic1.setId(1);

        given(topicService.getTopics(true, true, null, false)).willReturn(List.of(topic0, topic1));
        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);

        mvc.perform(get("/api/topic/filter-topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", "true")
                        .param("meetups", "true")
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        Mockito.verify(topicService, VerificationModeFactory.times(1)).getTopics(true, true, null, false);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }
}
