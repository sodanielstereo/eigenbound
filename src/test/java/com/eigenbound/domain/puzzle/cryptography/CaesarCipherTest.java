package com.eigenbound.domain.puzzle.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaesarCipherTest {

    private CaesarCipher cipher;

    @BeforeEach
    void setUp() {
        cipher = new CaesarCipher();
    }

    @Test
    void shouldEncryptKnownMessage() {
        assertEquals(
                "YHFWRU",
                cipher.encrypt(
                        "VECTOR",
                        3));
    }

    @Test
    void shouldDecryptKnownMessage() {
        assertEquals(
                "VECTOR",
                cipher.decrypt(
                        "YHFWRU",
                        3));
    }

    @Test
    void shouldWrapAroundAlphabet() {
        assertEquals(
                "ABC",
                cipher.encrypt(
                        "XYZ",
                        3));
    }

    @Test
    void shouldPreserveLetterCase() {
        assertEquals(
                "YhfWru",
                cipher.encrypt(
                        "VecTor",
                        3));
    }

    @Test
    void shouldPreserveSpacesNumbersAndPunctuation() {
        assertEquals(
                "YHFWRU 2.0!",
                cipher.encrypt(
                        "VECTOR 2.0!",
                        3));
    }

    @Test
    void shouldSupportNegativeShift() {
        assertEquals(
                "VECTOR",
                cipher.encrypt(
                        "YHFWRU",
                        -3));
    }

    @Test
    void shouldNormalizeShiftLargerThanAlphabet() {
        assertEquals(
                "YHFWRU",
                cipher.encrypt(
                        "VECTOR",
                        29));
    }

    @Test
    void shouldPreserveMessageForCompleteRotation() {
        assertEquals(
                "Vector",
                cipher.encrypt(
                        "Vector",
                        26));
    }

    @Test
    void shouldDecryptEveryTestedEncryption() {
        String message = "Eigenbound 2026!";

        for (int shift = -100; shift <= 100; shift++) {
            String encrypted = cipher.encrypt(
                    message,
                    shift);

            assertEquals(
                    message,
                    cipher.decrypt(
                            encrypted,
                            shift));
        }
    }

    @Test
    void shouldSupportMinimumIntegerShiftWithoutOverflow() {
        String encrypted = cipher.encrypt(
                "VECTOR",
                Integer.MIN_VALUE);

        assertEquals(
                "VECTOR",
                cipher.decrypt(
                        encrypted,
                        Integer.MIN_VALUE));
    }

    @Test
    void shouldSupportMaximumIntegerShift() {
        String encrypted = cipher.encrypt(
                "VECTOR",
                Integer.MAX_VALUE);

        assertEquals(
                "VECTOR",
                cipher.decrypt(
                        encrypted,
                        Integer.MAX_VALUE));
    }

    @Test
    void shouldAcceptEmptyMessage() {
        assertEquals(
                "",
                cipher.encrypt(
                        "",
                        3));
    }

    @Test
    void shouldRejectNullMessageWhenEncrypting() {
        assertThrows(
                NullPointerException.class,
                () -> cipher.encrypt(
                        null,
                        3));
    }

    @Test
    void shouldRejectNullMessageWhenDecrypting() {
        assertThrows(
                NullPointerException.class,
                () -> cipher.decrypt(
                        null,
                        3));
    }
}