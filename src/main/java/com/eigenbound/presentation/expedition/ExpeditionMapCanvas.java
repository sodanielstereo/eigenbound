package com.eigenbound.presentation.expedition;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.eigenbound.application.session.ExpeditionSession;
import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * JavaFX canvas that renders and detects interaction with a layered
 * expedition graph.
 *
 * <p>
 * The canvas reads the current state from an {@link ExpeditionSession}, draws
 * each room according to its navigation state and reports node selections to
 * the screen controller.
 * </p>
 */
public final class ExpeditionMapCanvas extends Canvas {

        private static final double DEFAULT_WIDTH = 1000;
        private static final double DEFAULT_HEIGHT = 650;
        private static final double LAYOUT_PADDING = 70;
        private static final double NODE_RADIUS = 22;
        private static final double ARROW_SIZE = 9;

        private static final Color BACKGROUND = Color.web("#0B0F19");

        private static final Color LOCKED_EDGE_COLOR = Color.web("#293247");

        private static final Color AVAILABLE_EDGE_COLOR = Color.web("#F4C95D");

        private static final Color VISITED_EDGE_COLOR = Color.web("#55D6A8");

        private static final Color CURRENT_NODE_BORDER = Color.web("#FFFFFF");

        private static final Color AVAILABLE_NODE_BORDER = Color.web("#F4C95D");

        private static final Color VISITED_NODE_BORDER = Color.web("#55D6A8");

        private static final Color LOCKED_NODE_BORDER = Color.web("#394660");

        private static final Color TEXT_COLOR = Color.web("#E8EEFA");

        private static final Color LOCKED_TEXT_COLOR = Color.web("#67738A");

        private final ExpeditionMapLayoutCalculator layoutCalculator = new ExpeditionMapLayoutCalculator();

        private final ExpeditionNodeHitTester hitTester = new ExpeditionNodeHitTester();

        private ExpeditionSession expeditionSession;

        private Map<String, ExpeditionNodePosition> nodePositions = Map.of();

        private Consumer<String> nodeSelectionHandler = ignoredNodeId -> {
        };

        /**
         * Creates an empty interactive expedition canvas.
         */
        public ExpeditionMapCanvas() {
                super(
                                DEFAULT_WIDTH,
                                DEFAULT_HEIGHT);

                widthProperty().addListener(
                                (observable, oldValue, newValue) -> redraw());

                heightProperty().addListener(
                                (observable, oldValue, newValue) -> redraw());

                setOnMouseClicked(
                                event -> handleMouseClick(
                                                event.getX(),
                                                event.getY()));

                setOnMouseMoved(
                                event -> updateCursor(
                                                event.getX(),
                                                event.getY()));

                setOnMouseExited(
                                event -> setCursor(Cursor.DEFAULT));

                redraw();
        }

        /**
         * Assigns an existing expedition session to the canvas.
         *
         * @param expeditionSession session whose progress will be rendered
         */
        public void setExpeditionSession(
                        ExpeditionSession expeditionSession) {
                this.expeditionSession = Objects.requireNonNull(
                                expeditionSession,
                                "Expedition session cannot be null");

                redraw();
        }

        /**
         * Creates a new session for an expedition map and displays it.
         *
         * <p>
         * This method preserves compatibility with controllers that only provide
         * an expedition map. Interactive controllers should preferably create the
         * session themselves and use {@link #setExpeditionSession}.
         * </p>
         *
         * @param expeditionMap map to display
         */
        public void setExpeditionMap(
                        ExpeditionMap expeditionMap) {
                Objects.requireNonNull(
                                expeditionMap,
                                "Expedition map cannot be null");

                setExpeditionSession(
                                new ExpeditionSession(expeditionMap));
        }

        /**
         * Registers the action executed when a map node is clicked.
         *
         * @param nodeSelectionHandler callback receiving the selected node ID
         */
        public void setOnNodeSelected(
                        Consumer<String> nodeSelectionHandler) {
                this.nodeSelectionHandler = Objects.requireNonNull(
                                nodeSelectionHandler,
                                "Node selection handler cannot be null");
        }

