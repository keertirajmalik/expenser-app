# Native Android rewrite instead of reusing the React frontend

The web app is React + a Go/Postgres server. To ship an offline Android app we
rewrite the UI natively in Kotlin/Compose with a local Room database, rather than
wrapping the existing React build in a WebView (Capacitor) or compiling the Go
server for mobile (gomobile). We accept throwing away frontend reuse in exchange
for a native experience, no WebView/JS bridge, and no attempt to run pgx/JWT on
device. The web app and Go server are left untouched as a separate project.

## Considered Options

- **Capacitor (WebView + reuse React):** fastest to ship, one shim behind
  `apiRequest` over local SQLite — rejected because the user wants a true native
  app, not a wrapped web view.
- **gomobile / compile Go to an Android lib:** rejected — pgx→SQLite rewrite plus
  JNI glue is large and fragile.

## Consequences

UI logic is reimplemented from scratch and must be kept in sync with the web app
by hand. Server sync, if built later, is a new integration, not a shared codebase.
