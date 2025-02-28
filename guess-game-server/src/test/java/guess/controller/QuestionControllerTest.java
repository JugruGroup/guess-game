package guess.controller;

import guess.domain.Conference;
import guess.domain.GuessMode;
import guess.domain.Language;
import guess.domain.source.*;
import guess.service.EventTypeService;
import guess.service.LocaleService;
import guess.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("QuestionController class tests")
@WebMvcTest(QuestionController.class)
class QuestionControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private EventTypeService eventTypeService;

    @MockitoBean
    private LocaleService localeService;

    @Test
    void getEventTypes() throws Exception {
        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

        EventType eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setConference(Conference.JPOINT);
        eventType0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        EventType eventType2 = new EventType();
        eventType2.setId(2);
        eventType2.setConference(Conference.JOKER);
        eventType0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name2")));

        given(eventTypeService.getEventTypes()).willReturn(List.of(eventType0, eventType1, eventType2));

        mvc.perform(get("/api/question/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[2].id", is(0)));
        Mockito.verify(eventTypeService, VerificationModeFactory.times(1)).getEventTypes();
    }

    @Test
    void getEvents() throws Exception {
        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        EventDays eventDays0 = new EventDays(
                LocalDate.of(2020, 10, 29),
                null,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        EventDays eventDays1 = new EventDays(
                LocalDate.of(2020, 10, 30),
                null,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        Event event0 = new Event();
        event0.setId(0);
        event0.setEventType(eventType0);
        event0.setDays(List.of(eventDays0));

        Event event1 = new Event();
        event1.setId(1);
        event1.setEventType(eventType0);
        event1.setDays(List.of(eventDays1));

        given(questionService.getEvents(List.of(0L, 1L))).willReturn(List.of(event0, event1));

        mvc.perform(get("/api/question/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventTypeIds", "0", "1")
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(0)));
        Mockito.verify(questionService, VerificationModeFactory.times(1)).getEvents(List.of(0L, 1L));
    }

    @Test
    void getQuantities() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        given(questionService.getQuantities(Mockito.anyList(), Mockito.anyList(), Mockito.any(GuessMode.class))).willReturn(List.of(5, 10, 15));

        mvc.perform(get("/api/question/quantities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventTypeIds", "0")
                        .param("eventIds", "0", "1")
                        .param("guessMode", "GUESS_NAME_BY_PHOTO_MODE")
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is(15)))
                .andExpect(jsonPath("$[1]", is(10)))
                .andExpect(jsonPath("$[2]", is(5)));
        Mockito.verify(questionService, VerificationModeFactory.times(1)).getQuantities(List.of(0L), List.of(0L, 1L), GuessMode.GUESS_NAME_BY_PHOTO_MODE);
    }
}
