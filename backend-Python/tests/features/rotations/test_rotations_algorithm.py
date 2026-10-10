import pytest

from app.features.rotations.service import find_survivor

SURVIVOR_ORACLES = [
    [5, 2, 3],
    [7, 3, 4],
    [2, 1, 2],
    [10, 4, 5],
    [41, 3, 31],
    [100, 7, 50],
]

MORE_SURVIVOR_ORACLES = [
    [1, 1, 1],
    [1, 5, 1],
    [2, 2, 1],
    [3, 1, 3],
    [3, 2, 3],
    [3, 3, 2],
    [4, 2, 1],
    [6, 2, 5],
    [6, 3, 1],
    [8, 3, 7],
    [9, 5, 8],
    [12, 4, 1],
    [15, 7, 5],
    [20, 2, 9],
    [25, 3, 14],
    [50, 1, 50],
    [64, 2, 1],
    [100, 2, 73],
    [1000, 2, 977],
    [1000, 3, 604],
    [1000, 999, 334],
    [1000, 1000, 609],
    [1000, 1001, 154],
    [5000, 7, 4173],
    [100000, 3, 92620],
]


@pytest.mark.parametrize(("pool_size", "rotation_step", "expected"), SURVIVOR_ORACLES)
def test_find_survivor_oracles(pool_size, rotation_step, expected):
    assert find_survivor(pool_size, rotation_step) == expected


@pytest.mark.parametrize(("pool_size", "rotation_step", "expected"), MORE_SURVIVOR_ORACLES)
def test_find_survivor_extended_oracles(pool_size, rotation_step, expected):
    assert find_survivor(pool_size, rotation_step) == expected


def simulate_survivor(pool_size, rotation_step):
    ring = list(range(1, pool_size + 1))
    index = 0
    while len(ring) > 1:
        index = (index + rotation_step - 1) % len(ring)
        ring.pop(index)
    return ring[0]


def test_matches_a_direct_elimination_simulation():
    for n in range(1, 61):
        for k in range(1, 16):
            assert find_survivor(n, k) == simulate_survivor(n, k), (n, k)


def test_k_equal_one_always_leaves_the_last_server():
    for n in range(1, 201):
        assert find_survivor(n, 1) == n


def test_a_single_server_always_survives():
    for k in range(1, 51):
        assert find_survivor(1, k) == 1


def test_the_survivor_is_always_within_one_to_n():
    for n in range(1, 81):
        for k in range(1, 21):
            assert 1 <= find_survivor(n, k) <= n, (n, k)


def test_k_equal_two_survivor_follows_the_power_of_two_formula():
    for n in range(1, 501):
        highest_power_of_two = 2 ** (n.bit_length() - 1)
        assert find_survivor(n, 2) == 2 * (n - highest_power_of_two) + 1, n
