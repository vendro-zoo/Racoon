package it.zoo.vendro.racoon.protocols

class PostgresSQLProtocol : SQLProtocol {
    override val name: String = "postgresql"

    override val quotation: Quotation = Quotation(
        identifierQuote = "\"",
        stringQuote = "'"
    )

    override val parameter: Parameter = Parameter(
        indexString = "?",
        namedString = ":",
        indexRegex = Regex("\\?[0-9]*"),
        namedRegex = Regex(":[\\w\\u0080-\\u00FF]+"),
    )
}