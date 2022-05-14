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
                    database = "test",
                    username = "test",
                    password = "test",
                    maxManagers = 2
                )
            )
        }

        const val verbose = true
    }

    @Test
    internal fun poolingSync() {
        var manager = RacoonDen.getManager()
        val managerRepr = manager.toString()

        manager.use { rm ->
            val cats = rm.createQueryRacoon("SELECT * FROM cat").use { qr -> qr.mapToClass<Cat>() }
            assertEquals(3, cats.size)
        }

        manager = RacoonDen.getManager()
        val newManagerRepr = manager.toString()

        assertEquals(newManagerRepr, managerRepr)
    }

    @Test
    internal fun poolingAsync() {
        val manager1 = RacoonDen.getManager()
        val managerRepr1 = manager1.toString()

        val manager2 = RacoonDen.getManager()
        val managerRepr2 = manager2.toString()

        assertNotEquals(managerRepr1, managerRepr2)
    }


    @Test
    internal fun poolingLimit() {
        RacoonDen.getManager()
        RacoonDen.getManager()

        assertThrows(SQLException::class.java, RacoonDen::getManager)
    }
}