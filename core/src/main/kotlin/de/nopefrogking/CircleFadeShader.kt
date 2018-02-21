package de.nopefrogking

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import de.nopefrogking.utils.insertShaderVars
import org.intellij.lang.annotations.Language

@Language("GLSL")
private val vertexShader = """
        attribute vec4 _POSITION_ATTRIBUTE;
        attribute vec4 _COLOR_ATTRIBUTE;

        varying vec4 v_col;

        uniform mat4 u_projModelView;

        void main() {
           gl_Position = u_projModelView * _POSITION_ATTRIBUTE;
           v_col = _COLOR_ATTRIBUTE;
           gl_PointSize = 1.0;
        }
        """.insertShaderVars()


@Language("GLSL")
private val fragmentShader = """
        #ifdef GL_ES
        precision mediump float;
        #endif
        varying vec4 v_col;

        uniform vec2 u_center;
        uniform vec2 u_resolution;
        uniform float u_radius;

        void main() {
            vec2 relToCenter = (gl_FragCoord.xy - u_center) / u_resolution;

            if (u_resolution.y > u_resolution.x) {
	            relToCenter.y *= u_resolution.y / u_resolution.x;
            } else {
	            relToCenter.x *= u_resolution.x / u_resolution.y;
            }

            float dist = length(relToCenter);

            if(dist < u_radius) {
                discard;
            } else {
                gl_FragColor = v_col;
            }

        }
        """.insertShaderVars()



class CircleFadeShader: ShaderProgram(vertexShader, fragmentShader) {
    var radius = 1.0f
    val center = Vector2(0f, 0f)

    private val centerUniformLocation: Int
    private val resolutionUniformLocation: Int
    private val radiusUniformLocation: Int

    init {
        if (!isCompiled) throw IllegalArgumentException("Error compiling shader: " + log)
        begin()
        centerUniformLocation = getUniformLocation("u_center")
        resolutionUniformLocation = getUniformLocation("u_resolution")
        radiusUniformLocation = getUniformLocation("u_radius")
        end()
    }

    override fun begin() {
        super.begin()
        setUniform2fv(centerUniformLocation, arrayOf(center.x, center.y).toFloatArray(), 0, 2)
        setUniform2fv(
                resolutionUniformLocation,
                arrayOf(
                        Gdx.graphics.width.toFloat(),
                        Gdx.graphics.height.toFloat()
                ).toFloatArray(),
                0,
                2
        )
        setUniformf(radiusUniformLocation, radius/maxOf(Gdx.graphics.width, Gdx.graphics.height))
    }
}