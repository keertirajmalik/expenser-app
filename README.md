# Expenser (Android)

Offline-first native Android expense tracker. Kotlin + Jetpack Compose + Room.
Ported from the `expenser` web app; runs fully on-device with no server and no auth.

See [CONTEXT.md](CONTEXT.md) for the domain model and [docs/adr](docs/adr) for
architecture decisions.

## Stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- Room (SQLite) for local storage
- Manual DI (`AppContainer` on the `Application`)
- minSdk / targetSdk 35 (Android 15)

## Build & run

This repo has no Gradle wrapper JAR committed. Open it in **Android Studio**
(Ladybug or newer); it will download the SDK, generate the wrapper, and sync.

Then either press **Run** on an Android 15 emulator/device, or from a terminal
once the wrapper exists:

```bash
./gradlew installDebug        # build + install on a connected device
./gradlew test                # run unit tests (money conversion)
```

If you have Gradle installed locally you can generate the wrapper first:

```bash
gradle wrapper --gradle-version 8.11.1
```

## v1 scope

Dashboard (total expense), Expenses (list + add/edit/delete), Categories
(list + add/edit/delete). Income, Investment, charts, and Excel import are
scaffolded in the data model but not yet exposed in the UI.
