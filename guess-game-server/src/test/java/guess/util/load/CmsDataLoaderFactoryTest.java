package guess.util.load;

import guess.domain.source.load.CmsType;
import guess.util.load.contentful.ContentfulDataLoader;
import guess.util.load.jrgcms.JrgCmsDataLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("CmsDataLoaderFactory class tests")
class CmsDataLoaderFactoryTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createDataLoader method tests (enum)")
    class CreateDataLoaderEnumTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(CmsType.CONTENTFUL, null, ContentfulDataLoader.class),
                    arguments(CmsType.JUGRUGROUP_CMS, null, JrgCmsDataLoader.class),
                    arguments(null, IllegalArgumentException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createDataLoader(CmsType cmsType, Class<? extends Throwable> expectedException, Class<? extends CmsDataLoader> expectedValueClass) {
            if (expectedException == null) {
                assertEquals(expectedValueClass, CmsDataLoaderFactory.createDataLoader(cmsType).getClass());
            } else {
                assertThrows(expectedException, () -> CmsDataLoaderFactory.createDataLoader(cmsType));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createDataLoader method tests (date)")
    class CreateDataLoaderDateTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(LocalDate.of(2021, 1, 1), null, ContentfulDataLoader.class),
                    arguments(LocalDate.of(2022, 1, 1), null, JrgCmsDataLoader.class),
                    arguments(LocalDate.of(2023, 1, 1), null, JrgCmsDataLoader.class),
                    arguments(null, NullPointerException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createDataLoader(LocalDate startDate, Class<? extends Throwable> expectedException, Class<? extends CmsDataLoader> expectedValueClass) {
            if (expectedException == null) {
                assertEquals(expectedValueClass, CmsDataLoaderFactory.createDataLoader(startDate).getClass());
            } else {
                assertThrows(expectedException, () -> CmsDataLoaderFactory.createDataLoader(startDate));
            }
        }
    }
}
