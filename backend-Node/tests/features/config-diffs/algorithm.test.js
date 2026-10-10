import assert from "node:assert/strict";
import { test } from "node:test";
import { buildDiffDetails, computeDrift } from "../../../src/features/config-diffs/service.js";

const SOURCE_CONFIG = "server_port=8080\ntimeout=30";
const TARGET_CONFIG = "server_port=9090\ntimeout=60\nssl=true";

const DRIFT_ORACLES = [
  ["kitten", "sitting", 3],
  ["", "", 0],
  ["abc", "abc", 0],
  ["a=1\nb=2\nc=3", "a=1\nc=3", 4],
  [SOURCE_CONFIG, TARGET_CONFIG, 12],
];

for (const [source, target, expected] of DRIFT_ORACLES) {
  test(`compute_drift(${JSON.stringify(source)}, ${JSON.stringify(target)}) === ${expected}`, () => {
    assert.equal(computeDrift(source, target), expected);
  });
}

test("the documented pair has exactly one replace diff entry", () => {
  assert.deepEqual(buildDiffDetails(SOURCE_CONFIG, TARGET_CONFIG), [
    { op: "replace", line: "server_port" },
  ]);
});

test("the deletion pair reports the removed line", () => {
  assert.deepEqual(buildDiffDetails("a=1\nb=2\nc=3", "a=1\nc=3"), [{ op: "delete", line: "b" }]);
});

test("identical configs produce no diff details", () => {
  assert.deepEqual(
    buildDiffDetails("server_port=8080\ntimeout=30", "server_port=8080\ntimeout=30"),
    [],
  );
});

test("an added line is reported as insert", () => {
  assert.deepEqual(buildDiffDetails("a=1", "a=1\nb=2"), [{ op: "insert", line: "b" }]);
});

const MORE_DRIFT_ORACLES = [["", "", 0], ["", "abc", 3], ["abc", "", 3], ["a", "a", 0], ["a", "b", 1], ["ab", "ba", 2], ["abc", "yabd", 2], ["intention", "execution", 5], ["horse", "ros", 3], ["sunday", "saturday", 3], ["flaw", "lawn", 2], ["saturday", "sunday", 3], ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", 50], ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 100], ["abcdef", "azced", 3], ["kitten", "sitting", 3], ["été", "ete", 2], ["line1\nline2", "line1\nline3", 1], ["a=1\nb=2", "a=1\r\nb=2", 1], ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab", "baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 2]];

for (const [source, target, expected] of MORE_DRIFT_ORACLES) {
  test(`compute_drift(${JSON.stringify(source).slice(0, 30)}, ${JSON.stringify(target).slice(0, 30)}) === ${expected} (extended)`, () => {
    assert.equal(computeDrift(source, target), expected);
  });
}

function referenceDistance(a, b) {
  const table = Array.from({ length: a.length + 1 }, () => new Array(b.length + 1).fill(0));
  for (let i = 0; i <= a.length; i++) table[i][0] = i;
  for (let j = 0; j <= b.length; j++) table[0][j] = j;
  for (let i = 1; i <= a.length; i++) {
    for (let j = 1; j <= b.length; j++) {
      table[i][j] = Math.min(
        table[i - 1][j] + 1,
        table[i][j - 1] + 1,
        table[i - 1][j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1),
      );
    }
  }
  return table[a.length][b.length];
}

function allStrings(alphabet, maxLength) {
  let all = [""];
  let layer = [""];
  for (let length = 1; length <= maxLength; length++) {
    layer = layer.flatMap((prefix) => [...alphabet].map((ch) => prefix + ch));
    all = all.concat(layer);
  }
  return all;
}

test("matches the reference table for every string pair over {a,b} up to length 4", () => {
  const strings = allStrings("ab", 4);
  for (const a of strings) {
    for (const b of strings) {
      assert.equal(computeDrift(a, b), referenceDistance(a, b), `${a} -> ${b}`);
    }
  }
});

test("drift is symmetric", () => {
  const strings = allStrings("abc", 3);
  for (const a of strings) {
    for (const b of strings) assert.equal(computeDrift(a, b), computeDrift(b, a), `${a} <-> ${b}`);
  }
});

test("drift is zero only for identical strings", () => {
  const strings = allStrings("ab", 4);
  for (const a of strings) {
    for (const b of strings) assert.equal(computeDrift(a, b) === 0, a === b, `${a} / ${b}`);
  }
});

test("drift satisfies the triangle inequality", () => {
  const strings = allStrings("ab", 3);
  for (const a of strings) {
    for (const b of strings) {
      for (const c of strings) {
        assert.ok(computeDrift(a, c) <= computeDrift(a, b) + computeDrift(b, c), `${a} ${b} ${c}`);
      }
    }
  }
});

test("drift is bounded by the longer string and at least the length difference", () => {
  const strings = allStrings("ab", 4);
  for (const a of strings) {
    for (const b of strings) {
      const drift = computeDrift(a, b);
      assert.ok(drift <= Math.max(a.length, b.length), `${a} / ${b}`);
      assert.ok(drift >= Math.abs(a.length - b.length), `${a} / ${b}`);
    }
  }
});

test("handles long inputs", () => {
  assert.equal(computeDrift("a".repeat(1000), "a".repeat(1000)), 0);
  assert.equal(computeDrift("a".repeat(1000), ""), 1000);
  assert.equal(computeDrift("ab".repeat(300), "ba".repeat(300)), 2);
});
