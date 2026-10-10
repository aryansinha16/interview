export class NotFoundError extends Error {}

export class ValidationFailure extends Error {
  constructor(details) {
    super("Validation failed");
    this.details = details;
  }
}
