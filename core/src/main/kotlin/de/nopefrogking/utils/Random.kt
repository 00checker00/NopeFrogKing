package de.nopefrogking.utils

import java.util.*

fun Random.nextFloat(min: Float, max: Float): Float = nextFloat() * (max - min) + min
