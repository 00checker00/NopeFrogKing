package de.nopefrogking.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import de.nopefrogking.CircleFadeShader
import de.nopefrogking.Main
import de.nopefrogking.utils.DelegatingBlockingInputProcessor
import de.project.ice.screens.BaseScreenAdapter

open class CircleFadeScreen(game: Main, val duration: Float = 5.0f, val interpolation: Interpolation = Interpolation.pow2, private var onFinish: (()->Unit)? = null) : BaseScreenAdapter(game) {
    override val inputProcessor = DelegatingBlockingInputProcessor(super.inputProcessor)
    override val priority = 1
    private var elapsed = 0f

    private val shader = CircleFadeShader()
    private val renderer = ShapeRenderer(20, shader)

    var center = Vector2(Gdx.graphics.width/2f, Gdx.graphics.height/2f)
    var color = Color.BLACK

    var minRadius = 0f
    var maxRadius = 1f

    override fun update(delta: Float) {
        elapsed += delta

        val progress = Math.min(1f, elapsed/duration)
        val alpha = interpolation.apply(progress)

        if (progress >= 1f && onFinish != null) {
            onFinish?.invoke()
            onFinish = null
            finished()
        }

        val diff = maxRadius - minRadius
        shader.radius = minRadius + diff - diff*alpha
    }

    protected open fun finished() {

    }

    override fun render() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shader.center.set(center)

        renderer.begin(ShapeRenderer.ShapeType.Filled)

        renderer.color = color

        renderer.rect(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        renderer.end()
    }
}