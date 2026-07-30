package com.eigenbound.presentation.expedition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;

/**
 * Calculates deterministic canvas positions for expedition nodes.
 *
 * <p>
 * Graph layers are distributed horizontally. Nodes belonging to the same
 * layer are distributed vertically with equal spacing.
 * </p>
 */
public final class ExpeditionMapLayoutCalculator {

    /**
     * Calculates the visual position of every node.
     *
     * @param map     expedition graph
     * @param width   available canvas width
     * @param height  available canvas height
     * @param padding empty space around the graph
     * @return immutable node-position map
     */
    public Map<String, ExpeditionNodePosition> calculate(
            ExpeditionMap map,
            double width,
            double height,
            double padding) {
        Objects.requireNonNull(
                map,
                "Expedition map cannot be null");

        validateDimensions(
                width,
                height,
                padding);

        Map<Integer, List<ExpeditionNode>> nodesByLayer = groupNodesByLayer(map);

        int maximumLayer = nodesByLayer.keySet()
                .stream()
                .max(Integer::compareTo)
                .orElseThrow();

        Map<String, ExpeditionNodePosition> positions = new LinkedHashMap<>();

        for (Map.Entry<Integer, List<ExpeditionNode>> entry : nodesByLayer.entrySet()) {

            int layer = entry.getKey();

            List<ExpeditionNode> layerNodes = entry.getValue();

            double x = calculateX(
                    layer,
                    maximumLayer,
                    width,
                    padding);

            addLayerPositions(
                    positions,
                    layerNodes,
                    x,
                    height,
                    padding);
        }

        return Map.copyOf(positions);
    }

    /**
     * Groups nodes by layer while preserving deterministic ordering.
     */
    private Map<Integer, List<ExpeditionNode>> groupNodesByLayer(
            ExpeditionMap map) {

        List<ExpeditionNode> sortedNodes = new ArrayList<>(map.nodes());

        sortedNodes.sort(
                Comparator.comparingInt(
                        ExpeditionNode::layer).thenComparing(
                                ExpeditionNode::id));

        Map<Integer, List<ExpeditionNode>> result = new LinkedHashMap<>();

        for (ExpeditionNode node : sortedNodes) {
            result.computeIfAbsent(
                    node.layer(),
                    ignored -> new ArrayList<>()).add(node);
        }

        return result;
    }

    /**
     * Calculates the horizontal coordinate for one layer.
     */
    private double calculateX(
            int layer,
            int maximumLayer,
            double width,
            double padding) {
        if (maximumLayer == 0) {
            return width / 2.0;
        }

        double usableWidth = width - padding * 2;

        return padding
                + ((double) layer
                        / maximumLayer) * usableWidth;
    }

    /**
     * Distributes one layer's nodes vertically.
     */
    private void addLayerPositions(
            Map<String, ExpeditionNodePosition> positions,
            List<ExpeditionNode> nodes,
            double x,
            double height,
            double padding) {
        double usableHeight = height - padding * 2;

        double verticalSpacing = usableHeight / (nodes.size() + 1);

        for (int index = 0; index < nodes.size(); index++) {

            ExpeditionNode node = nodes.get(index);

            double y = padding
                    + verticalSpacing
                            * (index + 1);

            positions.put(
                    node.id(),
                    new ExpeditionNodePosition(
                            node.id(),
                            x,
                            y));
        }
    }

    /**
     * Validates the available drawing area.
     */
    private void validateDimensions(
            double width,
            double height,
            double padding) {
        if (!Double.isFinite(width)
                || !Double.isFinite(height)
                || !Double.isFinite(padding)) {
            throw new IllegalArgumentException(
                    "Layout dimensions must be finite");
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Layout dimensions must be positive");
        }

        if (padding < 0) {
            throw new IllegalArgumentException(
                    "Padding cannot be negative");
        }

        if (padding * 2 >= width
                || padding * 2 >= height) {
            throw new IllegalArgumentException(
                    "Padding leaves no drawing area");
        }
    }
}