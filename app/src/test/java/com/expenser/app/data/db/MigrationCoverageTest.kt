package com.expenser.app.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every schema version Room has exported must be reachable from version 1 one step at a
 * time. A version bump without a matching Migration crashes every existing install on
 * launch ("A migration from N to N+1 was required but not found"), just as a schema
 * change without a version bump did in v2.0.0.
 */
class MigrationCoverageTest {

    // Unit tests run with the module directory as the working directory.
    private val schemaDir = File("schemas/${ExpenserDatabase::class.qualifiedName}")

    private fun exportedVersions(): List<Int> =
        schemaDir.listFiles().orEmpty().mapNotNull { it.name.removeSuffix(".json").toIntOrNull() }.sorted()

    @Test fun exported_schema_versions_have_no_gaps() {
        val versions = exportedVersions()
        assertTrue("no exported schemas in ${schemaDir.absolutePath}", versions.isNotEmpty())
        assertEquals((1..versions.last()).toList(), versions)
    }

    @Test fun every_version_step_has_a_migration() {
        val latest = exportedVersions().last()
        val steps = ExpenserDatabase.MIGRATIONS.map { it.startVersion to it.endVersion }.toSet()
        val missing = (1 until latest).map { it to it + 1 }.filterNot { it in steps }
        assertEquals("add these to ExpenserDatabase.MIGRATIONS", emptyList<Pair<Int, Int>>(), missing)
    }
}
