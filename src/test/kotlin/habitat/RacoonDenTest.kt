package habitat

import commons.configuration.ConnectionSettings
import habitat.configuration.RacoonConfiguration
import models.Cat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

internal class RacoonDenTest {
    // Configuring the connection settings
    companion object {
        @BeforeAll
        @JvmStatic
        internal fun setUpClass() {
            RacoonConfiguration.Connection.setDefault(
                ConnectionSettings(
                    host = "localhost",
                    port = 3306,
                    database = "racoon-ktor-sample",
                    username = "admin",
                    password = "admin",
                    maxManagers = 2
                )
            )
        }

        const val verbose = true
    }

    @Test
    internal fun test() {
        poolingSync()
        assertEquals(0, RacoonDen.inUseManagers())
        poolingAsync()
        assertEquals(0, RacoonDen.inUseManagers())
        poolingLimit()
        assertEquals(0, RacoonDen.inUseManagers())
    }

    private fun poolingSync() {
        var manager = RacoonDen.getManager()
        val managerRepr = manager.connection.toString()

        manager.use { rm ->
            rm.createQueryRacoon("SELECT * FROM cat").use { qr -> qr.mapToClass<Cat>() }
        }

        manager = RacoonDen.getManager()
        val newManagerRepr = manager.connection.toString()
        manager.rollback().release()

        assertEquals(newManagerRepr, managerRepr)
    }

    private fun poolingAsync() {
        val manager1 = RacoonDen.getManager()
        val managerRepr1 = manager1.toString()
        manager1.rollback().release()

        val manager2 = RacoonDen.getManager()
        val managerRepr2 = manager2.toString()
        manager2.rollback().release()

        assertNotEquals(managerRepr1, managerRepr2)
    }


    private fun poolingLimit() {
        val m1 = RacoonDen.getManager()
        val m2 = RacoonDen.getManager()

        assertThrows(SQLException::class.java, RacoonDen::getManager)
        m1.rollback().release()
        m2.rollback().release()
    }
}