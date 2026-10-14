# Android Starter

A runnable, single-module Android starter with Compose/Material 3, Hilt, Navigation 3,
and a removable offline sample catalog. Persistent screen state and transient actions
follow the documented ViewModel-based MVI/UDF contract.

## Open and run

1. Open this repository directory in Android Studio (AGP 9.0-compatible or newer).
2. Use JDK 17+ (locally validated with Android Studio JBR 21), install SDK 36, and let
   Android Studio set `local.properties`. The Gradle wrapper supplies Gradle 9.1.0.
3. Sync, select the `app` run configuration and an API 26+ device/emulator, then Run.

The default build uses offline sample data. No account, API key, or server is required.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# With an emulator/device connected:
./gradlew :app:connectedDebugAndroidTest
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Release builds are
minified and unsigned; configure your own signing before distribution.

## Start a real product

Copy/clone this repository, rename its app identity, configure integrations, remove the
sample feature if unnecessary, and replace [PROJECT_CONTEXT](docs/PROJECT_CONTEXT.md).
Follow its [adoption workflow](docs/PROJECT_CONTEXT.md#clone-rename-configure).

Networking is wired with Retrofit, OkHttp, and kotlinx.serialization. To replace the
sample provider, pass `-Pstarter.apiBaseUrl=https://your-host.example/api/` when building,
or set that Gradle property in your user-level `~/.gradle/gradle.properties`. The URL
must use HTTPS and end in `/`. The sample API contract is documented in PROJECT_CONTEXT;
replace it for your backend. This URL is embedded in the app and must not contain secrets.
Run the UI tests in the default offline configuration.

## Documentation

| File | Role |
|---|---|
| [AGENTS.md](AGENTS.md) | Agent rules, selective context loading, prohibitions, validation |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Screen contract, boundaries, package placement, examples |
| [docs/PROJECT_CONTEXT.md](docs/PROJECT_CONTEXT.md) | Actual implementation, setup, validation, product template |
| [docs/DECISIONS.md](docs/DECISIONS.md) | Decision rationale and revisit conditions |

Repository documentation is authoritative. Refresh a documentation-only export with:

```sh
python3 scripts/export_docs.py /path/to/documentation/AndroidStarter
```

If reading an export, open the AndroidStarter source repository to run these commands.
