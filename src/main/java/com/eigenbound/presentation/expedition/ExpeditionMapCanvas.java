package com.eigenbound.presentation.expedition;

import java.util.Map;
import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * JavaFX canvas that renders a layered expedition graph.
 */
public final class ExpeditionMapCanvas extends Canvas {

    private static final double DEFAULT_WIDTH = 1000;
    private static final double DEFAULT_HEIGHT = 650;
    private static final double LAYOUT_PADDING = 70;
    private static final double NODE_RADIUS = 22;
    private static final double ARROW_SIZE = 9;

    private static final Color BACKGROUND = Color.web("#0B0F19");
    private static final Color EDGE_COLOR = Color.web("#42506A");
    private static final Color NODE_BORDER = Color.web("#D3DCF0");
    private static final Color TEXT_COLOR = Color.web("#E8EEFA");

    private final ExpeditionMapLayoutCalculator layoutCalculator = new ExpeditionMapLayoutCalculator();

    private ExpeditionMap expeditionMap;

    /**
     * Creates an empty expedition canvas.
     */
    public ExpeditionMapCanvas() {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        widthProperty().addListener(
                (observable, oldValue, newValue) -> redraw());

        heightProperty().addListener(
                (observable, oldValue, newValue) -> redraw());

        redraw();
    }

    /**
     * Assigns the expedition displayed by the canvas.
     *
     * @param expeditionMap map to render
     */
    public void setExpeditionMap(
            ExpeditionMap expeditionMap) {
        this.expeditionMap = Objects.requireNonNull(
                expeditionMap,
                "Expedition map cannot be null");

        redraw();
    }

    /**
     * Redraws the complete expedition graph.
     */
    public void redraw() {
        double width = getWidth();
        double height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        GraphicsContext graphics = getGraphicsContext2D();

        graphics.setFill(BACKGROUND);
        graphics.fillRect(
                0,
                0,
                width,
                height);

        if (expeditionMap == null) {
            drawEmptyMessage(graphics, width, height);
            return;
        }

        Map<String, ExpeditionNodePosition> positions = layoutCalculator.calculate(
                expeditionMap,
                width,
                height,
                LAYOUT_PADDING);

        drawEdges(graphics, positions);
        drawNodes(graphics, positions);
    }

    /**
     * Draws every directed connection before drawing the nodes.
     */
    private void drawEdges(
            GraphicsContext graphics,
            Map<String, ExpeditionNodePosition> positions) {
        for (ExpeditionEdge edge : expeditionMap.edges()) {

            ExpeditionNodePosition source = positions.get(edge.sourceId());

            ExpeditionNodePosition destination = positions.get(
                    edge.destinationId());

            drawArrow(
                    graphics,
                    source,
                    destination);
        }
    }

    /**
     * Draws one directional graph edge.
     */
    private void drawArrow(
            GraphicsContext graphics,
            ExpeditionNodePosition source,
            ExpeditionNodePosition destination) {
        double deltaX = destination.x() - source.x();

        double deltaY = destination.y() - source.y();

        double length = Math.hypot(deltaX, deltaY);

        if (length == 0) {
            return;
        }

        double unitX = deltaX / length;
        double unitY = deltaY / length;

        double startX = source.x() + unitX * NODE_RADIUS;

        double startY = source.y() + unitY * NODE_RADIUS;

        double endX = destination.x()
                - unitX * NODE_RADIUS;

        double endY = destination.y()
                - unitY * NODE_RADIUS;

        graphics.setStroke(EDGE_COLOR);
        graphics.setFill(EDGE_COLOR);
        graphics.setLineWidth(2.5);

        graphics.strokeLine(
                startX,
                startY,
                endX,
                endY);

        double angle = Math.atan2(
                endY - startY,
                endX - startX);

        double leftX = endX
                - ARROW_SIZE
                        * Math.cos(angle - Math.PI / 6);

        double leftY = endY
                - ARROW_SIZE
                        * Math.sin(angle - Math.PI / 6);

        double rightX = endX
                - ARROW_SIZE
                        * Math.cos(angle + Math.PI / 6);

        double rightY = endY
                - ARROW_SIZE
                        * Math.sin(angle + Math.PI / 6);

        graphics.fillPolygon(
                new double[] {
                        endX,
                        leftX,
                        rightX
                },
                new double[] {
                        endY,
                        leftY,
                        rightY
                },
                3);
    }

    /**
     * Draws every graph node over the previously rendered edges.
     */
    private void drawNodes(
            GraphicsContext graphics,
            Map<String, ExpeditionNodePosition> positions) {
        for (ExpeditionNode node : expeditionMap.nodes()) {

            ExpeditionNodePosition position = positions.get(node.id());

            drawNode(
                    graphics,
                    node,
                    position);
        }
    }

    /**
     * Draws one room with its type symbol and identifier.
     */
    private void drawNode(
            GraphicsContext graphics,
            ExpeditionNode node,
            ExpeditionNodePosition position) {
        graphics.setFill(
                colorFor(node.type()));

        graphics.fillOval(
                position.x() - NODE_RADIUS,
                position.y() - NODE_RADIUS,
                NODE_RADIUS * 2,
                NODE_RADIUS * 2);

        graphics.setStroke(NODE_BORDER);
        graphics.setLineWidth(2);

        graphics.strokeOval(
                position.x() - NODE_RADIUS,
                position.y() - NODE_RADIUS,
                NODE_RADIUS * 2,
                NODE_RADIUS * 2);

        graphics.setFill(TEXT_COLOR);
        graphics.setTextAlign(
                TextAlignment.CENTER);

        graphics.setFont(
                Font.font(
                        "Segoe UI",
                        FontWeight.BOLD,
                        14));

        graphics.fillText(
                symbolFor(node.type()),
                position.x(),
                position.y() + 5);

        graphics.setFont(
                Font.font("Segoe UI", 10));

        graphics.fillText(
                node.id(),
                position.x(),
                position.y()
                        + NODE_RADIUS
                        + 15);
    }

    /**
     * Displays a message while no expedition has been assigned.
     */
    private void drawEmptyMessage(
            GraphicsContext graphics,
            double width,
            double height) {
        graphics.setFill(TEXT_COLOR);
        graphics.setTextAlign(
                TextAlignment.CENTER);
        graphics.setFont(
                Font.font("Segoe UI", 16));

        graphics.fillText(
                "No expedition generated",
                width / 2.0,
                height / 2.0);
    }

    /**
     * Selects a display color for each room type.
     */
    private Color colorFor(RoomType type) {
        return switch (type) {
            case START ->
                Color.web("#46A6FF");

            case VECTOR_CHALLENGE ->
                Color.web("#8D5CFF");

            case ELITE_CHALLENGE ->
                Color.web("#D85CFF");

            case REST ->
                Color.web("#55D6A8");

            case REWARD ->
                Color.web("#F4C95D");

            case BOSS ->
                Color.web("#FF667D");
        };
    }

    /**
     * Selects a compact symbol for each room type.
     */
    private String symbolFor(RoomType type) {
        return switch (type) {
            case START -> "S";
            case VECTOR_CHALLENGE -> "V";
            case ELITE_CHALLENGE -> "E";
            case REST -> "R";
            case REWARD -> "◆";
            case BOSS -> "B";
        };
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
    public void resize(
            double width,
            double height) {
        setWidth(width);
        setHeight(height);
    }
}