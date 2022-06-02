package internals.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConnectionSettingsTest {

    @Test
    fun testToStringNoCredentials() {
        val cs = ConnectionSettings(
            host = "localhost",
            port = 3306,
            database = "test",
        )

        assertEquals("jdbc:mysql://localhost:3306/" +
                "test?characterEncoding=UTF-8&connectionTimeout=100&socketTimeout=2000",
            cs.toString())
    }

    @Test
    fun testToStringWithCredentials() {
        val cs = ConnectionSettings(
            host = "localhost",
            port = 3306,
            database = "test",
            username = "user",
            password = "pass",
        )

        assertEquals("jdbc:mysql://localhost:3306/" +
                "test?characterEncoding=UTF-8&connectionTimeout=100&socketTimeout=2000" +
                "&user=user&password=pass",
            cs.toString())
    }
}