package com.eigenbound.domain.puzzle.generation;

import com.eigenbound.domain.puzzle.PuzzleTopic;

/**
 * Defines a deterministic generator of educational mini-puzzles.
 *
 * <p>
 * Implementations must produce the same puzzle when given the same seed and
 * difficulty. This makes procedural content reproducible during testing and
 * bug investigation.
 * </p>
 */
public interface MiniPuzzleGenerator {

    /**
     * Returns the academic topic produced by this generator.
     *
     * @return generator puzzle topic
     */
    PuzzleTopic topic();

    /**
     * Generates a validated mini-puzzle.
     *
     * @param seed       deterministic generation seed
     * @param difficulty puzzle difficulty from one to five
     * @return generated puzzle and its original seed
     */
    GeneratedMiniPuzzle generate(
            long seed,
            int difficulty);
}