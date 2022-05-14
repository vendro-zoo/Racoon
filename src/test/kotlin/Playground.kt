import commons.expansions.lowerSnakeCase
import commons.model.generateInsertQuery
import models.Cat
import org.junit.jupiter.api.Test

internal class Playground {
    @Test
    internal fun insert() {
        println("Cat".lowerSnakeCase())
        println(generateInsertQuery<Cat>())
    }
}