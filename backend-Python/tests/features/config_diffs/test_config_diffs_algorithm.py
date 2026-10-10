import itertools

import pytest

from app.features.config_diffs.service import build_diff_details, compute_drift

SOURCE_CONFIG = "server_port=8080\ntimeout=30"
TARGET_CONFIG = "server_port=9090\ntimeout=60\nssl=true"

DRIFT_ORACLES = [
    ("kitten", "sitting", 3),
    ("", "", 0),
    ("abc", "abc", 0),
    ("a=1\nb=2\nc=3", "a=1\nc=3", 4),
    (SOURCE_CONFIG, TARGET_CONFIG, 12),
]

MORE_DRIFT_ORACLES = [
    ["", "", 0],
    ["", "abc", 3],
    ["abc", "", 3],
    ["a", "a", 0],
    ["a", "b", 1],
    ["ab", "ba", 2],
    ["abc", "yabd", 2],
    ["intention", "execution", 5],
    ["horse", "ros", 3],
    ["sunday", "saturday", 3],
    ["flaw", "lawn", 2],
    ["saturday", "sunday", 3],
    ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", 50],
    ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 100],
    ["abcdef", "azced", 3],
    ["kitten", "sitting", 3],
    ["été", "ete", 2],
    ["line1\nline2", "line1\nline3", 1],
    ["a=1\nb=2", "a=1\r\nb=2", 1],
    ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab", "baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 2],
]


@pytest.mark.parametrize(("source", "target", "expected"), DRIFT_ORACLES)
def test_compute_drift_oracles(source, target, expected):
    assert compute_drift(source, target) == expected


def test_the_documented_pair_has_exactly_one_replace_diff_entry():
    assert build_diff_details(SOURCE_CONFIG, TARGET_CONFIG) == [{"op": "replace", "line": "server_port"}]


def test_the_deletion_pair_reports_the_removed_line():
    assert build_diff_details("a=1\nb=2\nc=3", "a=1\nc=3") == [{"op": "delete", "line": "b"}]


def test_identical_configs_produce_no_diff_details():
    assert build_diff_details(SOURCE_CONFIG, SOURCE_CONFIG) == []


def test_an_added_line_is_reported_as_insert():
    assert build_diff_details("a=1", "a=1\nb=2") == [{"op": "insert", "line": "b"}]


@pytest.mark.parametrize(("source", "target", "expected"), MORE_DRIFT_ORACLES)
def test_compute_drift_extended_oracles(source, target, expected):
    assert compute_drift(source, target) == expected


def reference_distance(a, b):
    table = [[0] * (len(b) + 1) for _ in range(len(a) + 1)]
    for i in range(len(a) + 1):
        table[i][0] = i
    for j in range(len(b) + 1):
        table[0][j] = j
    for i in range(1, len(a) + 1):
        for j in range(1, len(b) + 1):
            table[i][j] = min(
                table[i - 1][j] + 1,
                table[i][j - 1] + 1,
                table[i - 1][j - 1] + (0 if a[i - 1] == b[j - 1] else 1),
            )
    return table[len(a)][len(b)]


def all_strings(alphabet, max_length):
    return ["".join(chars) for length in range(max_length + 1) for chars in itertools.product(alphabet, repeat=length)]


def test_matches_the_reference_table_for_every_string_pair_over_a_b_up_to_length_4():
    strings = all_strings("ab", 4)
    for a in strings:
        for b in strings:
            assert compute_drift(a, b) == reference_distance(a, b), (a, b)


def test_drift_is_symmetric():
    strings = all_strings("abc", 3)
    for a in strings:
        for b in strings:
            assert compute_drift(a, b) == compute_drift(b, a), (a, b)


def test_drift_is_zero_only_for_identical_strings():
    strings = all_strings("ab", 4)
    for a in strings:
        for b in strings:
            assert (compute_drift(a, b) == 0) == (a == b), (a, b)


def test_drift_satisfies_the_triangle_inequality():
    strings = all_strings("ab", 3)
    for a in strings:
        for b in strings:
            for c in strings:
                assert compute_drift(a, c) <= compute_drift(a, b) + compute_drift(b, c), (a, b, c)


def test_drift_is_bounded_by_the_longer_string_and_at_least_the_length_difference():
    strings = all_strings("ab", 4)
    for a in strings:
        for b in strings:
            drift = compute_drift(a, b)
            assert drift <= max(len(a), len(b)), (a, b)
            assert drift >= abs(len(a) - len(b)), (a, b)


def test_handles_long_inputs():
    assert compute_drift("a" * 1000, "a" * 1000) == 0
    assert compute_drift("a" * 1000, "") == 1000
    assert compute_drift("ab" * 300, "ba" * 300) == 2
