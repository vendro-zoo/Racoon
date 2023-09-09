package it.zoo.vendro.racoon.protocols

interface SQLProtocol {
    val name: String
    val quotation: Quotation
    val parameter: Parameter
}

data class Quotation(
    val identifierQuote: String,
    val stringQuote: String
) {
    fun quoteIdentifier(identifier: String): String = "$identifierQuote$identifier$identifierQuote"
    fun quoteString(string: String): String = "$stringQuote$string$stringQuote"
}

data class Parameter(
    val indexString: String,
    val namedString: String,
    val indexRegex: Regex,
    val namedRegex: Regex,
)