        /**
         * Redraws the complete expedition graph using the current session state.
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

                if (expeditionSession == null) {
                        nodePositions = Map.of();

                        drawEmptyMessage(
                                        graphics,
                                        width,
                                        height);

                        return;
                }

                ExpeditionMap map = expeditionSession.map();

                nodePositions = layoutCalculator.calculate(
                                map,
                                width,
                                height,
                                LAYOUT_PADDING);

                Set<String> visitedNodeIds = expeditionSession
                                .visitedNodes()
                                .stream()
                                .map(ExpeditionNode::id)
                                .collect(Collectors.toUnmodifiableSet());

                Set<String> availableNodeIds = expeditionSession
                                .availableNodes()
                                .stream()
                                .map(ExpeditionNode::id)
                                .collect(Collectors.toUnmodifiableSet());

                String currentNodeId = expeditionSession
                                .currentNode()
                                .id();

                drawEdges(
                                graphics,
                                map,
                                visitedNodeIds,
                                availableNodeIds,
                                currentNodeId);

                drawNodes(
                                graphics,
                                map,
                                visitedNodeIds,
                                availableNodeIds,
                                currentNodeId);
        }

        /**
         * Processes a pointer click and reports the selected node to the
         * controller.
         */
        private void handleMouseClick(
                        double pointX,
                        double pointY) {
                if (expeditionSession == null) {
                        return;
                }

                hitTester.findNodeAt(
                                nodePositions,
                                pointX,
                                pointY,
                                NODE_RADIUS)
                                .ifPresent(nodeSelectionHandler);

                updateCursor(
                                pointX,
                                pointY);
        }

        /**
         * Displays a hand cursor only when the pointer is over an available room.
         */
        private void updateCursor(
                        double pointX,
                        double pointY) {
                if (expeditionSession == null) {
                        setCursor(Cursor.DEFAULT);
                        return;
                }

                boolean overAvailableNode = hitTester.findNodeAt(
                                nodePositions,
                                pointX,
                                pointY,
                                NODE_RADIUS)
                                .map(this::isNodeAvailable)
                                .orElse(false);

                setCursor(
                                overAvailableNode
                                                ? Cursor.HAND
                                                : Cursor.DEFAULT);
        }

        /**
         * Determines whether a node can currently be selected.
         */
        private boolean isNodeAvailable(
                        String nodeId) {
                return expeditionSession
                                .availableNodes()
                                .stream()
                                .anyMatch(
                                                node -> node.id().equals(nodeId));
        }

        /**
         * Draws every directional connection using its navigation state.
         */
        private void drawEdges(
                        GraphicsContext graphics,
                        ExpeditionMap map,
                        Set<String> visitedNodeIds,
                        Set<String> availableNodeIds,
                        String currentNodeId) {
                for (ExpeditionEdge edge : map.edges()) {
                        ExpeditionNodePosition source = nodePositions.get(edge.sourceId());

                        ExpeditionNodePosition destination = nodePositions.get(edge.destinationId());

                        Color edgeColor = edgeColorFor(
                                        edge,
                                        visitedNodeIds,
                                        availableNodeIds,
                                        currentNodeId);

                        drawArrow(
                                        graphics,
                                        source,
                                        destination,
                                        edgeColor);
                }
        }

        /**
         * Selects the color of an edge according to expedition progress.
         */
        private Color edgeColorFor(
                        ExpeditionEdge edge,
                        Set<String> visitedNodeIds,
                        Set<String> availableNodeIds,
                        String currentNodeId) {
                boolean leadsToAvailableNode = edge.sourceId().equals(currentNodeId)
                                && availableNodeIds.contains(
                                                edge.destinationId());

                if (leadsToAvailableNode) {
                        return AVAILABLE_EDGE_COLOR;
                }

                boolean belongsToVisitedPath = visitedNodeIds.contains(edge.sourceId())
                                && visitedNodeIds.contains(
                                                edge.destinationId());

                if (belongsToVisitedPath) {
                        return VISITED_EDGE_COLOR;
                }

                return LOCKED_EDGE_COLOR;
        }

