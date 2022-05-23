import commons.configuration.ConnectionSettings
import habitat.RacoonDen
import habitat.configuration.RacoonConfiguration
import models.Cat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

internal class Playground {
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
    internal fun insert() {
        val cat = Cat(
            name = "Kotlin",
        )

        val manager = RacoonDen.getManager()

        manager.insertK(cat)

        assertNotNull(cat.id)
    }

    @Test
    internal fun update() {
        val manager = RacoonDen.getManager()

        val cat = manager.find<Cat>(18)?.let {
            it.name = "newName"
            manager.update(it)
        }

        manager.commit()
    }

    @Test
    internal fun newMapping() {
        val manager = RacoonDen.getManager()

        val cat = manager.find<Cat>(1)

        println(cat?.owner_id?.get())

        manager.commit()
    }
}