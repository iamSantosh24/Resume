# Deploying the admin server (Option B: environment variable)

This document explains the recommended Option B: store the Firebase service account JSON outside the repo and point the server at it with an environment variable (safer for CI/deploy).

Steps (zsh / macOS)

1) Download the service account JSON
- In the Firebase Console: Project Settings -> Service accounts -> Generate new private key.
- The file will be downloaded (e.g. `project-...-firebase-adminsdk-xxxxx.json`).

2) Move the key to a secure location and restrict permissions
```bash
mkdir -p ~/.secrets
mv ~/Downloads/your-service-account-file.json ~/.secrets/resume-service-account.json
chmod 600 ~/.secrets/resume-service-account.json
```

3) Point the server to the key using an environment variable (current shell)
```bash
export SERVICE_ACCOUNT_PATH="$HOME/.secrets/resume-service-account.json"
# or use the other supported name:
# export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.secrets/resume-service-account.json"
```

4) Persist the variable in your interactive shell (optional)
- Add the export line to `~/.zshrc` (or to your CI environment variables instead of committing):
```bash
echo 'export SERVICE_ACCOUNT_PATH="$HOME/.secrets/resume-service-account.json"' >> ~/.zshrc
# then reload
source ~/.zshrc
```

IMPORTANT: do not commit the secret file or your updated dotfiles to source control. Use CI secrets for deployments.

5) (Optional) Set the database URL if needed
```bash
export FIREBASE_DATABASE_URL="https://resume-b707f-default-rtdb.firebaseio.com"
```

6) Start the admin server
```bash
cd /path/to/Resume/server-admin
npm start
```

7) Verify Admin SDK is initialized and server reads/writes via Admin SDK
- Health endpoint (shows adminInitialized: true when Admin SDK is used):
```bash
curl http://localhost:4000/health
# expected: { "ok": true, "adminInitialized": true }
```
- Read resume (server reads from RTDB path `users/myResumeProfile` when admin is enabled):
```bash
curl http://localhost:4000/resume | jq .
```
- Write test resume (server will write to RTDB via Admin SDK):
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"personalInfo":{"name":"Test User","email":"test@example.com"}}' \
  http://localhost:4000/resume
# then GET /resume to confirm
curl http://localhost:4000/resume | jq .
```

Troubleshooting
- If `/health` shows `adminInitialized: false`:
  - Confirm the `SERVICE_ACCOUNT_PATH` path is correct and readable by the process user.
  - Confirm the JSON file is valid (not truncated)
  - Check the server logs for an initialization error printed by `tryInitAdmin()`.
- If read/write fails after admin initializes:
  - Confirm `FIREBASE_DATABASE_URL` matches the project for the service account.
  - Ensure the service account has necessary Realtime Database permissions (use least-privilege roles in production).

Security notes
- Never commit the service account JSON to git. Keep it outside the repo and add `server-admin/serviceAccountKey.json` to `.gitignore` (already present in this repo).
- Use CI secret variables instead of adding keys to dotfiles in CI/CD.
- Rotate the key if it is ever exposed.

If you'd like, I can also:
- Add a one-line script to start the server with the env var in-place (for dev), or
- Help you configure CI (GitHub Actions, etc.) to inject the secret and run the server.

