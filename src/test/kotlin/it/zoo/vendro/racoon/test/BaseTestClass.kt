package it.zoo.vendro.racoon.test

import org.testcontainers.containers.PostgreSQLContainer
import java.io.File


internal object PostgreContainer {
    val POSTGRE: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpassword")

    init {
        val restoreFile = File(this::class.java.getResource("/postgres-restore.sql")!!.file)
        POSTGRE.withFileSystemBind(restoreFile.absolutePath, "/docker-entrypoint-initdb.d/restore.sql")
        POSTGRE.start()
    }
}