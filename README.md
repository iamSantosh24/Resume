# Resume Android App

This repository contains an Android app (Kotlin, Jetpack Compose) that displays a resume fetched from a backend API. It follows MVVM architecture and uses Retrofit + Coroutines for networking.

## What I added
- Jetpack Compose UI that shows name, title, summary, and a skills list.
- MVVM: `ResumeViewModel` exposes UI state.
- Networking: `ResumeRepository` uses Retrofit to call `GET /resume`.

## How to run
1. Set the backend URL in `app/src/main/java/com/example/resume/ResumeRepository.kt` (replace `BASE_URL`).
   - For local development with emulator, use `http://10.0.2.2:8000/`.
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

## Notes
- If you want me to create the GitHub repo for you from this environment I can attempt to use the GitHub CLI (if installed and authenticated). Otherwise follow the manual steps printed below after the script runs.

## License
Add a license if you want to make this public (e.g., MIT).

