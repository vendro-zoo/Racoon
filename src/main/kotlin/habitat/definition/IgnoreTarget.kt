package habitat.definition

enum class IgnoreTarget(val children: List<IgnoreTarget> = listOf()) {
    INSERT,
    UPDATE,
    ALL(listOf(INSERT, UPDATE)),
    ;

    fun getList(): List<IgnoreTarget> {
        return children + this
    }
}