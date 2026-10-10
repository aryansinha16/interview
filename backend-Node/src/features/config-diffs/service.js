import { newId } from "../../shared/db.js";
import { ConfigDiffDao } from "./dao.js";

const dao = new ConfigDiffDao();

export function computeDrift(source, target) {
  // TODO: implement by interviewee
  throw new Error("computeDrift not implemented");
}

export function buildDiffDetails(source, target) {
  return changedLines(splitLines(source), splitLines(target)).map((changed) => ({
    op: changed.op,
    line: lineLabel(changed.line),
  }));
}

function splitLines(text) {
  const lines = text.split(/\r\n|\r|\n/);
  if (lines.at(-1) === "") lines.pop();
  return lines;
}

function lineLabel(line) {
  return line.split("=")[0];
}

function changedLines(sourceLines, targetLines) {
  const m = sourceLines.length;
  const n = targetLines.length;
  const lcs = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
  for (let i = m - 1; i >= 0; i -= 1) {
    for (let j = n - 1; j >= 0; j -= 1) {
      lcs[i][j] = sourceLines[i] === targetLines[j]
        ? lcs[i + 1][j + 1] + 1
        : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
    }
  }

  const runs = [];
  let i = 0;
  let j = 0;
  while (i < m || j < n) {
    if (i < m && j < n && sourceLines[i] === targetLines[j]) {
      pushRun(runs, "equal", sourceLines[i]);
      i += 1;
      j += 1;
    } else if (j < n && (i === m || lcs[i + 1][j] < lcs[i][j + 1])) {
      pushRun(runs, "insert", targetLines[j]);
      j += 1;
    } else {
      pushRun(runs, "delete", sourceLines[i]);
      i += 1;
    }
  }

  const opcodes = [];
  for (let index = 0; index < runs.length; index += 1) {
    const run = runs[index];
    if (run.op === "equal") continue;
    const next = runs[index + 1];
    if (next !== undefined && next.op !== "equal") {
      opcodes.push({ op: "replace", line: next.op === "insert" ? next.line : run.line });
      index += 1;
      continue;
    }
    opcodes.push(run);
  }
  return opcodes;
}

function pushRun(runs, op, line) {
  if (runs.at(-1)?.op === op) return;
  runs.push({ op, line });
}

export function diffConfigs(body) {
  return dao.insert({
    id: newId("diff"),
    config_id: body.config_id,
    source_env: body.source_env,
    target_env: body.target_env,
    drift_score: computeDrift(body.source_config, body.target_config),
    diff_details: buildDiffDetails(body.source_config, body.target_config),
  });
}

export function listConfigDiffs(configId) {
  return dao.findAllNewestFirst({ config_id: configId });
}
