package it.zoo.vendro.racoon.serdes

import it.zoo.vendro.racoon.context.FromQueryCasterContext
import it.zoo.vendro.racoon.context.ToQueryCasterContext

/**
 * A class that casts a query parameter of a given type to another given type.
 *
 * @param [T] the type of the parameter to cast
 * @param [K] the type to cast to
 */
interface RacoonSerDe<T, K> {
    val toQueryPrefix: String
        get() = ""
    val toQueryPostfix: String
        get() = ""

    val fromQueryPrefix: String
        get() = ""
    val fromQueryPostfix: String
        get() = ""

    /**
     * Converts the given parameter to another type.
     *
     * @param parameter the parameter to convert
     * @return the same parameter converted to the other type
     * that can be used as the value of a query parameter.
     */
    fun toQuery(parameter: T, context: ToQueryCasterContext): K

    /**
     * Converts the parameter back to its original type.
     *
     * @param parameter the parameter to convert to the original type
     * @return the same parameter converted to the original type
     */
    fun fromQuery(parameter: K, context: FromQueryCasterContext): T
}