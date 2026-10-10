import assert from "node:assert/strict";
import { test } from "node:test";
import { findSurvivor } from "../../../src/features/rotations/service.js";

const SURVIVOR_ORACLES = [
  [5, 2, 3],
  [7, 3, 4],
  [2, 1, 2],
  [10, 4, 5],
  [41, 3, 31],
  [100, 7, 50],
];

for (const [poolSize, rotationStep, expected] of SURVIVOR_ORACLES) {
  test(`find_survivor(${poolSize}, ${rotationStep}) === ${expected}`, () => {
    assert.equal(findSurvivor(poolSize, rotationStep), expected);
  });
}

const MORE_SURVIVOR_ORACLES = [[1, 1, 1], [1, 5, 1], [2, 2, 1], [3, 1, 3], [3, 2, 3], [3, 3, 2], [4, 2, 1], [6, 2, 5], [6, 3, 1], [8, 3, 7], [9, 5, 8], [12, 4, 1], [15, 7, 5], [20, 2, 9], [25, 3, 14], [50, 1, 50], [64, 2, 1], [100, 2, 73], [1000, 2, 977], [1000, 3, 604], [1000, 999, 334], [1000, 1000, 609], [1000, 1001, 154], [5000, 7, 4173], [100000, 3, 92620]];

for (const [poolSize, rotationStep, expected] of MORE_SURVIVOR_ORACLES) {
  test(`find_survivor(${poolSize}, ${rotationStep}) === ${expected} (extended)`, () => {
    assert.equal(findSurvivor(poolSize, rotationStep), expected);
  });
}

function simulateSurvivor(poolSize, rotationStep) {
  const ring = Array.from({ length: poolSize }, (_, i) => i + 1);
  let index = 0;
  while (ring.length > 1) {
    index = (index + rotationStep - 1) % ring.length;
    ring.splice(index, 1);
  }
  return ring[0];
}

test("matches a direct elimination simulation for every n in 1..60 and k in 1..15", () => {
  for (let n = 1; n <= 60; n++) {
    for (let k = 1; k <= 15; k++) {
      assert.equal(findSurvivor(n, k), simulateSurvivor(n, k), `n=${n} k=${k}`);
    }
  }
});

test("k = 1 always leaves the last server", () => {
  for (let n = 1; n <= 200; n++) assert.equal(findSurvivor(n, 1), n);
});

test("a single server always survives", () => {
  for (let k = 1; k <= 50; k++) assert.equal(findSurvivor(1, k), 1);
});

test("the survivor is always within 1..n", () => {
  for (let n = 1; n <= 80; n++) {
    for (let k = 1; k <= 20; k++) {
      const survivor = findSurvivor(n, k);
      assert.ok(survivor >= 1 && survivor <= n, `n=${n} k=${k} got ${survivor}`);
    }
  }
});

test("k = 2 survivor is 2 * (n - 2^floor(log2 n)) + 1", () => {
  for (let n = 1; n <= 500; n++) {
    const highestPowerOfTwo = 2 ** Math.floor(Math.log2(n));
    assert.equal(findSurvivor(n, 2), 2 * (n - highestPowerOfTwo) + 1, `n=${n}`);
  }
});
