package it.zoo.vendro.racoon.internals.protocols

class PostgresSQLProtocol : SQLProtocol {
    override val name: String = "postgresql"

    override val quotation: Quotation = Quotation(
        identifierQuote = "\"",
        stringQuote = "'"
    )
}