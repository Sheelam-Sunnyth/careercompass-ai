# Setup
Copy .env.example to .env, set strong secrets and a Gemini key, and use GEMINI_MODEL=gemini-3.5-flash-lite. Then run docker compose --env-file .env up --build. Flyway migrates MySQL automatically.
