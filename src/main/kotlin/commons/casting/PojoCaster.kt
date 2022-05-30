package commons.casting

@FunctionalInterface
interface PojoCaster<T> {
    fun cast(record: T): Map<String, Any?>
}