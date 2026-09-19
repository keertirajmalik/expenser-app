# Single transactions table with a type discriminator

The server stores expenses, incomes, and investments in three separate tables
(`transactions`, `incomes`, `investments`) that are structurally identical. On
device we collapse them into one `transactions` table with a `type` column
(`Expense | Income | Investment`), because the shapes are the same and one table
+ one DAO removes threefold duplication. v1 only reads `type = Expense`; adding
Income and Investment later is a filter change, not new plumbing.

## Consequences

The on-device schema deliberately diverges from the server's. A future server
sync must map `type` to the right server table (`WHERE type = ...`) — a trivial
split, but it must not be forgotten. This is why the divergence is recorded here.
