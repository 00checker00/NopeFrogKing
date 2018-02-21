package de.nopefrogking.utils

import java.util.*

internal val random by lazy { Random() }

fun <T> randomOf(collection: Collection<T>): T {
    val selected = random.nextInt(collection.size)
    val iterator = collection.iterator()
    for (i in 0 until (selected-1)) iterator.next()
    return iterator.next()
}

fun <T> randomOf(array: Array<T>): T {
    return array[random.nextInt(array.size)]
}

fun <T> Array<T>.getRandom(): T = randomOf(this)
fun <T> Collection<T>.getRandom(): T = randomOf(this)