package it.zoo.vendro.racoon.definition

enum class IgnoreTarget(val children: List<IgnoreTarget> = listOf()) {
    INSERT,
    UPDATE,
    SELECT,
    ALL(listOf(INSERT, UPDATE, SELECT)),
    ;

    fun getList(): List<IgnoreTarget> {
        return children + this
    }
}