package de.nopefrogking.utils

inline infix fun <T> T?.orElse(create: ()->T) = this ?: let { create() }