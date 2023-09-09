package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.TestConfiguration
import models.Dog
import models.DogColor
import models.DogSize
import models.Dogs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EnumCasterTest {
    val pool = TestConfiguration.POOL

    @Test
    fun enumInsert() {
        pool.getManager().use { rm ->
            rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.SMALL
                )
            )
        }
    }

    @Test
    fun enumInsertWithColumnName() {
        pool.getManager().use { rm ->
            rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.L,
                    color = DogColor.DARK
                )
            )
        }
    }

    @Test
    fun enumSelect() {
        pool.getManager().use { rm ->
            val dog = rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.SMALL,
                    color = DogColor.LIGHT
                )
            )
            val dog2 = Dogs.find(rm, dog.id!!)
            assertEquals(DogSize.SMALL, dog2!!.size)
        }
    }

    @Test
    fun enumSelectWithColumnName() {
        pool.getManager().use { rm ->
            val dog = rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.L
                )
            )
            val dog2 = Dogs.find(rm, dog.id!!)
            assertEquals(DogSize.L, dog2!!.size)
        }
    }
}