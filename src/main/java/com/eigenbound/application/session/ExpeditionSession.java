package com.eigenbound.application.session;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;

/**
 * Maintains the state of a player's journey through an expedition map.
 *
 * <p>
 * The expedition graph remains immutable inside {@link ExpeditionMap}. This
 * class stores only the mutable progress of one run: the current room and the
 * rooms visited by the player.
 * </p>
 */
public final class ExpeditionSession {

    private final ExpeditionMap map;
    private final Set<String> visitedNodeIds;
    private String currentNodeId;

    /**
     * Starts a new session at the configured start node of the map.
     *
     * @param map expedition graph used by this run
     */
    public ExpeditionSession(ExpeditionMap map) {
        this.map = Objects.requireNonNull(
                map,
                "Expedition map cannot be null");
        this.currentNodeId = map.startNodeId();
        this.visitedNodeIds = new LinkedHashSet<>();
        this.visitedNodeIds.add(currentNodeId);
    }

    /**
     * Returns the immutable expedition graph used by this session.
     *
     * @return expedition map
     */
    public ExpeditionMap map() {
        return map;
    }

    /**
     * Returns the room currently occupied by the player.
     *
     * @return current expedition node
     */
    public ExpeditionNode currentNode() {
        return map.findNode(currentNodeId);
    }

    /**
     * Returns the rooms that can be selected from the current room.
     *
     * @return immutable list of directly connected rooms
     */
    public List<ExpeditionNode> availableNodes() {
        if (isCompleted()) {
            return List.of();
        }

        return map.neighborsOf(currentNodeId);
    }

    /**
     * Returns the rooms visited during this run in traversal order.
     *
     * @return immutable list of visited rooms
     */
    public List<ExpeditionNode> visitedNodes() {
        return visitedNodeIds.stream()
                .map(map::findNode)
                .toList();
    }

    /**
     * Determines whether a room has already been visited.
     *
     * @param nodeId room identifier
     * @return {@code true} when the room belongs to the recorded path
     */
    public boolean hasVisited(String nodeId) {
        ExpeditionNode node = map.findNode(nodeId);

        return visitedNodeIds.contains(node.id());
    }

    /**
     * Determines whether the player can move directly to a room.
     *
     * @param destinationNodeId destination room identifier
     * @return {@code true} when a direct route exists and the run is active
     */
    public boolean canMoveTo(String destinationNodeId) {
        ExpeditionNode destination = map.findNode(destinationNodeId);

        return !isCompleted()
                && map.hasConnection(
                        currentNodeId,
                        destination.id());
    }

    /**
     * Moves the player through a direct outgoing connection.
     *
     * @param destinationNodeId destination room identifier
     * @throws IllegalStateException    when the expedition is already complete
     * @throws IllegalArgumentException when the destination is not directly
     *                                  connected to the current room
     */
    public void moveTo(String destinationNodeId) {
        if (isCompleted()) {
            throw new IllegalStateException(
                    "Completed expedition cannot accept more movements");
        }

        ExpeditionNode destination = map.findNode(destinationNodeId);

        if (!map.hasConnection(
                currentNodeId,
                destination.id())) {
            throw new IllegalArgumentException(
                    "Destination node is not available from the current room");
        }

        currentNodeId = destination.id();
        visitedNodeIds.add(currentNodeId);
    }

    /**
     * Indicates whether the player has reached the boss room.
     *
     * @return {@code true} when the current node is the configured boss node
     */
    public boolean isCompleted() {
        return currentNodeId.equals(map.bossNodeId());
    }
}