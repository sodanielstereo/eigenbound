package com.eigenbound.application.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.challenge.ChallengeResult;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

/**
 * Maintains the state of a player's current challenge attempt.
 *
 * The mathematical rules remain inside {@link VectorChallenge}; this class
 * manages the sequence of movements selected by the player.
 */
public final class ChallengeSession {

    private final VectorChallenge challenge;
    private final List<Vector2> selectedMoves;

    /**
     * Creates an empty session for a vector challenge.
     *
     * @param challenge challenge played during this session
     */
    public ChallengeSession(VectorChallenge challenge) {
        this.challenge = Objects.requireNonNull(
                challenge,
                "Challenge cannot be null");
        this.selectedMoves = new ArrayList<>();
    }

    /**
     * Returns the challenge associated with this session.
     */
    public VectorChallenge challenge() {
        return challenge;
    }

    /**
     * Returns an immutable snapshot of the selected movements.
     */
    public List<Vector2> selectedMoves() {
        return List.copyOf(selectedMoves);
    }

    /**
     * Calculates the position reached with the currently selected movements.
     */
    public Vector2 currentPosition() {
        Vector2 position = challenge.start();

        for (Vector2 movement : selectedMoves) {
            position = position.add(movement);
        }

        return position;
    }

    /**
     * Returns the number of movements that can still be selected.
     */
    public int remainingSteps() {
        return challenge.maxSteps() - selectedMoves.size();
    }

    /**
     * Indicates whether the last movement can be removed.
     */
    public boolean canUndo() {
        return !selectedMoves.isEmpty();
    }

    /**
     * Adds an available movement to the current attempt.
     *
     * @param movement movement selected by the player
     */
    public void selectMove(Vector2 movement) {
        Objects.requireNonNull(
                movement,
                "Movement cannot be null");

        if (!challenge.availableMoves().contains(movement)) {
            throw new IllegalArgumentException(
                    "Movement is not available in this challenge");
        }

        if (remainingSteps() == 0) {
            throw new IllegalStateException(
                    "Maximum number of movements reached");
        }

        selectedMoves.add(movement);
    }

    /**
     * Removes the most recently selected movement.
     *
     * Calling this method on an empty session has no effect.
     */
    public void undo() {
        if (!selectedMoves.isEmpty()) {
            selectedMoves.remove(selectedMoves.size() - 1);
        }
    }

    /**
     * Clears all selected movements.
     */
    public void reset() {
        selectedMoves.clear();
    }

    /**
     * Evaluates the current sequence of movements.
     */
    public ChallengeResult check() {
        return challenge.evaluate(selectedMoves);
    }
}