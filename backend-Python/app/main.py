from fastapi import FastAPI

from .features.capacity.controller import router as capacity_router
from .features.config_diffs.controller import router as config_diffs_router
from .features.rotations.controller import router as rotations_router
from .shared.error_handlers import register_error_handlers


def create_app() -> FastAPI:
    app = FastAPI(title="OptiFlow API (Python backend)")
    register_error_handlers(app)
    app.include_router(rotations_router)
    app.include_router(config_diffs_router)
    app.include_router(capacity_router)

    @app.get("/health")
    def health():
        return {"status": "ok", "backend": "python"}

    return app


app = create_app()
