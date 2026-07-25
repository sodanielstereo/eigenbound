package com.eigenbound.presentation.canvas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CartesianPlaneMapperTest {

    private static final double EPSILON = 1e-9;

    @Test
    void shouldPlaceOriginAtCenterOfPlane() {
        CartesianPlaneMapper mapper = new CartesianPlaneMapper(800, 600, 40);

        assertEquals(400, mapper.toPixelX(0), EPSILON);
        assertEquals(300, mapper.toPixelY(0), EPSILON);
    }

    @Test
    void shouldConvertPositiveCoordinatesToPixels() {
        CartesianPlaneMapper mapper = new CartesianPlaneMapper(800, 600, 40);

        assertEquals(480, mapper.toPixelX(2), EPSILON);
        assertEquals(260, mapper.toPixelY(1), EPSILON);
    }

    @Test
    void shouldConvertNegativeCoordinatesToPixels() {
        CartesianPlaneMapper mapper = new CartesianPlaneMapper(800, 600, 40);

        assertEquals(320, mapper.toPixelX(-2), EPSILON);
        assertEquals(340, mapper.toPixelY(-1), EPSILON);
    }

    @Test
    void shouldInvertVerticalAxis() {
        CartesianPlaneMapper mapper = new CartesianPlaneMapper(800, 600, 40);

        double positiveY = mapper.toPixelY(2);
        double negativeY = mapper.toPixelY(-2);

        assertEquals(220, positiveY, EPSILON);
        assertEquals(380, negativeY, EPSILON);
    }

    @Test
    void shouldConvertPixelsToMathematicalCoordinates() {
        CartesianPlaneMapper mapper = new CartesianPlaneMapper(800, 600, 40);

        assertEquals(2, mapper.toMathematicalX(480), EPSILON);
        assertEquals(1, mapper.toMathematicalY(260), EPSILON);
    }

    @Test
    void conversionShouldBeReversible() {
        CartesianPlaneMapper mapper = new CartesianPlaneMapper(900, 700, 35);

        double originalX = 3.75;
        double originalY = -2.25;

        double pixelX = mapper.toPixelX(originalX);
        double pixelY = mapper.toPixelY(originalY);

        assertEquals(
                originalX,
                mapper.toMathematicalX(pixelX),
                EPSILON);
        assertEquals(
                originalY,
                mapper.toMathematicalY(pixelY),
                EPSILON);
    }

    @Test
    void shouldRejectNonPositiveWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CartesianPlaneMapper(0, 600, 40));
    }

    @Test
    void shouldRejectNonPositiveHeight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CartesianPlaneMapper(800, 0, 40));
    }

    @Test
    void shouldRejectNonPositiveScale() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CartesianPlaneMapper(800, 600, 0));
    }
}