        /**
         * Draws one directional graph edge.
         */
        private void drawArrow(
                        GraphicsContext graphics,
                        ExpeditionNodePosition source,
                        ExpeditionNodePosition destination,
                        Color color) {
                double deltaX = destination.x() - source.x();

                double deltaY = destination.y() - source.y();

                double length = Math.hypot(
                                deltaX,
                                deltaY);

                if (length == 0) {
                        return;
                }

                double unitX = deltaX / length;
                double unitY = deltaY / length;

                double startX = source.x() + unitX * NODE_RADIUS;

                double startY = source.y() + unitY * NODE_RADIUS;

                double endX = destination.x() - unitX * NODE_RADIUS;

                double endY = destination.y() - unitY * NODE_RADIUS;

                graphics.setStroke(color);
                graphics.setFill(color);
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
                                                * Math.cos(
                                                                angle - Math.PI / 6);

                double leftY = endY
                                - ARROW_SIZE
                                                * Math.sin(
                                                                angle - Math.PI / 6);

                double rightX = endX
                                - ARROW_SIZE
                                                * Math.cos(
                                                                angle + Math.PI / 6);

                double rightY = endY
                                - ARROW_SIZE
                                                * Math.sin(
                                                                angle + Math.PI / 6);

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
         * Draws every room over the previously rendered edges.
         */
        private void drawNodes(
                        GraphicsContext graphics,
                        ExpeditionMap map,
                        Set<String> visitedNodeIds,
                        Set<String> availableNodeIds,
                        String currentNodeId) {
                for (ExpeditionNode node : map.nodes()) {
                        ExpeditionNodePosition position = nodePositions.get(node.id());

                        NodeState state = stateFor(
                                        node.id(),
                                        visitedNodeIds,
                                        availableNodeIds,
                                        currentNodeId);

                        drawNode(
                                        graphics,
                                        node,
                                        position,
                                        state);
                }
        }

        /**
         * Determines the visual navigation state of a room.
         */
        private NodeState stateFor(
                        String nodeId,
                        Set<String> visitedNodeIds,
                        Set<String> availableNodeIds,
                        String currentNodeId) {
                if (nodeId.equals(currentNodeId)) {
                        return NodeState.CURRENT;
                }

                if (availableNodeIds.contains(nodeId)) {
                        return NodeState.AVAILABLE;
                }

                if (visitedNodeIds.contains(nodeId)) {
                        return NodeState.VISITED;
                }

                return NodeState.LOCKED;
        }

        /**
         * Draws one room with its navigation state, symbol and identifier.
         */
        private void drawNode(
                        GraphicsContext graphics,
                        ExpeditionNode node,
                        ExpeditionNodePosition position,
                        NodeState state) {
                graphics.setFill(
                                fillColorFor(
                                                node.type(),
                                                state));

                graphics.fillOval(
                                position.x() - NODE_RADIUS,
                                position.y() - NODE_RADIUS,
                                NODE_RADIUS * 2,
                                NODE_RADIUS * 2);

                graphics.setStroke(
                                borderColorFor(state));

                graphics.setLineWidth(
                                borderWidthFor(state));

                graphics.strokeOval(
                                position.x() - NODE_RADIUS,
                                position.y() - NODE_RADIUS,
                                NODE_RADIUS * 2,
                                NODE_RADIUS * 2);

                graphics.setFill(
                                state == NodeState.LOCKED
                                                ? LOCKED_TEXT_COLOR
                                                : TEXT_COLOR);

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
                                Font.font(
                                                "Segoe UI",
                                                10));

                graphics.fillText(
                                node.id(),
                                position.x(),
                                position.y()
                                                + NODE_RADIUS
                                                + 15);
        }

        /**
         * Selects the fill color for a room and dims inactive rooms.
         */
        private Color fillColorFor(
                        RoomType type,
                        NodeState state) {
                Color baseColor = colorFor(type);

                return switch (state) {
                        case CURRENT, AVAILABLE ->
                                baseColor;

                        case VISITED ->
                                baseColor.deriveColor(
                                                0,
                                                0.75,
                                                0.75,
                                                0.85);

                        case LOCKED ->
                                baseColor.deriveColor(
                                                0,
                                                0.35,
                                                0.40,
                                                0.40);
                };
        }

        /**
         * Selects the border color for a room state.
         */
        private Color borderColorFor(
                        NodeState state) {
                return switch (state) {
                        case CURRENT ->
                                CURRENT_NODE_BORDER;

                        case AVAILABLE ->
                                AVAILABLE_NODE_BORDER;

                        case VISITED ->
                                VISITED_NODE_BORDER;

                        case LOCKED ->
                                LOCKED_NODE_BORDER;
                };
        }

        /**
         * Selects the border width for a room state.
         */
        private double borderWidthFor(
                        NodeState state) {
                return switch (state) {
                        case CURRENT -> 4;
                        case AVAILABLE -> 3;
                        case VISITED -> 2;
                        case LOCKED -> 1.5;
                };
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
                                Font.font(
                                                "Segoe UI",
                                                16));

                graphics.fillText(
                                "No expedition generated",
                                width / 2.0,
                                height / 2.0);
        }

        /**
         * Selects the base display color for each room type.
         */
        private Color colorFor(
                        RoomType type) {
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
        private String symbolFor(
                        RoomType type) {
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
        public double prefWidth(
                        double height) {
                return DEFAULT_WIDTH;
        }

        @Override
        public double prefHeight(
                        double width) {
                return DEFAULT_HEIGHT;
        }

        @Override
        public void resize(
                        double width,
                        double height) {
                setWidth(width);
                setHeight(height);
        }

        /**
         * Visual states supported by an expedition room.
         */
        private enum NodeState {
                CURRENT,
                AVAILABLE,
                VISITED,
                LOCKED
        }
}