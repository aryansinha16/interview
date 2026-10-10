from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from .errors import NotFoundError


def register_error_handlers(app: FastAPI) -> None:
    @app.exception_handler(RequestValidationError)
    async def validation_error_handler(_: Request, exc: RequestValidationError) -> JSONResponse:
        details = []
        for error in exc.errors():
            loc = error["loc"]
            if error["type"] == "json_invalid":
                field = "body"
            else:
                field = ".".join(str(part) for part in loc[1:]) or "body"
            details.append({"field": field, "message": error["msg"]})
        return JSONResponse(
            status_code=422, content={"error": "Validation failed", "details": details}
        )

    @app.exception_handler(NotFoundError)
    async def not_found_handler(_: Request, exc: NotFoundError) -> JSONResponse:
        return JSONResponse(status_code=404, content={"error": str(exc)})
