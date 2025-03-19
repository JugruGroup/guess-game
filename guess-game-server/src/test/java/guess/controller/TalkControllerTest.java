package guess.controller;

import guess.domain.Language;
import guess.domain.source.*;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.service.TalkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TalkController class tests")
@WebMvcTest(TalkController.class)
class TalkControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TalkService talkService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventTypeService eventTypeService;

    @Test
    void getTalks() throws Exception {
        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        EventType eventType1 = new EventType();
        eventType1.setId(1);

        Event event0 = new Event();
        event0.setId(0);
        event0.setEventType(eventType0);

        Event event1 = new Event();
        event1.setId(1);
        event1.setEventType(eventType0);

        Talk talk0 = new Talk();
        talk0.setId(0);
        talk0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

        Talk talk1 = new Talk();
        talk1.setId(1);
        talk1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        eventType0.setEvents(List.of(event0));
        eventType1.setEvents(List.of(event1));

        EventDays eventDays0 = new EventDays(
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 10, 31),
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        event0.setTalks(List.of(talk0));
        event1.setTalks(List.of(talk1));

        event0.setDays(List.of(eventDays0));
        event1.setDays(List.of(eventDays0));

        given(talkService.getTalks(0L, 1L, "Talk", "Speaker", 2L, "EN"))
                .willReturn(List.of(talk1, talk0));
        given(eventService.getEventByTalk(talk0)).willReturn(event0);
        given(eventService.getEventByTalk(talk1)).willReturn(event1);
        given(eventTypeService.getEventTypeByEvent(event0)).willReturn(eventType0);
        given(eventTypeService.getEventTypeByEvent(event1)).willReturn(eventType1);

        mvc.perform(get("/api/talk/talks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventTypeId", "0")
                        .param("eventId", "1")
                        .param("talkName", "Talk")
                        .param("speakerName", "Speaker")
                        .param("topicId", "2")
                        .param("talkLanguage", "EN")
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(0)))
                .andExpect(jsonPath("$[1].id", is(1)));
        Mockito.verify(talkService, VerificationModeFactory.times(1)).getTalks(0L,
                1L, "Talk", "Speaker", 2L, "EN");
    }

    @Test
    void getTalk() throws Exception {
        Organizer organizer = new Organizer();
        organizer.setId(0);

        EventType eventType = new EventType();
        eventType.setId(0);
        eventType.setOrganizer(organizer);
        eventType.setTimeZone("Europe/Moscow");
        eventType.setTimeZoneId(ZoneId.of(eventType.getTimeZone()));

        EventDays eventDays = new EventDays(
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 10, 31),
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        Event event = new Event();
        event.setId(0);
        event.setEventType(eventType);
        event.setDays(List.of(eventDays));

        Talk talk = new Talk();
        talk.setId(0);
        talk.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name")));
        talk.setTalkDay(1L);
        talk.setStartTime(LocalTime.of(10, 0));
        talk.setEndTime(LocalTime.of(10, 45));

        given(talkService.getTalkById(0)).willReturn(talk);
        given(eventService.getEventByTalk(talk)).willReturn(event);

        mvc.perform(get("/api/talk/talk/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.talk.id", is(0)))
                .andExpect(jsonPath("$.talk.name", is("Name")));
        Mockito.verify(talkService, VerificationModeFactory.times(1)).getTalkById(0);
    }
}
