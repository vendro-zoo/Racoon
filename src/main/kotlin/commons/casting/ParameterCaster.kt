package commons.casting

/**
 * A class that casts a query parameter of a given type to another given type.
 *
 * @param [T] the type of the parameter to cast
 * @param [K] the type to cast to
 */
interface ParameterCaster<T: Any, K: Any> {
    /**
     * Converts the given parameter to another type.
     *
     * @param parameter the parameter to convert
     * @return the same parameter converted to the other type
     * that can be used as the value of a query parameter.
     */
    fun cast(parameter: T): K

    /**
     * Converts the parameter back to its original type.
     *
     * @param parameter the parameter to convert to the original type
     * @return the same parameter converted to the original type
     */
    fun uncast(parameter: K): T
}