package com.eigenbound.domain.expedition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Immutable directed graph representing a roguelite expedition.
 *
 * <p>
 * Nodes represent rooms and edges represent valid forward routes. Layers
 * enforce a directed acyclic structure because every edge must connect a node
 * to another node in a greater layer.
 * </p>
 */
public final class ExpeditionMap {

    private final List<ExpeditionNode> nodes;
    private final List<ExpeditionEdge> edges;
    private final String startNodeId;
    private final String bossNodeId;
    private final Map<String, ExpeditionNode> nodesById;
    private final Map<String, List<ExpeditionNode>> adjacency;

    /**
     * Creates and validates an immutable expedition graph.
     *
     * @param nodes       expedition rooms
     * @param edges       directed room connections
     * @param startNodeId configured starting node
     * @param bossNodeId  configured final boss node
     */
    public ExpeditionMap(
            List<ExpeditionNode> nodes,
            List<ExpeditionEdge> edges,
            String startNodeId,
            String bossNodeId) {
        Objects.requireNonNull(
                nodes,
                "Nodes cannot be null");
        Objects.requireNonNull(
                edges,
                "Edges cannot be null");
        Objects.requireNonNull(
                startNodeId,
                "Start node ID cannot be null");
        Objects.requireNonNull(
                bossNodeId,
                "Boss node ID cannot be null");

        this.nodes = List.copyOf(nodes);
        this.edges = List.copyOf(edges);
        this.startNodeId = normalizeId(startNodeId);
        this.bossNodeId = normalizeId(bossNodeId);

        if (this.nodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expedition must contain nodes");
        }

        this.nodesById = buildNodeIndex();
        validateSpecialNodes();
        validateEdges();
        this.adjacency = buildAdjacency();

        if (!isReachable(
                this.startNodeId,
                this.bossNodeId)) {
            throw new IllegalArgumentException(
                    "Boss node must be reachable from start");
        }
    }

    /**
     * Returns an immutable list of all expedition nodes.
     *
     * @return graph nodes
     */
    public List<ExpeditionNode> nodes() {
        return nodes;
    }

    /**
     * Returns an immutable list of all expedition edges.
     *
     * @return graph edges
     */
    public List<ExpeditionEdge> edges() {
        return edges;
    }

    /**
     * Returns the configured starting-node identifier.
     *
     * @return start node ID
     */
    public String startNodeId() {
        return startNodeId;
    }

    /**
     * Returns the configured boss-node identifier.
     *
     * @return boss node ID
     */
    public String bossNodeId() {
        return bossNodeId;
    }

    /**
     * Finds a node by its identifier.
     *
     * @param nodeId identifier to search
     * @return matching expedition node
     * @throws IllegalArgumentException when the node does not exist
     */
    public ExpeditionNode findNode(String nodeId) {
        return requireNode(nodeId);
    }

    /**
     * Returns every node directly reachable from a given node.
     *
     * @param nodeId origin node identifier
     * @return immutable neighbor list
     */
    public List<ExpeditionNode> neighborsOf(
            String nodeId) {
        String normalizedId = requireNode(nodeId).id();

        return adjacency.get(normalizedId);
    }

    /**
     * Determines whether a direct edge exists.
     *
     * @param sourceId      origin node identifier
     * @param destinationId destination node identifier
     * @return {@code true} when the direct connection exists
     */
    public boolean hasConnection(
            String sourceId,
            String destinationId) {
        ExpeditionNode source = requireNode(sourceId);

        ExpeditionNode destination = requireNode(destinationId);

        return adjacency.get(source.id())
                .contains(destination);
    }

    /**
     * Determines whether one node can reach another through any number of
     * directed connections.
     *
     * <p>
     * This method uses breadth-first search.
     * </p>
     *
     * @param sourceId      origin node identifier
     * @param destinationId destination node identifier
     * @return {@code true} when a route exists
     */
    public boolean isReachable(
            String sourceId,
            String destinationId) {
        ExpeditionNode source = requireNode(sourceId);

        ExpeditionNode destination = requireNode(destinationId);

        if (source.equals(destination)) {
            return true;
        }

        Queue<ExpeditionNode> frontier = new ArrayDeque<>();

        Set<String> visited = new HashSet<>();

        frontier.add(source);
        visited.add(source.id());

        while (!frontier.isEmpty()) {
            ExpeditionNode current = frontier.remove();

            for (ExpeditionNode neighbor : adjacency.get(current.id())) {

                if (neighbor.equals(destination)) {
                    return true;
                }

                if (visited.add(neighbor.id())) {
                    frontier.add(neighbor);
                }
            }
        }

        return false;
    }

