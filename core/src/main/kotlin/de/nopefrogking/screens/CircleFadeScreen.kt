package de.nopefrogking.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import de.nopefrogking.CircleFadeShader
import de.nopefrogking.Main
import de.nopefrogking.utils.DelegatingBlockingInputProcessor
import de.project.ice.screens.BaseScreenAdapter

class CircleFadeScreen(game: Main, val duration: Float = 5.0f, val interpolation: Interpolation = Interpolation.pow2) : BaseScreenAdapter(game) {
    override val inputProcessor = DelegatingBlockingInputProcessor(super.inputProcessor)
    override val priority = 0
    private var elapsed = 0f

    private val shader = CircleFadeShader()
    private val renderer = ShapeRenderer(20, shader)

    val centerOffset = Vector2()

    override fun update(delta: Float) {
        elapsed += delta

        val progress = Math.min(1f, elapsed/duration)
        val alpha = interpolation.apply(progress)

        shader.radius = 1f-alpha
    }

    override fun render() {
        shader.center.set(Gdx.graphics.width/2f + centerOffset.x, Gdx.graphics.height/2f + centerOffset.y)

        renderer.begin(ShapeRenderer.ShapeType.Filled)

        renderer.color = Color.BLACK

        renderer.rect(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        renderer.end()
    }
}