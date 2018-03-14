package de.nopefrogking.utils

import com.badlogic.gdx.graphics.g2d.Animation

inline fun <reified T> Animation<T>.getKeyFramesTyped(): Array<T> {
    val untyped = (this as Animation<*>).keyFrames
    return Array(untyped.size) { untyped[it] as T }
}

@Suppress("UNCHECKED_CAST")
fun <T> Animation<T>.getKeyFrameAt(index: Int): T? =
        (this as Animation<*>).keyFrames.let {
            if (it.size > index)
                it[index] as T?
            else
                null
        }