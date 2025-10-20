# Resume Android App

This repository contains an Android app (Kotlin, Jetpack Compose) that displays a resume fetched from a backend API. It follows MVVM architecture and uses Retrofit + Coroutines for networking.

## What I added
- Jetpack Compose UI that shows name, title, summary, and a skills list.
- MVVM: `ResumeViewModel` exposes UI state.
- Networking: `ResumeRepository` uses Retrofit to call `GET /resume`.

## How to run
1. Set the backend URL in `gradle.properties` or rely on the defaults below.
2. Build the app:

```bash
./gradlew :app:assembleDebug
```

3. Install on a connected device or emulator:

```bash
./gradlew :app:installDebug
```

4. Open the app; press Load if it does not auto-load.

## API contract
GET `${BASE_URL}resume` returns JSON shaped like:

```json
{
  "name": "Santosh Example",
  "title": "Android Developer",
  "summary": "A concise summary about me.",
  "skills": [
    { "name": "Kotlin", "level": "Expert" },
    { "name": "Jetpack Compose", "level": "Advanced" }
  ]
}
```

## Configuration — BACKEND URL and build types
This project exposes the backend base URL to the app as a `BuildConfig` field named `BASE_URL`.

Where the URL comes from
- `debug` buildType (used during local development on the Android emulator) is set to:
  - `http://10.0.2.2:3000/` — update the port to match your local backend.
- `staging` buildType is set to `STAGING_BASE_URL` if present in `gradle.properties`, otherwise it falls back to `https://staging-api.yourdomain.com/`.
- `release` buildType reads the production URL from the `PROD_BASE_URL` Gradle property if set; otherwise it uses `https://api.yourdomain.com/`.

Where to set the production/staging URL
- Edit `gradle.properties` at the project root and add or change:

```ini
PROD_BASE_URL=https\://api.yourdomain.com/
# STAGING_BASE_URL=https\://staging-api.yourdomain.com/
```

Overriding at build time (example)
- Pass a property on the Gradle command line to override `PROD_BASE_URL` for a single build:

```bash
./gradlew :app:assembleRelease -PPROD_BASE_URL="https://api.myprodhost.com/"
```

Building specific variants
- Debug (local emulator):

```bash
./gradlew :app:assembleDebug
```

- Staging:

```bash
./gradlew :app:assembleStaging
```

- Release:

```bash
./gradlew :app:assembleRelease
```

Notes
- Emulator -> host machine mapping: use `10.0.2.2` for the default Android AVD.
- For a physical device on the same Wi‑Fi network, use your computer's LAN IP (e.g., `http://192.168.1.42:3000/`).
- For exposing a local server publicly for testing, use ngrok and point `PROD_BASE_URL` or `STAGING_BASE_URL` at the ngrok HTTPS URL.

Debug network security
- To avoid the "CLEARTEXT communication to 10.0.2.2 not permitted" error during local development, this project includes a debug-only network security configuration that permits cleartext to the emulator loopback address `10.0.2.2`.
- File: `app/src/main/res/xml/network_security_config.xml` and it's enabled via the debug manifest overlay at `app/src/debug/AndroidManifest.xml`.
- This is intentionally limited to debug builds only. Do not enable cleartext in release builds — use HTTPS for production APIs.

## Notes
- If you want me to create the GitHub repo for you from this environment I can attempt to use the GitHub CLI (if installed and authenticated). Otherwise follow the manual steps printed below after the script runs.

## License
Add a license if you want to make this public (e.g., MIT).
