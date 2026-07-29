package com.eigenbound.domain.expedition.generation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;

/**
 * Generates deterministic roguelite expedition graphs.
 *
 * <p>
 * The same seed and difficulty always produce the same graph. Generated
 * nodes are organized into layers, and edges only connect consecutive layers.
 * This guarantees a directed acyclic graph.
 * </p>
 */
public final class ExpeditionMapGenerator {

    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 5;

    /**
     * Generates a complete expedition.
     *
     * @param seed       deterministic generation seed
     * @param difficulty expedition difficulty from one to five
     * @return generated expedition
     */
    public GeneratedExpedition generate(
            long seed,
            int difficulty) {
        validateDifficulty(difficulty);

        Random random = new Random(seed);

        List<List<ExpeditionNode>> layers = generateLayers(
                random,
                difficulty);

        List<ExpeditionNode> nodes = flattenLayers(layers);

        List<ExpeditionEdge> edges = generateEdges(
                random,
                layers,
                difficulty);

        ExpeditionMap map = new ExpeditionMap(
                nodes,
                edges,
                "start",
                "boss");

        return new GeneratedExpedition(
                map,
                seed,
                difficulty);
    }

    /**
     * Generates the start, intermediate and boss layers.
     */
    private List<List<ExpeditionNode>> generateLayers(
            Random random,
            int difficulty) {
        List<List<ExpeditionNode>> layers = new ArrayList<>();

        layers.add(
                List.of(
                        new ExpeditionNode(
                                "start",
                                0,
                                RoomType.START,
                                0)));

        int intermediateLayerCount = difficulty + 2;

        for (int layer = 1; layer <= intermediateLayerCount; layer++) {

            layers.add(
                    generateIntermediateLayer(
                            random,
                            layer,
                            intermediateLayerCount,
                            difficulty));
        }

        int bossLayer = intermediateLayerCount + 1;

        layers.add(
                List.of(
                        new ExpeditionNode(
                                "boss",
                                bossLayer,
                                RoomType.BOSS,
                                difficulty)));

        return List.copyOf(layers);
    }

    /**
     * Generates the rooms contained in one intermediate layer.
     */
    private List<ExpeditionNode> generateIntermediateLayer(
            Random random,
            int layer,
            int intermediateLayerCount,
            int difficulty) {

        int maximumWidth = maximumLayerWidth(difficulty);

        int nodeCount = 2 + random.nextInt(
                maximumWidth - 1);

        List<ExpeditionNode> nodes = new ArrayList<>();

        for (int index = 0; index < nodeCount; index++) {

            /*
             * The first node is always a standard vector challenge. This
             * guarantees that every expedition contains learning content.
             */
            RoomType type = index == 0
                    ? RoomType.VECTOR_CHALLENGE
                    : randomRoomType(
                            random,
                            difficulty);

            int roomDifficulty = difficultyForRoom(
                    type,
                    layer,
                    intermediateLayerCount,
                    difficulty);

            nodes.add(
                    new ExpeditionNode(
                            nodeId(layer, index),
                            layer,
                            type,
                            roomDifficulty));
        }

        return List.copyOf(nodes);
    }

    /**
     * Connects every layer to the next layer.
     *
     * <p>
     * Every source receives at least one outgoing edge, and every
     * destination receives at least one incoming edge.
     * </p>
     */
    private List<ExpeditionEdge> generateEdges(
            Random random,
            List<List<ExpeditionNode>> layers,
            int difficulty) {
        Set<ExpeditionEdge> edges = new LinkedHashSet<>();

        for (int layer = 0; layer < layers.size() - 1; layer++) {

            List<ExpeditionNode> sources = layers.get(layer);

            List<ExpeditionNode> destinations = layers.get(layer + 1);

            connectEverySource(
                    random,
                    sources,
                    destinations,
                    edges);

            connectEveryDestination(
                    random,
                    sources,
                    destinations,
                    edges);

            addOptionalConnections(
                    random,
                    sources,
                    destinations,
                    edges,
                    difficulty);
        }

        return List.copyOf(edges);
    }

