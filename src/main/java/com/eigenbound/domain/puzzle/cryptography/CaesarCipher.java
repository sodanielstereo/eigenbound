package com.eigenbound.domain.puzzle.cryptography;

import java.util.Objects;

/**
 * Applies the classical Caesar substitution cipher to ASCII letters.
 *
 * <p>
 * The cipher rotates letters through an alphabet of twenty-six characters,
 * preserves letter case and leaves spaces, numbers and punctuation unchanged.
 * It is included for educational puzzles and must not be considered secure
 * modern cryptography.
 * </p>
 */
public final class CaesarCipher {

    private static final int ALPHABET_SIZE = 26;

    /**
     * Creates a reusable Caesar cipher.
     */
    public CaesarCipher() {
    }

    /**
     * Encrypts text by rotating every ASCII letter.
     *
     * @param message text that must be encrypted
     * @param shift   number of alphabet positions used by the rotation
     * @return encrypted text
     */
    public String encrypt(
            String message,
            int shift) {
        Objects.requireNonNull(
                message,
                "Message cannot be null");

        return transform(
                message,
                Math.floorMod(
                        shift,
                        ALPHABET_SIZE));
    }

    /**
     * Decrypts text by applying the inverse alphabet rotation.
     *
     * <p>
     * The inverse is calculated from the normalized shift instead of directly
     * negating the original value. This also works for
     * {@link Integer#MIN_VALUE}, whose direct negation would overflow.
     * </p>
     *
     * @param message text that must be decrypted
     * @param shift   rotation originally used during encryption
     * @return decrypted text
     */
    public String decrypt(
            String message,
            int shift) {
        Objects.requireNonNull(
                message,
                "Message cannot be null");

        int normalizedShift = Math.floorMod(
                shift,
                ALPHABET_SIZE);

        int inverseShift = normalizedShift == 0
                ? 0
                : ALPHABET_SIZE - normalizedShift;

        return transform(
                message,
                inverseShift);
    }

    /**
     * Applies an already normalized shift to every character.
     */
    private String transform(
            String message,
            int normalizedShift) {
        StringBuilder transformed = new StringBuilder(
                message.length());

        for (int index = 0; index < message.length(); index++) {
            transformed.append(
                    rotateCharacter(
                            message.charAt(index),
                            normalizedShift));
        }

        return transformed.toString();
    }

    /**
     * Rotates an ASCII letter and leaves every other character unchanged.
     */
    private char rotateCharacter(
            char character,
            int normalizedShift) {
        if (character >= 'A'
                && character <= 'Z') {
            return rotateFromBase(
                    character,
                    'A',
                    normalizedShift);
        }

        if (character >= 'a'
                && character <= 'z') {
            return rotateFromBase(
                    character,
                    'a',
                    normalizedShift);
        }

        return character;
    }

    /**
     * Rotates a character relative to its uppercase or lowercase alphabet base.
     */
    private char rotateFromBase(
            char character,
            char alphabetBase,
            int normalizedShift) {
        int alphabetIndex = character - alphabetBase;

        int rotatedIndex = Math.floorMod(
                alphabetIndex + normalizedShift,
                ALPHABET_SIZE);

        return (char) (alphabetBase + rotatedIndex);
    }
}