    private Map<String, ExpeditionNode> buildNodeIndex() {

        Map<String, ExpeditionNode> index = new LinkedHashMap<>();

        for (ExpeditionNode node : nodes) {
            ExpeditionNode previous = index.putIfAbsent(
                    node.id(),
                    node);

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Duplicate node ID: "
                                + node.id());
            }
        }

        return Map.copyOf(index);
    }

    private void validateSpecialNodes() {
        List<ExpeditionNode> startNodes = nodes.stream()
                .filter(
                        node -> node.type() == RoomType.START)
                .toList();

        List<ExpeditionNode> bossNodes = nodes.stream()
                .filter(
                        node -> node.type() == RoomType.BOSS)
                .toList();

        if (startNodes.size() != 1) {
            throw new IllegalArgumentException(
                    "Expedition must contain exactly one start node");
        }

        if (bossNodes.size() != 1) {
            throw new IllegalArgumentException(
                    "Expedition must contain exactly one boss node");
        }

        ExpeditionNode configuredStart = requireNode(startNodeId);

        ExpeditionNode configuredBoss = requireNode(bossNodeId);

        if (configuredStart.type() != RoomType.START) {
            throw new IllegalArgumentException(
                    "Configured start node must have START type");
        }

        if (configuredBoss.type() != RoomType.BOSS) {
            throw new IllegalArgumentException(
                    "Configured boss node must have BOSS type");
        }
    }

    private void validateEdges() {
        Set<ExpeditionEdge> uniqueEdges = new HashSet<>();

        for (ExpeditionEdge edge : edges) {
            if (!uniqueEdges.add(edge)) {
                throw new IllegalArgumentException(
                        "Duplicate edge: "
                                + edge.sourceId()
                                + " -> "
                                + edge.destinationId());
            }

            ExpeditionNode source = requireNode(edge.sourceId());

            ExpeditionNode destination = requireNode(
                    edge.destinationId());

            if (source.layer() >= destination.layer()) {
                throw new IllegalArgumentException(
                        "Edges must move to a greater layer");
            }

            if (source.type() == RoomType.BOSS) {
                throw new IllegalArgumentException(
                        "Boss node cannot have outgoing edges");
            }

            if (destination.type() == RoomType.START) {
                throw new IllegalArgumentException(
                        "Start node cannot have incoming edges");
            }
        }
    }

    private Map<String, List<ExpeditionNode>> buildAdjacency() {

        Map<String, List<ExpeditionNode>> result = new LinkedHashMap<>();

        for (ExpeditionNode node : nodes) {
            result.put(
                    node.id(),
                    new ArrayList<>());
        }

        for (ExpeditionEdge edge : edges) {
            result.get(edge.sourceId())
                    .add(
                            nodesById.get(
                                    edge.destinationId()));
        }

        Map<String, List<ExpeditionNode>> immutableResult = new LinkedHashMap<>();

        for (Map.Entry<String, List<ExpeditionNode>> entry : result.entrySet()) {

            immutableResult.put(
                    entry.getKey(),
                    List.copyOf(entry.getValue()));
        }

        return Map.copyOf(immutableResult);
    }

    private ExpeditionNode requireNode(
            String nodeId) {
        String normalizedId = normalizeId(nodeId);

        ExpeditionNode node = nodesById.get(normalizedId);

        if (node == null) {
            throw new IllegalArgumentException(
                    "Unknown node ID: "
                            + normalizedId);
        }

        return node;
    }

    private String normalizeId(String nodeId) {
        Objects.requireNonNull(
                nodeId,
                "Node ID cannot be null");

        String normalizedId = nodeId.trim();

        if (normalizedId.isBlank()) {
            throw new IllegalArgumentException(
                    "Node ID cannot be blank");
        }

        return normalizedId;
    }
}