# frontoffice_travagir (JavaFX)

User-facing Frontoffice UI for Travagir.

## Prerequisites
- Java 21
- Backend running on `http://localhost:8080`

## Run
From the repo root, using the existing backend Maven wrapper:

```powershell
\backend-Travagir\mvnw.cmd -f .\frontoffice\frontoffice_travagir\pom.xml javafx:run
```

If you have Maven installed globally:

```powershell
mvn -f .\frontoffice\frontoffice_travagir\pom.xml javafx:run
```

## Common issue
- `gradlew` is for the **backoffice** (Gradle project in `backoffice/backoffice_travagir/`).
- The **frontoffice** is a **Maven** project, so use `mvnw.cmd` or `mvn`.

## Notes
- Login: `POST /api/v1/users/login`
- Register: `POST /api/v1/users/create`
- Voyages: `GET /api/v1/voyages`
- Offers: `GET /api/v1/offers`
- My reservations: `GET /api/v1/reservations/my?userId=...` (requires Bearer token)
