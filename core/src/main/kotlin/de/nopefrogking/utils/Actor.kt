package de.nopefrogking.utils

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor

fun Actor.drawWithTexture(batch: Batch, textureRegion: TextureRegion?, parentAlpha: Float, flipX: Boolean = false, flipY: Boolean = false) {
    drawWithTexture(batch, textureRegion, x, y, width, height, parentAlpha, flipX, flipY)
}

fun Actor.drawWithTexture(batch: Batch, textureRegion: TextureRegion?, x: Float, y: Float, width: Float, height: Float, parentAlpha: Float, flipX: Boolean = false, flipY: Boolean = false) {
    textureRegion?.let {
        drawWithTexture(
                batch,
                textureRegion.texture,
                x,
                y,
                width,
                height,
                textureRegion.regionX,
                textureRegion.regionY,
                textureRegion.regionWidth,
                textureRegion.regionHeight,
                textureRegion.isFlipX xor flipX,
                textureRegion.isFlipY xor flipY,
                parentAlpha
        )
    }
}

fun Actor.drawWithTexture(batch: Batch,
                          texture: Texture?,
                          srcX: Int,
                          srcY: Int,
                          srcWidth: Int,
                          srcHeight: Int,
                          flipX: Boolean,
                          flipY: Boolean,
                          parentAlpha: Float) {
    drawWithTexture(batch, texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY, parentAlpha)
}

fun Actor.drawWithTexture(batch: Batch,
                          texture: Texture?,
                          x: Float,
                          y: Float,
                          width: Float,
                          height: Float,
                          srcX: Int,
                          srcY: Int,
                          srcWidth: Int,
                          srcHeight: Int,
                          flipX: Boolean,
                          flipY: Boolean,
                          parentAlpha: Float) {
    texture?.let {
        val color = batch.color
        batch.color = this.color.cpy().mul(1.0f, 1.0f, 1.0f, parentAlpha)
        batch.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY)
        batch.color = color
    }
}

inline fun Actor.drawWithTexture(batch: Batch, texture: Texture?, parentAlpha: Float, drawCall: Actor.(Texture)->Unit) {
    texture?.let {
        val color = batch.color
        batch.color = this.color.cpy().mul(1.0f, 1.0f, 1.0f, parentAlpha)
        drawCall(texture)
        batch.color = color
    }
}

inline fun Actor.drawWithTexture(batch: Batch, texture: TextureRegion?, parentAlpha: Float, drawCall: Actor.(TextureRegion)->Unit) {
    texture?.let {
        val color = batch.color
        batch.color = this.color.cpy().mul(1.0f, 1.0f, 1.0f, parentAlpha)
        drawCall(texture)
        batch.color = color
    }
}

fun Actor.setWidthByHeight(region: TextureRegion?, centerOrigin: Boolean = true) {
    region ?: return

    val ratio = region.regionWidth.toFloat()/region.regionHeight.toFloat()
    width = height * ratio
    if (centerOrigin)
        originX = width / 2
}

fun Actor.setHeightByWidth(region: TextureRegion?, centerOrigin: Boolean = true) {
    region ?: return

    val ration = region.regionHeight.toFloat()/region.regionWidth.toFloat()
    height = width * ration
    if (centerOrigin)
        originY = height / 2
}

fun Actor.intersects(other: Actor): Boolean
    = Rectangle(x, y, width, height).overlaps(Rectangle(other.x, other.y, other.width, other.height))