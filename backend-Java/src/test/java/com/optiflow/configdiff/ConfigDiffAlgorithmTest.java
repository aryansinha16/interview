package com.optiflow.configdiff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigDiffAlgorithmTest {

    private static final String SOURCE_CONFIG = "server_port=8080\ntimeout=30";
    private static final String TARGET_CONFIG = "server_port=9090\ntimeout=60\nssl=true";

    static Stream<Arguments> driftOracles() {
        return Stream.of(
                Arguments.of("kitten", "sitting", 3),
                Arguments.of("", "", 0),
                Arguments.of("abc", "abc", 0),
                Arguments.of("a=1\nb=2\nc=3", "a=1\nc=3", 4),
                Arguments.of(SOURCE_CONFIG, TARGET_CONFIG, 12));
    }

    static Stream<Arguments> moreDriftOracles() {
        return Stream.of(
                Arguments.of("", "", 0),
                Arguments.of("", "abc", 3),
                Arguments.of("abc", "", 3),
                Arguments.of("a", "a", 0),
                Arguments.of("a", "b", 1),
                Arguments.of("ab", "ba", 2),
                Arguments.of("abc", "yabd", 2),
                Arguments.of("intention", "execution", 5),
                Arguments.of("horse", "ros", 3),
                Arguments.of("sunday", "saturday", 3),
                Arguments.of("flaw", "lawn", 2),
                Arguments.of("saturday", "sunday", 3),
                Arguments.of("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", 50),
                Arguments.of("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 100),
                Arguments.of("abcdef", "azced", 3),
                Arguments.of("kitten", "sitting", 3),
                Arguments.of("\u00e9t\u00e9", "ete", 2),
                Arguments.of("line1\nline2", "line1\nline3", 1),
                Arguments.of("a=1\nb=2", "a=1\r\nb=2", 1),
                Arguments.of("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab", "baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 2));
    }

    @ParameterizedTest
    @MethodSource("driftOracles")
    void computeDriftOracles(String source, String target, int expected) {
        assertEquals(expected, ConfigDiffService.computeDrift(source, target));
    }

    @Test
    void theDocumentedPairHasExactlyOneReplaceDiffEntry() {
        assertEquals(List.of(new DiffDetail("replace", "server_port")),
                ConfigDiffService.buildDiffDetails(SOURCE_CONFIG, TARGET_CONFIG));
    }

    @Test
    void theDeletionPairReportsTheRemovedLine() {
        assertEquals(List.of(new DiffDetail("delete", "b")),
                ConfigDiffService.buildDiffDetails("a=1\nb=2\nc=3", "a=1\nc=3"));
    }

    @Test
    void identicalConfigsProduceNoDiffDetails() {
        assertEquals(List.of(), ConfigDiffService.buildDiffDetails(SOURCE_CONFIG, SOURCE_CONFIG));
    }

    @Test
    void anAddedLineIsReportedAsInsert() {
        assertEquals(List.of(new DiffDetail("insert", "b")),
                ConfigDiffService.buildDiffDetails("a=1", "a=1\nb=2"));
    }

    @ParameterizedTest
    @MethodSource("moreDriftOracles")
    void computeDriftExtendedOracles(String source, String target, int expected) {
        assertEquals(expected, ConfigDiffService.computeDrift(source, target));
    }

    private static int referenceDistance(String a, String b) {
        int[][] table = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            table[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            table[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                table[i][j] = Math.min(Math.min(table[i - 1][j] + 1, table[i][j - 1] + 1),
                        table[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
            }
        }
        return table[a.length()][b.length()];
    }

    private static List<String> allStrings(String alphabet, int maxLength) {
        List<String> all = new ArrayList<>(List.of(""));
        List<String> layer = List.of("");
        for (int length = 1; length <= maxLength; length++) {
            List<String> next = new ArrayList<>();
            for (String prefix : layer) {
                for (char ch : alphabet.toCharArray()) {
                    next.add(prefix + ch);
                }
            }
            layer = next;
            all.addAll(layer);
        }
        return all;
    }

    @Test
    void matchesTheReferenceTableForEveryStringPairOverABUpToLength4() {
        List<String> strings = allStrings("ab", 4);
        for (String a : strings) {
            for (String b : strings) {
                assertEquals(referenceDistance(a, b), ConfigDiffService.computeDrift(a, b), a + " -> " + b);
            }
        }
    }

    @Test
    void driftIsSymmetric() {
        List<String> strings = allStrings("abc", 3);
        for (String a : strings) {
            for (String b : strings) {
                assertEquals(ConfigDiffService.computeDrift(a, b), ConfigDiffService.computeDrift(b, a), a + " <-> " + b);
            }
        }
    }

    @Test
    void driftIsZeroOnlyForIdenticalStrings() {
        List<String> strings = allStrings("ab", 4);
        for (String a : strings) {
            for (String b : strings) {
                assertEquals(a.equals(b), ConfigDiffService.computeDrift(a, b) == 0, a + " / " + b);
            }
        }
    }

    @Test
    void driftSatisfiesTheTriangleInequality() {
        List<String> strings = allStrings("ab", 3);
        for (String a : strings) {
            for (String b : strings) {
                for (String c : strings) {
                    assertTrue(ConfigDiffService.computeDrift(a, c)
                            <= ConfigDiffService.computeDrift(a, b) + ConfigDiffService.computeDrift(b, c),
                            a + " " + b + " " + c);
                }
            }
        }
    }

    @Test
    void driftIsBoundedByTheLongerStringAndAtLeastTheLengthDifference() {
        List<String> strings = allStrings("ab", 4);
        for (String a : strings) {
            for (String b : strings) {
                int drift = ConfigDiffService.computeDrift(a, b);
                assertTrue(drift <= Math.max(a.length(), b.length()), a + " / " + b);
                assertTrue(drift >= Math.abs(a.length() - b.length()), a + " / " + b);
            }
        }
    }

    @Test
    void handlesLongInputs() {
        assertEquals(0, ConfigDiffService.computeDrift("a".repeat(1000), "a".repeat(1000)));
        assertEquals(1000, ConfigDiffService.computeDrift("a".repeat(1000), ""));
        assertEquals(2, ConfigDiffService.computeDrift("ab".repeat(300), "ba".repeat(300)));
    }
}
