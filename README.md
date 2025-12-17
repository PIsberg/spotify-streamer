# Spotify Streamer

A music playing Android app that utilizes the Spotify Web API.

## Project Status
Migrated to **Gradle 8.7**, **AndroidX**, and **Retrofit 2**.

## Prerequisites
1.  **Java JDK 17** or higher.
2.  **Android SDK** (verified with compileSdk 34).
3.  **Spotify Developer Account** (for Client ID & Secret).

## Configuration

### 1. Android SDK Location
Create a file named `local.properties` in the root directory of the project if it doesn't exist. Add the path to your Android SDK:

**Windows:**
```properties
sdk.dir=C\:\\Users\\<USERNAME>\\AppData\\Local\\Android\\Sdk
```
*(Note the double backslashes and the escaped colon)*

**Mac/Linux:**
```properties
sdk.dir=/Users/<USERNAME>/Library/Android/sdk
```

### 2. Spotify Credentials
### 2. Spotify Credentials
This app uses the **Spotify Client Credentials Flow**.

1.  Create a file named `.env.local` in the root directory.
2.  Add your credentials in the following format:
    ```properties
    SPOTIFY_CLIENT_ID=your_client_id
    SPOTIFY_CLIENT_SECRET=your_client_secret
    ```
    *(This file is gitignored to protect your secrets)*

3.  Build the project. The build system will automatically inject these into the app.

## Building the Project

### Command Line
To build the debug APK:
```bash
./gradlew assembleDebug
```

The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Android Studio
1.  Open Android Studio.
2.  Select **File > Open** and select the project root directory.
3.  Sync Gradle (it should happen automatically).
4.  Run the `app` configuration.

## Features
- Search for Artists.
- View Top Tracks for an Artist.
- Play previews of tracks (Note: Spotify has deprecated 30s previews for some tracks/regions; playback might fail if preview URL is null).

## Troubleshooting
- **Build fails with "SDK location not found"**: Ensure `local.properties` exists and points to a valid SDK.
- **"method not found" or class issues**: Ensure you are doing a clean build: `./gradlew clean assembleDebug`.
- **Playback not working**: The `preview_url` field from Spotify API is deprecated and may return null.
