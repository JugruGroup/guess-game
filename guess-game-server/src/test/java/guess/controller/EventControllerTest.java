package guess.controller;

import guess.domain.Language;
import guess.domain.source.*;
import guess.service.EventService;
import guess.service.EventTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("EventController class tests")
@WebMvcTest(EventController.class)
class EventControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventTypeService eventTypeService;

    @Autowired
    private EventController eventController;

    @Test
    void getEvents() throws Exception {
        boolean conferences = true;
        boolean meetups = true;
        Long organizerId = null;
        Long eventTypeId = 0L;

        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        Place place0 = new Place();
        place0.setId(0);
        place0.setCity(Collections.emptyList());
        place0.setVenueAddress(Collections.emptyList());

        Event event0 = new Event();
        event0.setId(0);
        event0.setDays(List.of(new EventDays(
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 10, 30),
                place0
        )));
        event0.setEventType(eventType0);

        Event event1 = new Event();
        event1.setId(1);
        event1.setDays(List.of(new EventDays(
                LocalDate.of(2020, 10, 29),
                LocalDate.of(2020, 10, 29),
                place0
        )));
        event1.setEventType(eventType0);

        Event event2 = new Event();
        event2.setId(2);
        event2.setDays(List.of(new EventDays(
                LocalDate.of(2020, 10, 31),
                LocalDate.of(2020, 10, 31),
                place0
        )));
        event2.setEventType(eventType0);

        given(eventService.getEvents(conferences, meetups, organizerId, eventTypeId)).willReturn(List.of(event0, event1, event2));

        mvc.perform(get("/api/event/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", Boolean.toString(conferences))
                        .param("meetups", Boolean.toString(meetups))
                        .param("eventTypeId", "0")
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[1].id", is(0)))
                .andExpect(jsonPath("$[2].id", is(1)));
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getEvents(conferences, meetups, organizerId, eventTypeId);
    }

    @Test
    void getEventParts() throws Exception {
        boolean conferences = true;
        boolean meetups = true;
        Long organizerId = null;
        Long eventTypeId = 0L;

        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        Place place0 = new Place();
        place0.setId(0);
        place0.setCity(Collections.emptyList());
        place0.setVenueAddress(Collections.emptyList());

        Event event0 = new Event();
        event0.setId(0);
        event0.setDays(List.of(new EventDays(
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 11, 1),
                place0
        )));
        event0.setEventType(eventType0);

        Event event1 = new Event();
        event1.setId(1);
        event1.setDays(List.of(new EventDays(
                LocalDate.of(2020, 10, 29),
                LocalDate.of(2020, 10, 29),
                place0
        )));
        event1.setEventType(eventType0);

        Event event2 = new Event();
        event2.setId(2);
        event2.setDays(List.of(new EventDays(
                LocalDate.of(2020, 10, 31),
                LocalDate.of(2020, 10, 31),
                place0
        )));
        event2.setEventType(eventType0);

        EventPart eventPart0 = new EventPart(
                0,
                eventType0,
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 11, 1)
        );

        EventPart eventPart1 = new EventPart(
                1,
                eventType0,
                LocalDate.of(2020, 10, 29),
                LocalDate.of(2020, 10, 29)
        );

        EventPart eventPart2 = new EventPart(
                2,
                eventType0,
                LocalDate.of(2020, 10, 31),
                LocalDate.of(2020, 10, 31)
        );

        given(eventService.getEvents(conferences, meetups, organizerId, eventTypeId)).willReturn(List.of(event0, event1, event2));
        given(eventService.convertEventsToEventParts(List.of(event0, event1, event2))).willReturn(List.of(eventPart0, eventPart1, eventPart2));

        mvc.perform(get("/api/event/event-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", Boolean.toString(conferences))
                        .param("meetups", Boolean.toString(meetups))
                        .param("eventTypeId", "0")
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[1].id", is(0)))
                .andExpect(jsonPath("$[2].id", is(1)));
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getEvents(conferences, meetups, organizerId, eventTypeId);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).convertEventsToEventParts(List.of(event0, event1, event2));
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getDefaultEvent method tests")
    class GetDefaultEventTest {
        private Stream<Arguments> data() {
            Organizer organizer0 = new Organizer();
            organizer0.setId(0);

            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setOrganizer(organizer0);

            EventDays eventDays0 = new EventDays(
                    null,
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

            return Stream.of(
                    arguments(new Object[]{null}),
                    arguments(event0)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getDefaultConference(Event defaultEvent) throws Exception {
            final boolean IS_CONFERENCES = Boolean.TRUE;
            final boolean IS_MEETUPS = Boolean.TRUE;

            given(eventService.getDefaultEvent(IS_CONFERENCES, IS_MEETUPS)).willReturn(defaultEvent);

            mvc.perform(get("/api/event/default-event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("language", "en"))
                    .andExpect(status().isOk());
            Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEvent(IS_CONFERENCES, IS_MEETUPS);
            Mockito.reset(eventService);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getDefaultEventPartHomeInfo method tests")
    class GetDefaultEventPartHomeInfoTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(new Object[]{null}),
                    arguments(new EventPart())
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getDefaultEventPartHomeInfo(EventPart defaultEventPart) throws Exception {
            final boolean IS_CONFERENCES = Boolean.TRUE;
            final boolean IS_MEETUPS = Boolean.TRUE;

            given(eventService.getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS)).willReturn(defaultEventPart);

            mvc.perform(get("/api/event/default-event-part-home-info")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("language", "en"))
                    .andExpect(status().isOk());
            Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS);
            Mockito.reset(eventService);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getDefaultEventHomeInfo method tests")
    class GetDefaultEventHomeInfoTest {
        private Stream<Arguments> data() {
            Organizer organizer0 = new Organizer();
            organizer0.setId(0);

            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setOrganizer(organizer0);

            Event event0 = new Event();
            event0.setId(0);
            event0.setEventType(eventType0);

            return Stream.of(
                    arguments(new Object[]{null}),
                    arguments(event0)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getDefaultConference(Event defaultEvent) throws Exception {
            final boolean IS_CONFERENCES = Boolean.TRUE;
            final boolean IS_MEETUPS = Boolean.TRUE;

            given(eventService.getDefaultEvent(IS_CONFERENCES, IS_MEETUPS)).willReturn(defaultEvent);

            mvc.perform(get("/api/event/default-event-part-home-info")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("language", "en"))
                    .andExpect(status().isOk());
            Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS);
            Mockito.reset(eventService);
        }
    }

    @Test
    void getEvent() throws Exception {
        Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company0")));
        Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company1")));

        EventType eventType = new EventType();
        eventType.setId(0);

        Speaker speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        speaker0.setCompanies(List.of(company0));

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
        speaker1.setCompanies(List.of(company1));

        Speaker speaker2 = new Speaker();
        speaker2.setId(2);
        speaker2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        speaker2.setCompanies(List.of(company1));

        Talk talk0 = new Talk();
        talk0.setId(0);
        talk0.setTalkDay(2L);
        talk0.setSpeakers(List.of(speaker1));

        Talk talk1 = new Talk();
        talk1.setId(1);
        talk1.setTalkDay(1L);
        talk1.setSpeakers(List.of(speaker0, speaker2));

        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer0);

        EventDays eventDays0 = new EventDays(
                LocalDate.of(2020, 10, 30),
                LocalDate.of(2020, 10, 30),
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        );

        Event event0 = new Event();
        event0.setId(0);
        event0.setTalks(List.of(talk0, talk1));
        event0.setEventType(eventType0);
        event0.setDays(List.of(eventDays0));

        given(eventService.getEventById(0)).willReturn(event0);
        given(eventService.getEventByTalk(talk0)).willReturn(event0);
        given(eventService.getEventByTalk(talk1)).willReturn(event0);
        given(eventTypeService.getEventTypeByEvent(event0)).willReturn(eventType);

        mvc.perform(get("/api/event/event/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.id", is(0)))
                .andExpect(jsonPath("$.speakers", hasSize(3)))
                .andExpect(jsonPath("$.speakers[0].id", is(0)))
                .andExpect(jsonPath("$.speakers[1].id", is(2)))
                .andExpect(jsonPath("$.speakers[2].id", is(1)))
                .andExpect(jsonPath("$.talks", hasSize(2)))
                .andExpect(jsonPath("$.talks[0].id", is(1)))
                .andExpect(jsonPath("$.talks[1].id", is(0)));
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getEventById(0);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getEventByTalk(talk0);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getEventByTalk(talk1);
        Mockito.verify(eventTypeService, VerificationModeFactory.times(3)).getEventTypeByEvent(event0);
    }
}
