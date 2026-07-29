package com.eigenbound.domain.expedition;

import java.util.List;

/**
 * Shared expedition maps used by unit tests.
 */
public final class ExpeditionMapTestFixtures {

    private ExpeditionMapTestFixtures() {
    }

    public static ExpeditionMap validMap() {
        ExpeditionNode start = new ExpeditionNode(
                "start",
                0,
                RoomType.START,
                0);

        ExpeditionNode challenge = new ExpeditionNode(
                "challenge",
                1,
                RoomType.VECTOR_CHALLENGE,
                1);

        ExpeditionNode boss = new ExpeditionNode(
                "boss",
                2,
                RoomType.BOSS,
                2);

        return new ExpeditionMap(
                List.of(
                        start,
                        challenge,
                        boss),
                List.of(
                        new ExpeditionEdge(
                                "start",
                                "challenge"),
                        new ExpeditionEdge(
                                "challenge",
                                "boss")),
                "start",
                "boss");
    }
}