package it.zoo.vendro.racoon.internals.protocols

interface SQLProtocol {
    val name: String
    val quotation: Quotation
}

data class Quotation(
    val identifierQuote: String,
    val stringQuote: String
) {
    fun quoteIdentifier(identifier: String): String = "$identifierQuote$identifier$identifierQuote"
    fun quoteString(string: String): String = "$stringQuote$string$stringQuote"
}