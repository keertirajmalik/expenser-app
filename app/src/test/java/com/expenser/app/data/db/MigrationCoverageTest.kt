package com.expenser.app.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A version bump without a matching Migration crashes existing installs on launch. Every
 * step k -> k+1 needs its own migration; shortcuts like 1 -> 3 don't replace them.
 */
class MigrationCoverageTest {

    private fun exportedVersions(): List<Int> {
        val root = System.getProperty("roomSchemaDir")
        assertNotNull("roomSchemaDir not set; run through Gradle", root)
        val dir = File(root!!, ExpenserDatabase::class.qualifiedName!!)
        val versions = dir.listFiles().orEmpty().mapNotNull { it.name.removeSuffix(".json").toIntOrNull() }.sorted()
        assertTrue("no exported schemas in ${dir.absolutePath}", versions.isNotEmpty())
        return versions
    }

    @Test fun exported_schema_versions_have_no_gaps() {
        val versions = exportedVersions()
        assertEquals((1..versions.last()).toList(), versions)
    }

    @Test fun every_version_step_has_a_migration() {
        val latest = exportedVersions().last()
        val steps = ExpenserDatabase.MIGRATIONS.map { it.startVersion to it.endVersion }.toSet()
        val missing = (1 until latest).map { it to it + 1 }.filterNot { it in steps }
        assertEquals("add these to ExpenserDatabase.MIGRATIONS", emptyList<Pair<Int, Int>>(), missing)
    }
}