    /**
     * Gives every source node at least one outgoing connection.
     */
    private void connectEverySource(
            Random random,
            List<ExpeditionNode> sources,
            List<ExpeditionNode> destinations,
            Set<ExpeditionEdge> edges) {
        for (ExpeditionNode source : sources) {
            ExpeditionNode destination = randomElement(
                    random,
                    destinations);

            edges.add(
                    new ExpeditionEdge(
                            source.id(),
                            destination.id()));
        }
    }

    /**
     * Gives every destination node at least one incoming connection.
     */
    private void connectEveryDestination(
            Random random,
            List<ExpeditionNode> sources,
            List<ExpeditionNode> destinations,
            Set<ExpeditionEdge> edges) {
        for (ExpeditionNode destination : destinations) {

            boolean hasIncomingEdge = edges.stream()
                    .anyMatch(
                            edge -> edge.destinationId()
                                    .equals(
                                            destination.id()));

            if (!hasIncomingEdge) {
                ExpeditionNode source = randomElement(
                        random,
                        sources);

                edges.add(
                        new ExpeditionEdge(
                                source.id(),
                                destination.id()));
            }
        }
    }

    /**
     * Adds extra routes to create meaningful player choices.
     */
    private void addOptionalConnections(
            Random random,
            List<ExpeditionNode> sources,
            List<ExpeditionNode> destinations,
            Set<ExpeditionEdge> edges,
            int difficulty) {
        double connectionProbability = 0.15 + difficulty * 0.05;

        for (ExpeditionNode source : sources) {
            for (ExpeditionNode destination : destinations) {

                ExpeditionEdge edge = new ExpeditionEdge(
                        source.id(),
                        destination.id());

                if (!edges.contains(edge)
                        && random.nextDouble() < connectionProbability) {
                    edges.add(edge);
                }
            }
        }
    }

    /**
     * Flattens the layered representation into one node list.
     */
    private List<ExpeditionNode> flattenLayers(
            List<List<ExpeditionNode>> layers) {
        List<ExpeditionNode> nodes = new ArrayList<>();

        for (List<ExpeditionNode> layer : layers) {
            nodes.addAll(layer);
        }

        return List.copyOf(nodes);
    }

    /**
     * Selects a room type using weighted probabilities.
     */
    private RoomType randomRoomType(
            Random random,
            int difficulty) {
        int roll = random.nextInt(100);

        if (roll < 50) {
            return RoomType.VECTOR_CHALLENGE;
        }

        if (roll < 68) {
            return RoomType.REWARD;
        }

        if (roll < 84) {
            return RoomType.REST;
        }

        return difficulty >= 3
                ? RoomType.ELITE_CHALLENGE
                : RoomType.VECTOR_CHALLENGE;
    }

    /**
     * Calculates the difficulty assigned to a room.
     */
    private int difficultyForRoom(
            RoomType type,
            int layer,
            int intermediateLayerCount,
            int expeditionDifficulty) {
        if (type == RoomType.REST
                || type == RoomType.REWARD) {
            return 0;
        }

        int progressionBonus = layer > intermediateLayerCount / 2
                ? 1
                : 0;

        int roomDifficulty = expeditionDifficulty
                + progressionBonus;

        if (type == RoomType.ELITE_CHALLENGE) {
            roomDifficulty++;
        }

        return Math.min(
                MAX_DIFFICULTY,
                roomDifficulty);
    }

    /**
     * Calculates the maximum number of rooms in one intermediate layer.
     */
    private int maximumLayerWidth(
            int difficulty) {
        return switch (difficulty) {
            case 1 -> 2;
            case 2, 3 -> 3;
            case 4, 5 -> 4;
            default -> throw new IllegalArgumentException(
                    "Unsupported difficulty: "
                            + difficulty);
        };
    }

    /**
     * Returns a deterministic random element from a list.
     */
    private <T> T randomElement(
            Random random,
            List<T> elements) {
        return elements.get(
                random.nextInt(elements.size()));
    }

    private String nodeId(
            int layer,
            int index) {
        return "room-"
                + layer
                + "-"
                + index;
    }

    private void validateDifficulty(
            int difficulty) {
        if (difficulty < MIN_DIFFICULTY
                || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException(
                    "Difficulty must be between "
                            + MIN_DIFFICULTY
                            + " and "
                            + MAX_DIFFICULTY);
        }
    }
}