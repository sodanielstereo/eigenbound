package com.eigenbound.domain.puzzle.generation;

import java.util.Objects;

import com.eigenbound.domain.puzzle.MiniPuzzle;

/**
 * Contains a procedurally generated mini-puzzle and its generation seed.
 *
 * @param puzzle generated and validated mini-puzzle
 * @param seed   seed used to produce the puzzle
 */
public record GeneratedMiniPuzzle(
        MiniPuzzle puzzle,
        long seed) {

    /**
     * Validates the generated puzzle container.
     */
    public GeneratedMiniPuzzle {
        Objects.requireNonNull(
                puzzle,
                "Generated mini-puzzle cannot be null");
    }
}