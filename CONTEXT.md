# Expenser (Android) — Context

Offline-first native Android expense tracker. Kotlin + Jetpack Compose + Room.
Ported from the `expenser` web app (React UI + Go/Postgres server), which stays
separate. No network, no auth in v1. Single local user; server sync is a future
possibility, so schema stays sync-friendly.

## Glossary

### User
The single local person using the app. Auto-seeded on first launch. No login, no
password. Exists only so `userId` foreign keys survive for a future server sync.

### Category
A user-defined bucket for transactions. Has a **[Type](#type)** that gates which
transactions may use it. A category **in use** by any transaction cannot be
deleted (delete is blocked, not cascaded).
Fields: `id` (UUID), `name`, `type`, `description?`, `userId`.
`name` is unique per (user, type is not part of uniqueness — matches server `UNIQUE(name, user_id)`).

### Type
The discriminator `Expense | Income | Investment`. Applies to both a Category and
a Transaction. A transaction of a given type may only reference a category of the
**same** type (an Expense picks only Expense categories).

### Transaction
A single money entry: an expense, income, or investment. All three are the same
shape, so they live in **one** `transactions` table distinguished by
**[Type](#type)** (rather than three identical tables as on the server).
Fields: `id` (UUID), `name`, `amount`, `category` (FK), `date`, `note?`, `type`, `userId`.

### Amount
A money value stored as **Long minor units** (e.g. `1250` = ₹12.50), 2 decimal
places. Never a float. Formatted for display as INR / en-IN.

### Expense
A Transaction whose Type is `Expense`. The only Type with UI in v1.
Income and Investment are schema-ready but have no screens yet.

## Scope (v1)

Screens (bottom navigation): **Dashboard** (Total Expense card), **Expenses**
(list + add/edit/delete via bottom sheet), **Categories** (same).

Deferred: Income & Investment UI, charts (pie/line/bar), Excel import, auth,
server sync, currency settings.

## Fixed decisions

- Separate repo; appId `com.expenser.app`; minSdk/target 35 (Android 15).
- Manual DI (an `AppContainer` on the `Application`). No Hilt.
- Room with `foreign_keys` ON; UUID `String` primary keys; `amount` as `Long`
  minor units; `date` as ISO `yyyy-MM-dd` `String`.
- Category delete blocked when in use (SQLite `RESTRICT`).
- Add/edit via modal bottom sheet; delete from inside the edit sheet.
- Theme follows system light/dark + Material You dynamic color; no manual toggle.
