# UUID string primary keys instead of autoincrement

Rows use `String` UUID primary keys (generated on device with
`UUID.randomUUID()`), not Room's idiomatic `Long` autoincrement. This matches the
server schema so IDs stay stable and collision-free if a server sync is added
later — no remapping of device-local integer IDs. We keep the `userId` foreign key
for the same reason, even though there is only one local user and no auth.

## Consequences

Slightly less idiomatic Room and marginally larger keys, accepted for
sync-friendliness. If server sync is ever ruled out permanently, this can be
revisited.
