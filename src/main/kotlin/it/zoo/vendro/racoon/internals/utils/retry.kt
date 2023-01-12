package it.zoo.vendro.racoon.internals.utils

fun <T> retryUntilNotNull(
    times: Int = 3,
    delay: Long = 500,
    block: () -> T?,
): T {
    var attempts = 0
    while (true) {
        val t = block()
        if (t != null) return t

        attempts++
        if (attempts >= times) throw IllegalStateException("Failed to execute after $attempts attempts")

        Thread.sleep(delay)
    }
}