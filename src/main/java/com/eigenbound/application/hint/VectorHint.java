package com.eigenbound.application.hint;

import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.math.Vector2;

public record VectorHint(
        HintLevel level,
        String message,
        List<Vector2> movements) {

    public VectorHint {
        Objects.requireNonNull(
                level,
                "Hint level cannot be null");
        Objects.requireNonNull(
                message,
                "Hint message cannot be null");
        Objects.requireNonNull(
                movements,
                "Hint movements cannot be null");

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "Hint message cannot be blank");
        }

        movements = List.copyOf(movements);
    }
}