package de.nopefrogking.utils

import java.lang.reflect.Field
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ClampDelegate<T: Comparable<T>>(val min: T, val max: T, initial: T) {
    var field: T = initial

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return field
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        field = when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
}
inline fun <T: Comparable<T>> clamp(min: T, max: T, init: ()->T) = ClampDelegate(min, max, init())

class ReflectDelegate<T> {
    fun getField(thisRef: Any, name: String): Field {
        val field: Field by lazy { thisRef.javaClass.findField(name) }
        field.isAccessible = true
        return field
    }

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
            = getField(thisRef!!, property.name).get(thisRef) as T

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        getField(thisRef!!, property.name).set(thisRef, value)
    }
}

fun <T> reflect() = ReflectDelegate<T>()


abstract class ObservableNotNullProperty<T>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    protected open fun afterChange (property: KProperty<*>, oldValue: T?, newValue: T): Unit {}

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        this.value = value
        afterChange(property, oldValue, value)
    }
}

inline fun <T> observableNotNull(crossinline onChange: (property: KProperty<*>, oldValue: T?, newValue: T) -> Unit):
        ReadWriteProperty<Any?, T> = object : ObservableNotNullProperty<T>() {
    override fun afterChange(property: KProperty<*>, oldValue: T?, newValue: T) = onChange(property, oldValue, newValue)
}