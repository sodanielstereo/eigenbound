package com.eigenbound.presentation.canvas;

/**
 * Converts between mathematical coordinates and JavaFX pixel coordinates.
 *
 * The mathematical origin is placed at the center of the drawing area.
 */
public final class CartesianPlaneMapper {

    private final double width;
    private final double height;
    private final double scale;

    public CartesianPlaneMapper(
            double width,
            double height,
            double scale) {
        if (width <= 0) {
            throw new IllegalArgumentException(
                    "Width must be greater than zero");
        }

        if (height <= 0) {
            throw new IllegalArgumentException(
                    "Height must be greater than zero");
        }

        if (scale <= 0) {
            throw new IllegalArgumentException(
                    "Scale must be greater than zero");
        }

        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public double toPixelX(double mathematicalX) {
        return width / 2.0 + mathematicalX * scale;
    }

    public double toPixelY(double mathematicalY) {
        return height / 2.0 - mathematicalY * scale;
    }

    public double toMathematicalX(double pixelX) {
        return (pixelX - width / 2.0) / scale;
    }

    public double toMathematicalY(double pixelY) {
        return (height / 2.0 - pixelY) / scale;
    }

    public double originPixelX() {
        return width / 2.0;
    }

    public double originPixelY() {
        return height / 2.0;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double scale() {
        return scale;
    }
}