package guess.util;

import guess.domain.source.image.ImageFormat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("ImageUtils class tests")
class ImageUtilsTest {
    private static final String JPG_IMAGE_400_X_400_PATH = createImagePath("400x400.jpg");
    private static final String PNG_IMAGE_400_X_400_PATH = createImagePath("400x400.png");
    private static final String JPG_IMAGE_1_X_1_PATH = createImagePath("1x1.jpg");
    private static final String JPG_IMAGE_400_X_1_PATH = createImagePath("400x1.jpg");
    private static final String INVALID_IMAGE_PATH = "invalid.jpg";

    private static String createImagePath(String resourceFileName) {
        ClassLoader classLoader = ImageUtilsTest.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(resourceFileName)).getFile());

        return file.getAbsolutePath();
    }

    private BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @BeforeAll
    static void setUp() throws IOException {
        FileUtils.deleteDirectory(ImageUtils.OUTPUT_DIRECTORY_NAME);
    }

    @AfterAll
    static void tearDown() throws IOException {
        FileUtils.deleteDirectory(ImageUtils.OUTPUT_DIRECTORY_NAME);
    }

    @Test
    void getImageByUrl() throws IOException {
        URL validUrl = Paths.get(JPG_IMAGE_400_X_400_PATH).toUri().toURL();
        URL invalidUrl = Paths.get(INVALID_IMAGE_PATH).toUri().toURL();

        assertNotNull(ImageUtils.getImageByUrl(validUrl));
        assertThrows(IOException.class, () -> ImageUtils.getImageByUrl(invalidUrl));
    }

    @Test
    void getImageByUrlString() throws IOException {
        final String validHttpUrlString = "https://valid.com";
        final String imageParametersTemplate = "w=%d&h=%d";
        final String imageParameters = String.format(imageParametersTemplate, ImageUtils.IMAGE_WIDTH, ImageUtils.IMAGE_HEIGHT);
        final URL validUrlWithParameters = URI.create(String.format("%s?%s", validHttpUrlString, imageParameters)).toURL();
        BufferedImage expected = createImage(1, 1);

        try (MockedStatic<ImageUtils> mockedStatic = Mockito.mockStatic(ImageUtils.class)) {
            mockedStatic.when(() -> ImageUtils.getImageByUrlString(Mockito.anyString(), Mockito.anyString()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> ImageUtils.getImageByUrl(Mockito.nullable(URL.class)))
                    .thenAnswer(
                            (Answer<BufferedImage>) invocation -> {
                                Object[] args = invocation.getArguments();
                                URL url = (URL) args[0];

                                if (url.equals(validUrlWithParameters)) {
                                    return expected;
                                } else {
                                    throw new IOException();
                                }
                            }
                    );

            assertEquals(expected, ImageUtils.getImageByUrlString(validHttpUrlString, imageParametersTemplate));
            assertThrows(IOException.class, () -> ImageUtils.getImageByUrlString("https://invalid.com",
                    imageParametersTemplate));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needUpdate method tests")
    class NeedUpdateTest {
        private Stream<Arguments> data() {
            final String image400X400UrlString = "https://valid.com/path0";
            final String image1X1UrlString = "https://valid.com/path1";
            final String imageParametersTemplate = "w=%d&h=%d";

            return Stream.of(
                    arguments(null, JPG_IMAGE_400_X_400_PATH, imageParametersTemplate, false),
                    arguments(image400X400UrlString, JPG_IMAGE_400_X_400_PATH, imageParametersTemplate, false),
                    arguments(image1X1UrlString, JPG_IMAGE_400_X_400_PATH, imageParametersTemplate, false),

                    arguments(image400X400UrlString, JPG_IMAGE_1_X_1_PATH, imageParametersTemplate, true),
                    arguments(image400X400UrlString, JPG_IMAGE_400_X_1_PATH, imageParametersTemplate, true),
                    arguments(image1X1UrlString, JPG_IMAGE_1_X_1_PATH, imageParametersTemplate, false),
                    arguments(image1X1UrlString, JPG_IMAGE_400_X_1_PATH, imageParametersTemplate, false)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needUpdate(String targetPhotoUrl, String resourceFileName, String imageParametersTemplate, boolean expected) throws IOException {
            final String image400X400UrlString = "https://valid.com/path0";
            final String image1X1UrlString = "https://valid.com/path1";

            try (MockedStatic<ImageUtils> mockedStatic = Mockito.mockStatic(ImageUtils.class)) {
                mockedStatic.when(() -> ImageUtils.needUpdate(Mockito.nullable(String.class), Mockito.anyString(), Mockito.anyString()))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ImageUtils.getImageByUrl(Mockito.any(URL.class)))
                        .thenAnswer(
                                (Answer<BufferedImage>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    URL url = (URL) args[0];

                                    return ImageIO.read(url);
                                }
                        );
                mockedStatic.when(() -> ImageUtils.getImageByUrlString(Mockito.nullable(String.class), Mockito.anyString()))
                        .thenAnswer(
                                (Answer<BufferedImage>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    String urlString = (String) args[0];

                                    return switch (urlString) {
                                        case image400X400UrlString -> createImage(400, 400);
                                        case image1X1UrlString -> createImage(1, 1);
                                        default -> throw new IOException();
                                    };
                                }
                        );

                assertEquals(expected, ImageUtils.needUpdate(targetPhotoUrl, resourceFileName, imageParametersTemplate));
            }
        }
    }

    @Test
    void getImageFormatByUrlString() {
        assertThrows(IllegalArgumentException.class, () -> ImageUtils.getImageFormatByUrlString(null));
        assertThrows(IllegalArgumentException.class, () -> ImageUtils.getImageFormatByUrlString(""));
        assertEquals(ImageFormat.JPG, ImageUtils.getImageFormatByUrlString("fileName"));
        assertEquals(ImageFormat.JPG, ImageUtils.getImageFormatByUrlString("fileName.jpg"));
        assertEquals(ImageFormat.PNG, ImageUtils.getImageFormatByUrlString("fileName.png"));
        assertEquals(ImageFormat.JPG, ImageUtils.getImageFormatByUrlString("url/fileName"));
        assertEquals(ImageFormat.JPG, ImageUtils.getImageFormatByUrlString("url/fileName.jpg"));
        assertEquals(ImageFormat.PNG, ImageUtils.getImageFormatByUrlString("url/fileName.png"));
        assertEquals(ImageFormat.JPG, ImageUtils.getImageFormatByUrlString("fileName.unknown"));
    }

    @Test
    void convertPngToJpg() throws IOException {
        BufferedImage pngImage = ImageIO.read(Paths.get(PNG_IMAGE_400_X_400_PATH).toUri().toURL());
        BufferedImage image = ImageUtils.convertPngToJpg(pngImage);

        assertNotNull(image);
        assertEquals(BufferedImage.TYPE_INT_RGB, image.getType());
    }

    @Test
    void create() {
        final String jpgImage400X400UrlString = "https://valid.com/fileName0.jpg";
        final String pngImage400X400UrlString = "https://valid.com/fileName1.png";
        final String jpgImage1X1UrlString = "https://valid.com/fileName3.jpg";
        final String fileName0 = "fileName0.jpg";
        final String fileName1 = "fileName1.png";
        final String fileName2 = "fileName2.jpg";
        final String fileName3 = "fileName3.jpg";
        final String imageParametersTemplate = "w=%d&h=%d";

        try (MockedStatic<ImageUtils> imageUtilsMockedStatic = Mockito.mockStatic(ImageUtils.class);
             MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class);
             MockedStatic<ImageIO> imageIOMockedStatic = Mockito.mockStatic(ImageIO.class)) {
            imageUtilsMockedStatic.when(() -> ImageUtils.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenCallRealMethod();
            imageUtilsMockedStatic.when(() -> ImageUtils.getImageByUrlString(Mockito.nullable(String.class), Mockito.anyString()))
                    .thenAnswer(
                            (Answer<BufferedImage>) invocation -> {
                                Object[] args = invocation.getArguments();
                                String urlString = (String) args[0];

                                return switch (urlString) {
                                    case jpgImage400X400UrlString ->
                                            ImageIO.read(Paths.get(JPG_IMAGE_400_X_400_PATH).toUri().toURL());
                                    case pngImage400X400UrlString ->
                                            ImageIO.read(Paths.get(PNG_IMAGE_400_X_400_PATH).toUri().toURL());
                                    case jpgImage1X1UrlString ->
                                            ImageIO.read(Paths.get(JPG_IMAGE_1_X_1_PATH).toUri().toURL());
                                    default -> null;
                                };
                            }
                    );
            imageUtilsMockedStatic.when(() -> ImageUtils.getImageFormatByUrlString(Mockito.nullable(String.class)))
                    .thenAnswer(
                            (Answer<ImageFormat>) invocation -> {
                                Object[] args = invocation.getArguments();
                                String sourceUrl = (String) args[0];

                                return switch (sourceUrl) {
                                    case jpgImage400X400UrlString -> ImageFormat.JPG;
                                    case pngImage400X400UrlString -> ImageFormat.PNG;
                                    default -> null;
                                };
                            }
                    );
            imageIOMockedStatic.when(() -> ImageIO.write(Mockito.nullable(RenderedImage.class), Mockito.nullable(String.class), Mockito.any(File.class)))
                    .thenAnswer(
                            (Answer<Boolean>) invocation -> {
                                Object[] args = invocation.getArguments();
                                File file = (File) args[2];

                                return !fileName2.equals(file.getName());
                            }
                    );

            assertDoesNotThrow(() -> ImageUtils.create(jpgImage400X400UrlString, fileName0, imageParametersTemplate));
            assertDoesNotThrow(() -> ImageUtils.create(pngImage400X400UrlString, fileName1, imageParametersTemplate));
            assertThrows(IOException.class, () -> ImageUtils.create(jpgImage400X400UrlString, fileName2, imageParametersTemplate));
            assertThrows(IllegalStateException.class, () -> ImageUtils.create(jpgImage1X1UrlString, fileName3, imageParametersTemplate));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fixImageType method tests")
    class FixImageTypeTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(new BufferedImage(1, 2, BufferedImage.TYPE_4BYTE_ABGR), new BufferedImage(1, 2, BufferedImage.TYPE_3BYTE_BGR)),
                    arguments(new BufferedImage(3, 4, BufferedImage.TYPE_INT_RGB), new BufferedImage(3, 4, BufferedImage.TYPE_INT_RGB))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fixImageType(BufferedImage image, BufferedImage expected) {
            BufferedImage actual = ImageUtils.fixImageType(image);

            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getWidth(), actual.getWidth());
            assertEquals(expected.getHeight(), actual.getHeight());
        }
    }
}
