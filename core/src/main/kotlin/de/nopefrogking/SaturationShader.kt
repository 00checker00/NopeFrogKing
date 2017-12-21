package de.nopefrogking

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import de.nopefrogking.utils.insertShaderVars
import org.intellij.lang.annotations.Language

@Language("GLSL")
private val vertexShader = """
        attribute vec4 _POSITION_ATTRIBUTE;
        attribute vec4 _COLOR_ATTRIBUTE;
        attribute vec2 _TEXCOORD_ATTRIBUTE0;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoords;

        void main()
        {
            v_color = _COLOR_ATTRIBUTE;
            v_color.a = v_color.a * (255.0/254.0);
            v_texCoords = _TEXCOORD_ATTRIBUTE0;
            gl_Position =  u_projTrans * _POSITION_ATTRIBUTE;
        }
    """.insertShaderVars()

@Language("GLSL")
private val fragmentShader = """
        #ifdef GL_ES
        #define LOWP lowp
        precision mediump float;
        #else
        #define LOWP
        #endif
        varying LOWP vec4 v_color;
        varying vec2 v_texCoords;
        uniform sampler2D u_texture;

        uniform float u_saturation;
        void main()
        {
            vec4 color = v_color * texture2D(u_texture, v_texCoords);

            float intensity = 0.3 * color.r + 0.59 * color.g + 0.11 * color.b;
            vec4 grayscale = vec4(intensity, intensity, intensity, color.a);

            gl_FragColor = color * u_saturation + grayscale * (1.0 - u_saturation);
        }
        """.insertShaderVars()



class SaturationShader: ShaderProgram(vertexShader, fragmentShader) {
    var saturation = 1.0f
    val saturationUniformLocation: Int

    init {
        if (!isCompiled) throw IllegalArgumentException("Error compiling shader: " + log)
        begin()
        saturationUniformLocation = getUniformLocation("u_saturation")
        end()
    }

    override fun begin() {
        super.begin()
        setUniformf(saturationUniformLocation, saturation)
    }
}