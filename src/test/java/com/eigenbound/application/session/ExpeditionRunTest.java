package com.eigenbound.application.session;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.ExpeditionResources;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;

class ExpeditionRunTest {

        private ExpeditionMap map;
        private ExpeditionRun run;

        @BeforeEach
        void setUp() {
                map = createMap();
                run = new ExpeditionRun(
                                new GeneratedExpedition(
                                                map,
                                                73L,
                                                3));
        }

        @Test
        void shouldStartAtExpeditionStartNode() {
                assertEquals(
                                map.findNode("start"),
                                run.expeditionSession().currentNode());
        }

        @Test
        void shouldStartWithActiveRunState() {
                assertEquals(
                                ExpeditionRunState.ACTIVE,
                                run.state());
                assertFalse(run.isFinished());
        }

        @Test
        void shouldExposeOriginalGenerationSettings() {
                assertEquals(73L, run.seed());
                assertEquals(3, run.difficulty());
        }

        @Test
        void shouldStartWithInitialResources() {
                assertEquals(
                                ExpeditionResources.initial(),
                                run.resources());
                assertFalse(run.isFailed());
        }

        @Test
        void shouldLoseStabilityUsingImmutableSnapshots() {
                ExpeditionResources initialResources = run.resources();

                run.loseStability(25);

                assertEquals(75, run.resources().stability());
                assertEquals(100, initialResources.stability());
        }

        @Test
        void shouldRestoreStability() {
                run.loseStability(60);

                run.restoreStability(20);

                assertEquals(60, run.resources().stability());
        }

        @Test
        void shouldGainAndSpendInsight() {
                run.gainInsight(30L);
                run.spendInsight(12L);

                assertEquals(18L, run.resources().insight());
        }

        @Test
        void shouldFailWhenStabilityIsDepleted() {
                run.loseStability(100);

                assertTrue(run.isFailed());
                assertTrue(run.isFinished());
                assertEquals(
                                ExpeditionRunState.DEFEAT,
                                run.state());
                assertEquals(0, run.resources().stability());
        }

        @Test
        void shouldReachVictoryAfterCompletingBossRoom() {
                completeExpedition();

                assertEquals(
                                ExpeditionRunState.VICTORY,
                                run.state());
                assertTrue(run.isFinished());
                assertFalse(run.isFailed());
        }

        @Test
        void shouldRejectRoomSelectionAfterVictory() {
                completeExpedition();

                assertThrows(
                                IllegalStateException.class,
                                () -> run.selectRoom("boss"));
        }

        @Test
        void shouldRejectResourceChangesAfterVictory() {
                completeExpedition();

                assertThrows(
                                IllegalStateException.class,
                                () -> run.loseStability(1));
                assertThrows(
                                IllegalStateException.class,
                                () -> run.restoreStability(1));
                assertThrows(
                                IllegalStateException.class,
                                () -> run.gainInsight(1L));
                assertThrows(
                                IllegalStateException.class,
                                () -> run.spendInsight(0L));

                assertEquals(
                                ExpeditionResources.initial(),
                                run.resources());
        }

        @Test
        void shouldRejectRoomSelectionAfterFailure() {
                run.loseStability(100);

                assertThrows(
                                IllegalStateException.class,
                                () -> run.selectRoom("challenge"));
        }

        @Test
        void shouldRejectStabilityRestorationAfterFailure() {
                run.loseStability(100);

                assertThrows(
                                IllegalStateException.class,
                                () -> run.restoreStability(10));

                assertEquals(0, run.resources().stability());
        }

        @Test
        void shouldRejectPendingRoomCompletionAfterFailure() {
                run.selectRoom("challenge");
                run.loseStability(100);

                assertThrows(
                                IllegalStateException.class,
                                run::completePendingRoom);

                assertTrue(run.hasPendingRoom());
        }

        @Test
        void shouldStartWithoutPendingRoom() {
                assertTrue(run.pendingRoom().isEmpty());
                assertFalse(run.hasPendingRoom());
        }

