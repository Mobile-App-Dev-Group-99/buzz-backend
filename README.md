# Buzz Backend

The backend starts PostgreSQL plus the auth, attendance, and safety services.

## Start

From this directory, run:

```powershell
docker compose up --build
```

Before the services start, Flyway applies the SQL files in `../buzz-database/db/migration` and records them in `flyway_schema_history`. An existing schema without Flyway history is baselined at version 9, so its data is preserved. The services then start on:

- Auth: `http://localhost:8081`
- Attendance: `http://localhost:8082`
- Safety: `http://localhost:8083`

Set `POSTGRES_USER`, `POSTGRES_PASSWORD`, and `JWT_SECRET` in `.env` before starting.
