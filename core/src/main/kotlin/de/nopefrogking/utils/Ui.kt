package de.nopefrogking.utils

import com.badlogic.gdx.scenes.scene2d.ui.Table

fun Table.addSpacer() = add("").minHeight(0f).minWidth(0f)!!