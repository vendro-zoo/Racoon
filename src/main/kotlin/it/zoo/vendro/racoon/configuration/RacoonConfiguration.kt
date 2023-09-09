package it.zoo.vendro.racoon.configuration

import it.zoo.vendro.racoon.connection.ConnectionManager
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.internals.extensions.camelCase
import it.zoo.vendro.racoon.internals.extensions.lowerSnakeCase
import it.zoo.vendro.racoon.internals.extensions.upperCamelCase
import it.zoo.vendro.racoon.internals.extensions.upperSnakeCase
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import it.zoo.vendro.racoon.serdes.builtin.*
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * An object containing the configuration for Racoon.
 */
@Suppress("unused")
class RacoonConfiguration(
    val connection: ConnectionSettings = ConnectionSettings(
        host = "localhost",
        database = "test"
    ),
    val caching: Caching = Caching(),
    val naming: Naming = Naming(),
    val casting: Casting = Casting(),
    val resourcing: Resourcing = Resourcing()
) {
    class Caching(
        /**
         * The maximum number of entries in the cache of each [ConnectionManager].
         *
         * The default value is 100.
         */
        val maxEntries: Int = 100,
        /**
         * The number of entries to remove from the cache when it is full.
         *
         * Note: the entries removed are the oldest ones.
         *
         * The default value is 20.
         */
        val cleaningBatchSize: Int = 20
    )

    /**
     * All the configuration for the default mapping of tables and columns names.
     */
    class Naming(
        /**
         * A lambda that will be used to map the alias of the table from the name of a given class.
         *
         * The default lambda is a function that returns the name of the class as-is.
         *
         * @see [it.zoo.vendro.racoon.internals.mappers.TableAliasMapper] for a list of built-in mappers.
         */
        val tableAliasMapper: (String) -> String = { it },

        /**
         * A lambda that will be used to map the name of the table from the name of a given class.
         *
         * The default lambda is a function that returns the name of the class as-is.
         *
         * @see [it.zoo.vendro.racoon.internals.mappers.NameMapper] for a list of built-in mappers.
         */
        val tableNameMapper: (String) -> String = { it },

        /**
         * A lambda that will be used to map the name of the column from the name of a given property.
         *
         * The default lambda is a function that returns the name of the property as-is.
         *
         * @see [it.zoo.vendro.racoon.internals.mappers.NameMapper] for a list of built-in mappers.
         */
        val columnNameMapper: (String) -> String = { it }
    ) {
        companion object {
            /**
             * Converts a camelCase string to snake_case using the [lowerSnakeCase] function extension.
             */
            val lowerSnakeCase: (String) -> String = { it.lowerSnakeCase() }

            /**
             * Converts a camelCase string to UPPER_SNAKE_CASE using the [upperSnakeCase] function extension.
             */
            val upperSnakeCase: (String) -> String = { it.upperSnakeCase() }

            /**
             * Converts the string to lowercase using the [lowercase] function.
             */
            val lowercase: (String) -> String = { it.lowercase() }

            /**
             * Converts the string to UPPERCASE using the [uppercase] function.
             */
            val uppercase: (String) -> String = { it.uppercase() }

            /**
             * Converts the string to camelCase using the [camelCase] function extension.
             */
            val camelCase: (String) -> String = { it.camelCase() }

            /**
             * Converts the string to UpperCamelCase using the [upperCamelCase] function extension.
             */
            val upperCamelCase: (String) -> String = { it.upperCamelCase() }

            /**
             * Extracts only the non-sequential uppercase letters from the given string, and converts the resulting string to lowercase.
             *
             * @return the extracted non-sequential uppercase letters from the given string, converted to lowercase.
             */
            val onlyUpperNonSequentialToLower: (String) -> String = { onlyUpperNonSequential(it).lowercase() }

            /**
             * Extracts only the non-sequential uppercase letters from the given string.
             *
             * @return The extracted non-sequential uppercase letters.
             */
            val onlyUpperNonSequential: (String) -> String = { s ->
                s.withIndex().filter { (index, char) ->
                    char.isUpperCase() && (
                            index == 0 ||
                                    index > 0 && s[index - 1].isLowerCase() ||
                                    index < s.length - 1 && s[index + 1].isLowerCase()

                            )
                }.map { it.value }.joinToString("")
            }

            /**
             * Extracts only the uppercase letters from the given string, and converts the resulting string to lowercase.
             *
             * @return the extracted uppercase letters from the given string, converted to lowercase.
             */
            val onlyUpperToLower: (String) -> String = { onlyUpper(it).lowercase() }

            /**
             * Extracts only the uppercase letters from the given string.
             *
             * @return The extracted uppercase letters.
             */
            val onlyUpper: (String) -> String = { it.filter { x -> x.isUpperCase() } }
        }
    }

    class Casting(
        val racoonCasters: MutableSet<Pair<WKClass, MutableSet<Pair<WKClass, RacoonSerDe<out Any?, out Any?>>>>> =
            mutableSetOf(
                WKClass(typeOf<LazyId<*, *>>()) to mutableSetOf(
                    WKClass(typeOf<Int>()) to LazySerDe(),
                ),
                WKClass(typeOf<Enum<*>>()) to mutableSetOf(WKClass(typeOf<String>()) to EnumSerDe()),
                WKClass(typeOf<Date>()) to mutableSetOf(
                    WKClass(typeOf<java.sql.Date>()) to DateSerDe(),
                    WKClass(typeOf<Timestamp>()) to DateTimestampSerDe()
                )
            )
    ) {
        data class WKClass(val kType: KType) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as WKClass

                val thisJvm = kType.jvmErasure
                val otherJvm = other.kType.jvmErasure

                if (thisJvm.qualifiedName != otherJvm.qualifiedName) return false

                if (!kType.arguments.all { it == KTypeProjection.STAR })
                    throw IllegalArgumentException("Only * type parameter is supported")

                return true
            }

            override fun hashCode(): Int {
                return kType.hashCode()
            }
        }

        /**
         * Adds the given [RacoonSerDe] to the list of registered list of [RacoonSerDe]s.
         *
         * If a caster has already been registered for the given [KClass], it will be replaced.
         *
         * Example:
         * ```
         * // Adds a custom caster for [LazyId]
         * RacoonConfiguration.Casting.setCaster(LazyId::class, Int::class, LazyCaster())
         * ```
         *
         * @param J The class of the parameter to cast (Java type).
         * @param S The class of the parameter to cast to (SQL type).
         * @param caster The caster to use to cast the parameter.
         * @see [it.zoo.vendro.racoon.serdes.RacoonSerDe]
         */
        inline fun <reified J, reified S> setCaster(caster: RacoonSerDe<J, S>) = apply {
            val wJClass = WKClass(typeOf<J>())
            val wSClass = WKClass(typeOf<S>())

            val m1: MutableSet<Pair<WKClass, RacoonSerDe<*, *>>> =
                racoonCasters.find { it.first == wJClass }?.second ?: mutableSetOf<Pair<WKClass, RacoonSerDe<*, *>>>().apply {
                    racoonCasters.add(wJClass to this)
                }
            m1.add(wSClass to caster)
        }

        /**
         * Returns the [RacoonSerDe] for the given [KClass].
         *
         * If no caster has been registered for the given [KClass],
         * it will try searching for a caster for the superclasses of the given [KClass].
         * If still no caster is found, it will return `null`.
         *
         * @param jClass The class of the parameter to cast (Java type).
         * @param sClass The class of the parameter to cast to (SQL type).
         * @return The [RacoonSerDe] for the given [KClass] or `null` if no caster is found.
         * @see [it.zoo.vendro.racoon.internals.serdes.RacoonSerDe]
         */
        inline fun <reified J, reified S> getCaster(): RacoonSerDe<Any?, Any?>? = getCasterK(typeOf<J>(), typeOf<S>())

        fun getCasterK(jType: KType, sType: KType): RacoonSerDe<Any?, Any?>? {
            val wJClass = WKClass(jType)
            val wSClass = WKClass(sType)

            val m1 = racoonCasters.find { it.first == wJClass }?.second ?: jType.jvmErasure.superclasses.firstNotNullOfOrNull { superclass ->
                val arguments = List(superclass.typeParameters.size) { KTypeProjection.STAR }
                racoonCasters.find { it.first == WKClass(superclass.createType(arguments = arguments, nullable = wJClass.kType.isMarkedNullable)) }
            }?.second ?: return null

            val m2 = m1.find { it.first == wSClass }?.second ?: sType.jvmErasure.superclasses.firstNotNullOfOrNull { superclass ->
                val arguments = List(superclass.typeParameters.size) { KTypeProjection.STAR }
                m1.find { it.first == WKClass(superclass.createType(arguments = arguments, nullable = wSClass.kType.isMarkedNullable)) }
            }?.second

            return m2 as RacoonSerDe<Any?, Any?>?
        }

        inline fun <reified J> getFirstCaster(): RacoonSerDe<*, *>? = getFirstCasterK(typeOf<J>())

        fun getFirstCasterK(jType: KType): RacoonSerDe<Any?, *>? {
            val wJClass = WKClass(jType)

            val m1 = racoonCasters.find { it.first == wJClass }?.second ?: jType.jvmErasure.superclasses.firstNotNullOfOrNull { superclass ->
                val arguments = List(superclass.typeParameters.size) { KTypeProjection.STAR }
                racoonCasters.find { it.first == WKClass(superclass.createType(arguments = arguments, nullable = wJClass.kType.isMarkedNullable)) }
            }?.second ?: return null

            return m1.first().second as RacoonSerDe<Any?, *>?
        }
    }

    /**
     * All the configuration for resource handling.
     */
    class Resourcing(
        /**
         * The base path for sql files.
         */
        val baseSQLPath: String = "sql"
    )
}