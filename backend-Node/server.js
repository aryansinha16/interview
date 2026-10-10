import express from "express";
import { capacityRouter } from "./src/features/capacity/controller.js";
import { CapacitySimulationDao } from "./src/features/capacity/dao.js";
import { configDiffsRouter } from "./src/features/config-diffs/controller.js";
import { ConfigDiffDao } from "./src/features/config-diffs/dao.js";
import { healthRouter } from "./src/features/health/controller.js";
import { rotationsRouter } from "./src/features/rotations/controller.js";
import { RotationDao } from "./src/features/rotations/dao.js";
import { ensureDataFile } from "./src/shared/db.js";
import { errorHandler } from "./src/shared/error-handler.js";

const PORT = 3000;
const API = "/api/v1";

const DATA_FILES = [
  RotationDao.file,
  ConfigDiffDao.file,
  CapacitySimulationDao.file,
];

await Promise.all(DATA_FILES.map(ensureDataFile));

const app = express();
app.use(express.json());
app.use(healthRouter());
app.use(API, rotationsRouter());
app.use(API, configDiffsRouter());
app.use(API, capacityRouter());
app.use(errorHandler);

app.listen(PORT, () => {
  console.log(`OptiFlow node backend listening on http://localhost:${PORT}`);
});
