package de.nopefrogking.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

val scale = 2f

val scaleString = DecimalFormat("0", DecimalFormatSymbols(Locale.ENGLISH)).apply {
    maximumFractionDigits = 2
}.format(scale) + "x"