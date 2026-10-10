package com.optiflow.rotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RotationAlgorithmTest {

    static Stream<Arguments> survivorOracles() {
        return Stream.of(
                Arguments.of(5, 2, 3),
                Arguments.of(7, 3, 4),
                Arguments.of(2, 1, 2),
                Arguments.of(10, 4, 5),
                Arguments.of(41, 3, 31),
                Arguments.of(100, 7, 50));
    }

    static Stream<Arguments> moreSurvivorOracles() {
        return Stream.of(
                Arguments.of(1, 1, 1),
                Arguments.of(1, 5, 1),
                Arguments.of(2, 2, 1),
                Arguments.of(3, 1, 3),
                Arguments.of(3, 2, 3),
                Arguments.of(3, 3, 2),
                Arguments.of(4, 2, 1),
                Arguments.of(6, 2, 5),
                Arguments.of(6, 3, 1),
                Arguments.of(8, 3, 7),
                Arguments.of(9, 5, 8),
                Arguments.of(12, 4, 1),
                Arguments.of(15, 7, 5),
                Arguments.of(20, 2, 9),
                Arguments.of(25, 3, 14),
                Arguments.of(50, 1, 50),
                Arguments.of(64, 2, 1),
                Arguments.of(100, 2, 73),
                Arguments.of(1000, 2, 977),
                Arguments.of(1000, 3, 604),
                Arguments.of(1000, 999, 334),
                Arguments.of(1000, 1000, 609),
                Arguments.of(1000, 1001, 154),
                Arguments.of(5000, 7, 4173),
                Arguments.of(100000, 3, 92620));
    }

    @ParameterizedTest
    @MethodSource("survivorOracles")
    void findSurvivorOracles(int poolSize, int rotationStep, int expected) {
        assertEquals(expected, RotationService.findSurvivor(poolSize, rotationStep));
    }

    @ParameterizedTest
    @MethodSource("moreSurvivorOracles")
    void findSurvivorExtendedOracles(int poolSize, int rotationStep, int expected) {
        assertEquals(expected, RotationService.findSurvivor(poolSize, rotationStep));
    }

    private static int simulateSurvivor(int poolSize, int rotationStep) {
        List<Integer> ring = new ArrayList<>();
        for (int i = 1; i <= poolSize; i++) {
            ring.add(i);
        }
        int index = 0;
        while (ring.size() > 1) {
            index = (index + rotationStep - 1) % ring.size();
            ring.remove(index);
        }
        return ring.get(0);
    }

    @Test
    void matchesADirectEliminationSimulation() {
        for (int n = 1; n <= 60; n++) {
            for (int k = 1; k <= 15; k++) {
                assertEquals(simulateSurvivor(n, k), RotationService.findSurvivor(n, k), "n=" + n + " k=" + k);
            }
        }
    }

    @Test
    void kEqualOneAlwaysLeavesTheLastServer() {
        for (int n = 1; n <= 200; n++) {
            assertEquals(n, RotationService.findSurvivor(n, 1));
        }
    }

    @Test
    void aSingleServerAlwaysSurvives() {
        for (int k = 1; k <= 50; k++) {
            assertEquals(1, RotationService.findSurvivor(1, k));
        }
    }

    @Test
    void theSurvivorIsAlwaysWithinOneToN() {
        for (int n = 1; n <= 80; n++) {
            for (int k = 1; k <= 20; k++) {
                int survivor = RotationService.findSurvivor(n, k);
                assertTrue(survivor >= 1 && survivor <= n, "n=" + n + " k=" + k + " got " + survivor);
            }
        }
    }

    @Test
    void kEqualTwoSurvivorFollowsThePowerOfTwoFormula() {
        for (int n = 1; n <= 500; n++) {
            int highestPowerOfTwo = Integer.highestOneBit(n);
            assertEquals(2 * (n - highestPowerOfTwo) + 1, RotationService.findSurvivor(n, 2), "n=" + n);
        }
    }
}
