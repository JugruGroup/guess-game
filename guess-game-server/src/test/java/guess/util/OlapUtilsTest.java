package guess.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("OlapUtils class tests")
class OlapUtilsTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getString method tests")
    class RemoveTrailingZeroesTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(0L, 0L, 0L), Collections.emptyList()),
                    arguments(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L)),
                    arguments(List.of(0L, 1L, 2L), List.of(0L, 1L, 2L)),
                    arguments(List.of(1L, 2L, 0L), List.of(1L, 2L)),
                    arguments(List.of(1L, 0L, 0L), List.of(1L)),
                    arguments(List.of(1L, 0L, 2L, 0L), List.of(1L, 0L, 2L))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getString(List<Long> source, List<Long> expected) {
            assertEquals(expected, OlapUtils.removeTrailingZeroes(source));
        }
    }
}
