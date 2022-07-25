package guess.domain.source.cms.jrgcms.event;

import guess.domain.source.cms.jrgcms.JrgCmsObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("JrgCmsEventComparator class tests")
class JrgCmsEventComparatorTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("compare method tests")
    class CompareTest {
        private JrgCmsEvent createJrgCmsEvent(String eventVersion) {
            JrgCmsObject<String> jrgCmsObject = new JrgCmsObject<>();
            jrgCmsObject.setIv(eventVersion);

            JrgCmsEvent jrgCmsEvent = new JrgCmsEvent();
            jrgCmsEvent.setEventVersion(jrgCmsObject);

            return jrgCmsEvent;
        }

        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(createJrgCmsEvent("2021"), createJrgCmsEvent("2022"), null, -1),
                    arguments(createJrgCmsEvent("2022"), createJrgCmsEvent("2021"), null, 1),
                    arguments(createJrgCmsEvent("2022"), createJrgCmsEvent("2022"), null, 0),
                    arguments(createJrgCmsEvent("2021 Spring"), createJrgCmsEvent("2022 Spring"), null, -1),
                    arguments(createJrgCmsEvent("2022 Autumn"), createJrgCmsEvent("2021 Autumn"), null, 1),
                    arguments(createJrgCmsEvent("2022 Spring"), createJrgCmsEvent("2022 Autumn"), null, -1),
                    arguments(createJrgCmsEvent("2022 Autumn"), createJrgCmsEvent("2022 Spring"), null, 1),
                    arguments(createJrgCmsEvent("2022 Spring"), createJrgCmsEvent("2022 Spring"), null, 0),
                    arguments(createJrgCmsEvent("2022 Autumn"), createJrgCmsEvent("2022 Autumn"), null, 0),
                    arguments(createJrgCmsEvent("Autumn"), createJrgCmsEvent("2022 Autumn"), IllegalArgumentException.class, 0)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void compare(JrgCmsEvent event1, JrgCmsEvent event2, Class<? extends Throwable> expectedException, int expectedValue) {
            JrgCmsEventComparator comparator = new JrgCmsEventComparator();
            
            if (expectedException == null) {
                assertEquals(expectedValue, comparator.compare(event1, event2));
            } else {
                assertThrows(expectedException, () -> comparator.compare(event1, event2));
            }
        }
    }
}
