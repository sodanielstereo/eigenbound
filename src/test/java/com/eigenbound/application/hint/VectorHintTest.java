package com.eigenbound.application.hint;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.math.Vector2;

class VectorHintTest {

    @Test
    void shouldProtectMovementList() {
        List<Vector2> movements = new ArrayList<>();
        movements.add(new Vector2(2, 1));

        VectorHint hint = new VectorHint(
                HintLevel.NEXT_MOVE,
                "Try this movement.",
                movements);

        movements.clear();

        assertEquals(1, hint.movements().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> hint.movements()
                        .add(new Vector2(1, 2)));
    }

    @Test
    void shouldRejectNullLevel() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorHint(
                        null,
                        "Message",
                        List.of()));
    }

    @Test
    void shouldRejectBlankMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VectorHint(
                        HintLevel.CONCEPTUAL,
                        "   ",
                        List.of()));
    }

    @Test
    void shouldRejectNullMovements() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorHint(
                        HintLevel.CONCEPTUAL,
                        "Message",
                        null));
    }
}