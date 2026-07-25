package com.eigenbound.domain.challenge;

/**
 * Possible outcomes of a vector challenge attempt.
 */
public enum ChallengeStatus {
    SOLVED,
    INCOMPLETE,
    INVALID_MOVE,
    STEP_LIMIT_EXCEEDED
}