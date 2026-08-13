package com.eigenbound.application.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.generation.GeneratedVectorChallenge;
import com.eigenbound.domain.generation.VectorChallengeGenerator;
import com.eigenbound.domain.puzzle.cryptography.CaesarCipherPuzzleGenerator;
import com.eigenbound.domain.puzzle.generation.GeneratedMiniPuzzle;

class ExpeditionRoomEventTest {

    private static final long SEED = 91L;

    private final VectorChallengeGenerator vectorGenerator = new VectorChallengeGenerator();

    private final CaesarCipherPuzzleGenerator puzzleGenerator = new CaesarCipherPuzzleGenerator();

    @Test
    void shouldCreateVectorChallengeRoomEvent() {
        ExpeditionNode room = room(
                RoomType.VECTOR_CHALLENGE,
                2);
        GeneratedVectorChallenge generated = vectorGenerator.generate(SEED, 2);

        ExpeditionRoomEvent event = new VectorChallengeRoomEvent(
                room,
                generated);

        assertSame(room, event.room());
        assertEquals(RoomEventKind.VECTOR_CHALLENGE, event.kind());
        assertEquals(SEED, event.seed());
        assertEquals(2, event.difficulty());
        assertSame(
                generated,
                ((VectorChallengeRoomEvent) event).generatedChallenge());
    }

    @Test
    void shouldAllowEliteVectorChallengeEvent() {
        VectorChallengeRoomEvent event = new VectorChallengeRoomEvent(
                room(RoomType.ELITE_CHALLENGE, 4),
                vectorGenerator.generate(SEED, 4));

        assertEquals(4, event.difficulty());
    }

    @Test
    void shouldAllowBossVectorChallengeEvent() {
        VectorChallengeRoomEvent event = new VectorChallengeRoomEvent(
                room(RoomType.BOSS, 5),
                vectorGenerator.generate(SEED, 5));

        assertEquals(RoomType.BOSS, event.room().type());
    }

    @Test
    void shouldRejectVectorEventForRestRoom() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VectorChallengeRoomEvent(
                        room(RoomType.REST, 0),
                        vectorGenerator.generate(SEED, 2)));
    }

    @Test
    void shouldRejectNullVectorEventRoom() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorChallengeRoomEvent(
                        null,
                        vectorGenerator.generate(SEED, 2)));
    }

    @Test
    void shouldRejectNullGeneratedVectorChallenge() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorChallengeRoomEvent(
                        room(RoomType.VECTOR_CHALLENGE, 2),
                        null));
    }

    @Test
    void shouldCreateMiniPuzzleRoomEvent() {
        ExpeditionNode room = room(
                RoomType.REST,
                0);
        GeneratedMiniPuzzle generated = puzzleGenerator.generate(
                SEED,
                3);

        ExpeditionRoomEvent event = new MiniPuzzleRoomEvent(
                room,
                generated);

        assertSame(room, event.room());
        assertEquals(RoomEventKind.MINI_PUZZLE, event.kind());
        assertEquals(SEED, event.seed());
        assertEquals(3, event.difficulty());
        assertSame(
                generated,
                ((MiniPuzzleRoomEvent) event).generatedPuzzle());
    }

    @Test
    void shouldAllowRewardMiniPuzzleEvent() {
        MiniPuzzleRoomEvent event = new MiniPuzzleRoomEvent(
                room(RoomType.REWARD, 0),
                puzzleGenerator.generate(SEED, 2));

        assertEquals(RoomType.REWARD, event.room().type());
    }

    @Test
    void shouldRejectMiniPuzzleEventForVectorRoom() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MiniPuzzleRoomEvent(
                        room(RoomType.VECTOR_CHALLENGE, 2),
                        puzzleGenerator.generate(SEED, 2)));
    }

    @Test
    void shouldRejectNullMiniPuzzleEventRoom() {
        assertThrows(
                NullPointerException.class,
                () -> new MiniPuzzleRoomEvent(
                        null,
                        puzzleGenerator.generate(SEED, 2)));
    }

    @Test
    void shouldRejectNullGeneratedMiniPuzzle() {
        assertThrows(
                NullPointerException.class,
                () -> new MiniPuzzleRoomEvent(
                        room(RoomType.REWARD, 0),
                        null));
    }

    private ExpeditionNode room(
            RoomType type,
            int difficulty) {
        return new ExpeditionNode(
                "room-1-0",
                1,
                type,
                difficulty);
    }
}