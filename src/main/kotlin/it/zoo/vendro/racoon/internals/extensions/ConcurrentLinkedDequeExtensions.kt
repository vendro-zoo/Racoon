package it.zoo.vendro.racoon.internals.extensions

import java.util.concurrent.ConcurrentLinkedDeque

fun <T> ConcurrentLinkedDeque<T>.removeLastOrNull(): T? {
    return try {
        removeLast()
    } catch (e: NoSuchElementException) {
        null
    }
}