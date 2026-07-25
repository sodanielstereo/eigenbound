package com.eigenbound.presentation.canvas;

import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * JavaFX canvas that renders vector challenges on a Cartesian plane.
 */
public final class VectorCanvas extends Canvas {

    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;
    private static final double DEFAULT_SCALE = 40;
    private static final double MINIMUM_SCALE = 10;
    private static final double VIEW_PADDING = 60;
    private static final double POINT_RADIUS = 7;
    private static final double ARROW_SIZE = 10;

    private static final Color BACKGROUND_COLOR = Color.web("#101522");
    private static final Color GRID_COLOR = Color.web("#293247");
    private static final Color AXIS_COLOR = Color.web("#8794AD");
    private static final Color LABEL_COLOR = Color.web("#AEB9CC");
    private static final Color START_COLOR = Color.web("#46A6FF");
    private static final Color TARGET_COLOR = Color.web("#F4C95D");
    private static final Color PATH_COLOR = Color.web("#A879FF");
    private static final Color CURRENT_COLOR = Color.web("#55D6A8");

    private VectorChallenge challenge;
    private List<Vector2> selectedMoves = List.of();

    public VectorCanvas() {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        widthProperty().addListener(
                (observable, oldValue, newValue) -> redraw());
        heightProperty().addListener(
                (observable, oldValue, newValue) -> redraw());

        redraw();
    }

    public void setChallenge(VectorChallenge challenge) {
        this.challenge = Objects.requireNonNull(
                challenge,
                "Challenge cannot be null");
        this.selectedMoves = List.of();
        redraw();
    }

    public void setSelectedMoves(List<Vector2> selectedMoves) {
        Objects.requireNonNull(
                selectedMoves,
                "Selected movements cannot be null");

        this.selectedMoves = List.copyOf(selectedMoves);
        redraw();
    }

    public void redraw() {
        double width = getWidth();
        double height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        GraphicsContext graphics = getGraphicsContext2D();
        double scale = calculateScale();

        CartesianPlaneMapper mapper = new CartesianPlaneMapper(width, height, scale);

        clear(graphics, width, height);
        drawGrid(graphics, mapper);
        drawAxes(graphics, mapper);
        drawAxisLabels(graphics, mapper);

        if (challenge != null) {
            drawChallenge(graphics, mapper);
        }
    }

    private void clear(
            GraphicsContext graphics,
            double width,
            double height) {
        graphics.setFill(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);
    }

    private void drawGrid(
            GraphicsContext graphics,
            CartesianPlaneMapper mapper) {
        graphics.setStroke(GRID_COLOR);
        graphics.setLineWidth(1);

        int minimumX = (int) Math.floor(
                mapper.toMathematicalX(0));
        int maximumX = (int) Math.ceil(
                mapper.toMathematicalX(mapper.width()));

        for (int x = minimumX; x <= maximumX; x++) {
            double pixelX = mapper.toPixelX(x);

            graphics.strokeLine(
                    pixelX,
                    0,
                    pixelX,
                    mapper.height());
        }

        int maximumY = (int) Math.ceil(
                mapper.toMathematicalY(0));
        int minimumY = (int) Math.floor(
                mapper.toMathematicalY(mapper.height()));

        for (int y = minimumY; y <= maximumY; y++) {
            double pixelY = mapper.toPixelY(y);

            graphics.strokeLine(
                    0,
                    pixelY,
                    mapper.width(),
                    pixelY);
        }
    }

    private void drawAxes(
            GraphicsContext graphics,
            CartesianPlaneMapper mapper) {
        graphics.setStroke(AXIS_COLOR);
        graphics.setLineWidth(2);

        graphics.strokeLine(
                0,
                mapper.originPixelY(),
                mapper.width(),
                mapper.originPixelY());

        graphics.strokeLine(
                mapper.originPixelX(),
                0,
                mapper.originPixelX(),
                mapper.height());
    }

    private void drawAxisLabels(
            GraphicsContext graphics,
            CartesianPlaneMapper mapper) {
        graphics.setFill(LABEL_COLOR);
        graphics.setFont(Font.font(11));
        graphics.setTextAlign(TextAlignment.CENTER);

        int minimumX = (int) Math.floor(
                mapper.toMathematicalX(0));
        int maximumX = (int) Math.ceil(
                mapper.toMathematicalX(mapper.width()));

        for (int x = minimumX; x <= maximumX; x++) {
            if (x != 0) {
                graphics.fillText(
                        Integer.toString(x),
                        mapper.toPixelX(x),
                        mapper.originPixelY() + 16);
            }
        }

        int maximumY = (int) Math.ceil(
                mapper.toMathematicalY(0));
        int minimumY = (int) Math.floor(
                mapper.toMathematicalY(mapper.height()));

        graphics.setTextAlign(TextAlignment.RIGHT);

        for (int y = minimumY; y <= maximumY; y++) {
            if (y != 0) {
                graphics.fillText(
                        Integer.toString(y),
                        mapper.originPixelX() - 7,
                        mapper.toPixelY(y) + 4);
            }
        }
    }

