package guess.controller;

import guess.domain.Language;
import guess.domain.source.*;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.service.LocaleService;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("EventTypeController class tests")
@WebMvcTest(EventTypeController.class)
class EventTypeControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventTypeService eventTypeService;

    @MockBean
    private EventService eventService;

    @MockBean
    private LocaleService localeService;

    @Autowired
    private EventTypeController eventTypeController;

    @Test
    void getEventTypes() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        EventType eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizer(organizer0);

        given(eventTypeService.getEventTypes(true, true, null, null)).willReturn(List.of(eventType0, eventType1));
        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);

        mvc.perform(get("/api/event-type/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", "true")
                        .param("meetups", "true")
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        Mockito.verify(eventTypeService, VerificationModeFactory.times(1)).getEventTypes(true, true, null, null);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }

    @Test
    void getFilterEventTypes() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        EventType eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizer(organizer0);

        given(eventTypeService.getEventTypes(true, true, null, null)).willReturn(List.of(eventType0, eventType1));
        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);

        mvc.perform(get("/api/event-type/filter-event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", "true")
                        .param("meetups", "true")
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        Mockito.verify(eventTypeService, VerificationModeFactory.times(1)).getEventTypes(true, true, null, null);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }

    @Test
    void getEventTypesAndSort() {
        final Long ORGANIZER_ID = 0L;
        final Long TOPIC_ID = 1L;

        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        eventType0.setOrganizer(organizer0);

        EventType eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
        eventType1.setOrganizer(organizer0);

        given(eventTypeService.getEventTypes(true, true, ORGANIZER_ID, TOPIC_ID)).willReturn(List.of(eventType1, eventType0));

        assertEquals(List.of(eventType0, eventType1), eventTypeController.getEventTypesAndSort(true, true, ORGANIZER_ID, TOPIC_ID, Language.ENGLISH));
        Mockito.verify(eventTypeService, VerificationModeFactory.times(1)).getEventTypes(true, true, ORGANIZER_ID, TOPIC_ID);
    }

    @Test
    void getEventType() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        Event event0 = new Event();
        event0.setId(0);

        Event event1 = new Event();
        event1.setId(1);

        Organizer organizer = new Organizer();
        organizer.setId(0);

        EventType eventType = new EventType();
        eventType.setId(0);
        eventType.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name")));
        eventType.setEvents(new ArrayList<>(List.of(event0, event1)));
        eventType.setOrganizer(organizer);

        EventDays eventDays0 = new EventDays(
                LocalDate.of(2020, 10, 29),
                LocalDate.of(2020, 10, 29),
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        EventDays eventDays1 = new EventDays(
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 10, 30),
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        event0.setEventType(eventType);
        event1.setEventType(eventType);

        event0.setDays(List.of(eventDays0));
        event1.setDays(List.of(eventDays1));

        EventPart eventPart0 = new EventPart(
                0,
                eventType,
                LocalDate.of(2020, 10, 29),
                LocalDate.of(2020, 10, 30)
        );

        EventPart eventPart1 = new EventPart(
                1,
                eventType,
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 10, 30)
        );

        given(eventTypeService.getEventTypeById(0)).willReturn(eventType);
        given(eventService.convertEventsToEventParts(Mockito.anyList())).willReturn(List.of(eventPart0, eventPart1));
        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);

        mvc.perform(get("/api/event-type/event-type/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType.id", is(0)))
                .andExpect(jsonPath("$.eventType.name", is("Name")))
                .andExpect(jsonPath("$.eventParts", hasSize(2)))
                .andExpect(jsonPath("$.eventParts[0].id", is(1)))
                .andExpect(jsonPath("$.eventParts[1].id", is(0)));
        Mockito.verify(eventTypeService, VerificationModeFactory.times(1)).getEventTypeById(0);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }
}
