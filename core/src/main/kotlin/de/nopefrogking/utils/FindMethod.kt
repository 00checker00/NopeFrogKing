package de.nopefrogking.utils

import java.lang.reflect.Field

fun Class<*>.findField(name: String): Field {
    var cur: Class<*>? = this
    while (cur != null) {
        try {
            return cur.getDeclaredField(name)
        } catch (ignore: NoSuchFieldException) { }
        cur = cur.superclass
    }
    throw NoSuchFieldException()
}