    private void drawChallenge(
            GraphicsContext graphics,
            CartesianPlaneMapper mapper) {
        drawPoint(
                graphics,
                mapper,
                challenge.start(),
                START_COLOR,
                "Start");

        drawPoint(
                graphics,
                mapper,
                challenge.target(),
                TARGET_COLOR,
                "Target");

        Vector2 currentPosition = challenge.start();

        for (Vector2 movement : selectedMoves) {
            Vector2 nextPosition = currentPosition.add(movement);

            drawArrow(
                    graphics,
                    mapper,
                    currentPosition,
                    nextPosition);

            currentPosition = nextPosition;
        }

        drawPoint(
                graphics,
                mapper,
                currentPosition,
                CURRENT_COLOR,
                formatVector(currentPosition));
    }

    private void drawArrow(
            GraphicsContext graphics,
            CartesianPlaneMapper mapper,
            Vector2 start,
            Vector2 end) {
        double startX = mapper.toPixelX(start.x());
        double startY = mapper.toPixelY(start.y());
        double endX = mapper.toPixelX(end.x());
        double endY = mapper.toPixelY(end.y());

        graphics.setStroke(PATH_COLOR);
        graphics.setFill(PATH_COLOR);
        graphics.setLineWidth(4);

        graphics.strokeLine(startX, startY, endX, endY);

        double angle = Math.atan2(
                endY - startY,
                endX - startX);

        double leftX = endX - ARROW_SIZE
                * Math.cos(angle - Math.PI / 6);
        double leftY = endY - ARROW_SIZE
                * Math.sin(angle - Math.PI / 6);

        double rightX = endX - ARROW_SIZE
                * Math.cos(angle + Math.PI / 6);
        double rightY = endY - ARROW_SIZE
                * Math.sin(angle + Math.PI / 6);

        graphics.fillPolygon(
                new double[] { endX, leftX, rightX },
                new double[] { endY, leftY, rightY },
                3);
    }

    private void drawPoint(
            GraphicsContext graphics,
            CartesianPlaneMapper mapper,
            Vector2 point,
            Color color,
            String label) {
        double pixelX = mapper.toPixelX(point.x());
        double pixelY = mapper.toPixelY(point.y());

        graphics.setFill(color);
        graphics.fillOval(
                pixelX - POINT_RADIUS,
                pixelY - POINT_RADIUS,
                POINT_RADIUS * 2,
                POINT_RADIUS * 2);

        graphics.setFont(Font.font(12));
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.fillText(
                label,
                pixelX + POINT_RADIUS + 4,
                pixelY - POINT_RADIUS);
    }

    private String formatVector(Vector2 vector) {
        return "("
                + formatNumber(vector.x())
                + ", "
                + formatNumber(vector.y())
                + ")";
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }

        return String.format("%.2f", value);
    }

    /**
     * Adjusts the view so the start, target and complete path remain visible.
     */
    private double calculateScale() {
        if (challenge == null) {
            return DEFAULT_SCALE;
        }

        double maximumAbsoluteX = Math.max(
                Math.abs(challenge.start().x()),
                Math.abs(challenge.target().x()));

        double maximumAbsoluteY = Math.max(
                Math.abs(challenge.start().y()),
                Math.abs(challenge.target().y()));

        Vector2 currentPosition = challenge.start();

        for (Vector2 movement : selectedMoves) {
            currentPosition = currentPosition.add(movement);

            maximumAbsoluteX = Math.max(
                    maximumAbsoluteX,
                    Math.abs(currentPosition.x()));
            maximumAbsoluteY = Math.max(
                    maximumAbsoluteY,
                    Math.abs(currentPosition.y()));
        }

        double horizontalSpace = Math.max(
                1,
                getWidth() / 2.0 - VIEW_PADDING);
        double verticalSpace = Math.max(
                1,
                getHeight() / 2.0 - VIEW_PADDING);

        double horizontalScale = maximumAbsoluteX == 0
                ? DEFAULT_SCALE
                : horizontalSpace / maximumAbsoluteX;

        double verticalScale = maximumAbsoluteY == 0
                ? DEFAULT_SCALE
                : verticalSpace / maximumAbsoluteY;

        double fittedScale = Math.min(
                horizontalScale,
                verticalScale);

        return Math.max(
                MINIMUM_SCALE,
                Math.min(DEFAULT_SCALE, fittedScale));
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return DEFAULT_WIDTH;
    }

    @Override
    public double prefHeight(double width) {
        return DEFAULT_HEIGHT;
    }

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }
}