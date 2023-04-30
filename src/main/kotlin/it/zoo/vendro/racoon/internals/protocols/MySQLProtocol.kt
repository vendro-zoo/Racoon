package it.zoo.vendro.racoon.internals.protocols

class MySQLProtocol : SQLProtocol {
    override val name: String = "mysql"

    override val quotation: Quotation = Quotation(
        identifierQuote = "`",
        stringQuote = "'"
    )
}