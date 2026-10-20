# Adding Features

1. Add a package under `feature/` for UI and presentation logic.
2. Keep reusable data models under `core/model/`.
3. Keep business rules under `domain/`.
4. Keep platform-specific code under `integration/`.
5. Do not put credentials in source code or logs.
6. Add tests and update documentation.

The current project uses one Gradle app module with separated Kotlin packages.
Gradle submodules can be introduced later when dependency boundaries are stable.
