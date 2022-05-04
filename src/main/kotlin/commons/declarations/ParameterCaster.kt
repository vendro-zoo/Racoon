package commons.declarations

/**
 * A class that casts a query parameter of a given type to a String.
 * The returned String is used as the value of the query parameter.
 */
interface ParameterCaster<T> {
    /**
     * Returns a String representation of the given parameter.
     * @param parameter the parameter to convert to a String
     * @return a String representation of the given parameter
     * that can be used as the value of a query parameter.
     */
    fun cast(parameter: T): String
}