import { ValidationFailure } from "./errors.js";

export function validateBody(schema, body) {
  const result = schema.safeParse(body);
  if (result.success) return result.data;
  throw new ValidationFailure(validationDetails(result.error.issues));
}

function validationDetails(issues) {
  return issues.flatMap((issue) => {
    if (issue.code === "unrecognized_keys") {
      return issue.keys.map((key) => ({ field: key, message: issue.message }));
    }
    return [{ field: issue.path.join(".") || "body", message: issue.message }];
  });
}

export function validationError(details) {
  return { error: "Validation failed", details };
}

export function isMalformedJson(error) {
  return error instanceof SyntaxError && "body" in error;
}

export function malformedJsonDetail(error) {
  return { field: "body", message: error.message };
}
