# Expenser (Android) — Context

Offline-first native Android tracker for expenses, income and investments.
Kotlin + Jetpack Compose + Room. Ported from the `expenser` web app (React UI +
Go/Postgres server), which stays separate. No network, no auth. Single local
user; server sync is a future possibility, so schema stays sync-friendly.

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
places. Never a float. Displayed in the **currency** chosen on the Profile screen
(INR by default), formatted for the device locale. Summary figures and list rows
drop a zero fraction (`₹45,000`, not `₹45,000.00`); a non-zero one is always shown.
The currency is a display setting only: amounts are never converted.

### Expense / Income / Investment
A Transaction whose Type is `Expense`, `Income` or `Investment`. Each has its
own bottom-navigation tab; the three share one list screen and one view model,
parameterised by Type.

### Month scope
The calendar month every screen is filtered to (the dashboard, the three lists
and the per-category totals). Shared app-wide, so changing it on one screen
changes it everywhere. Defaults to the current month and keeps following the
calendar until the user picks one; "All time" removes the scope. A custom date
range in a list's filters overrides it for that list only.

### Left this month
The dashboard's headline: income minus expenses minus investments, within the
**[Month scope](#month-scope)**. Negative means the month is overspent, and the
dashboard says so ("Over income by …") rather than showing a minus sign.

## Scope

Bottom navigation: **Dashboard**, **Income**, **Expenses**, **Investments**.
From the top bar of each: **Categories** and **Profile**.

- **Dashboard:** "Left this month" hero card with an income split bar; Income /
  Spent / Invested tiles that open their tabs; top expense categories; recent
  entries.
- **Lists:** grouped by day with subtotals; search, category chips, date-range
  and sort filters; add/edit in a bottom sheet; swipe to delete with undo.
- **Categories:** filter by Type; this month's total per category.
- **Profile:** name and photo, currency, theme, daily reminder, backup export /
  import (JSON) and CSV export.

Deferred: Excel import, auth, server sync, charts over time (trends across
months).

## Fixed decisions

- Separate repo; appId `com.expenser.app`; minSdk 26 (Android 8.0), targetSdk 36,
  compileSdk 37.
- Manual DI (an `AppContainer` on the `Application`). No Hilt.
- Room with `foreign_keys` ON; UUID `String` primary keys; `amount` as `Long`
  minor units; `date` as ISO `yyyy-MM-dd` `String`.
- Category delete blocked when in use (SQLite `RESTRICT`).
- Add/edit via modal bottom sheet; delete from inside the edit sheet.
- Fixed "Rose" palette in light and dark (no Material You dynamic colour), so the
  bento layout looks the same on every device. Theme is System / Light / Dark,
  chosen on the Profile screen and previewed live before saving.
- Category badge colours are derived from the category id, not stored, so they
  need no schema column and survive renames.
