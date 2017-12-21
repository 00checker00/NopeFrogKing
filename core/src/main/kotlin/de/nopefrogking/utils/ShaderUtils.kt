package de.nopefrogking.utils

import com.badlogic.gdx.graphics.glutils.ShaderProgram

internal fun String.insertShaderVars(): String =
    replace("_POSITION_ATTRIBUTE", ShaderProgram.POSITION_ATTRIBUTE)
            .replace("_NORMAL_ATTRIBUTE", ShaderProgram.NORMAL_ATTRIBUTE)
            .replace("_COLOR_ATTRIBUTE", ShaderProgram.COLOR_ATTRIBUTE)
            .replace("_TEXCOORD_ATTRIBUTE", ShaderProgram.TEXCOORD_ATTRIBUTE)
            .replace("_TANGENT_ATTRIBUTE", ShaderProgram.TANGENT_ATTRIBUTE)
            .replace("_BINORMAL_ATTRIBUTE", ShaderProgram.BINORMAL_ATTRIBUTE)
            .replace("_BONEWEIGHT_ATTRIBUTE", ShaderProgram.BONEWEIGHT_ATTRIBUTE)