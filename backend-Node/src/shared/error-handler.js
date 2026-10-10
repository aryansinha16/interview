import { NotFoundError, ValidationFailure } from "./errors.js";
import { isMalformedJson, malformedJsonDetail, validationError } from "./validation.js";

export function errorHandler(err, req, res, next) {
  if (err instanceof NotFoundError) {
    res.status(404).json({ error: err.message });
  } else if (err instanceof ValidationFailure) {
    res.status(422).json(validationError(err.details));
  } else if (isMalformedJson(err)) {
    res.status(422).json(validationError([malformedJsonDetail(err)]));
  } else {
    next(err);
  }
}