        @Test
        void shouldSelectAvailableRoomWithoutMovingPlayer() {
                run.selectRoom("challenge");

                assertEquals(
                                map.findNode("challenge"),
                                run.pendingRoom().orElseThrow());
                assertEquals(
                                map.findNode("start"),
                                run.expeditionSession().currentNode());
        }

        @Test
        void shouldDeriveDeterministicSeedForPendingRoom() {
                run.selectRoom("challenge");

                long expectedSeed = 73L
                                ^ Integer.toUnsignedLong(
                                                "challenge".hashCode());

                assertEquals(
                                expectedSeed,
                                run.pendingChallengeSeed());
        }

        @Test
        void shouldDeriveDifferentSeedsForDifferentRooms() {
                run.selectRoom("challenge");
                long challengeSeed = run.pendingChallengeSeed();

                run.cancelPendingRoom();
                run.selectRoom("rest");

                assertNotEquals(
                                challengeSeed,
                                run.pendingChallengeSeed());
        }

        @Test
        void shouldRejectChallengeSeedWithoutPendingRoom() {
                assertThrows(
                                IllegalStateException.class,
                                run::pendingChallengeSeed);
        }

        @Test
        void shouldRejectUnavailableRoomSelection() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> run.selectRoom("boss"));

                assertFalse(run.hasPendingRoom());
        }

        @Test
        void shouldRejectAnotherSelectionWhileRoomIsPending() {
                run.selectRoom("challenge");

                assertThrows(
                                IllegalStateException.class,
                                () -> run.selectRoom("rest"));

                assertEquals(
                                map.findNode("challenge"),
                                run.pendingRoom().orElseThrow());
        }

        @Test
        void shouldCompletePendingRoomAndCommitMovement() {
                run.selectRoom("challenge");

                run.completePendingRoom();

                assertEquals(
                                map.findNode("challenge"),
                                run.expeditionSession().currentNode());
                assertFalse(run.hasPendingRoom());
        }

        @Test
        void shouldCancelPendingRoomWithoutMovingPlayer() {
                run.selectRoom("challenge");

                run.cancelPendingRoom();

                assertEquals(
                                map.findNode("start"),
                                run.expeditionSession().currentNode());
                assertFalse(run.hasPendingRoom());
        }

        @Test
        void shouldRejectCompletionWithoutPendingRoom() {
                assertThrows(
                                IllegalStateException.class,
                                run::completePendingRoom);
        }

        @Test
        void shouldRejectCancellationWithoutPendingRoom() {
                assertThrows(
                                IllegalStateException.class,
                                run::cancelPendingRoom);
        }

        @Test
        void shouldRejectNullRoomIdentifier() {
                assertThrows(
                                NullPointerException.class,
                                () -> run.selectRoom(null));
        }

        @Test
        void shouldRejectNullGeneratedExpedition() {
                assertThrows(
                                NullPointerException.class,
                                () -> new ExpeditionRun(null));
        }

        private void completeExpedition() {
                run.selectRoom("challenge");
                run.completePendingRoom();
                run.selectRoom("boss");
                run.completePendingRoom();
        }

        private ExpeditionMap createMap() {
                ExpeditionNode start = new ExpeditionNode(
                                "start",
                                0,
                                RoomType.START,
                                0);

                ExpeditionNode challenge = new ExpeditionNode(
                                "challenge",
                                1,
                                RoomType.VECTOR_CHALLENGE,
                                2);

                ExpeditionNode rest = new ExpeditionNode(
                                "rest",
                                1,
                                RoomType.REST,
                                0);

                ExpeditionNode boss = new ExpeditionNode(
                                "boss",
                                2,
                                RoomType.BOSS,
                                3);

                return new ExpeditionMap(
                                List.of(
                                                start,
                                                challenge,
                                                rest,
                                                boss),
                                List.of(
                                                new ExpeditionEdge(
                                                                "start",
                                                                "challenge"),
                                                new ExpeditionEdge(
                                                                "start",
                                                                "rest"),
                                                new ExpeditionEdge(
                                                                "challenge",
                                                                "boss"),
                                                new ExpeditionEdge(
                                                                "rest",
                                                                "boss")),
                                "start",
                                "boss");
        }
}