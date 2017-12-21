package de.nopefrogking.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue

class FreetypeSkin: Skin {

    constructor() : super()
    constructor(skinFile: FileHandle) : super(skinFile)
    constructor(skinFile: FileHandle, atlas: TextureAtlas) : super(skinFile, atlas)
    constructor(atlas: TextureAtlas) : super(atlas)


    override fun getJsonLoader(skinFile: FileHandle?): Json? {
        val loader = super.getJsonLoader(skinFile)

        val fontSerializer = loader.getSerializer(BitmapFont::class.java)

        loader.setSerializer(BitmapFont::class.java, object: Json.ReadOnlySerializer<BitmapFont>() {
            override fun read(json: Json, data: JsonValue, type: Class<*>): BitmapFont {
                val file = json.readValue("file", String::class.java, data)

                if (!file.endsWith(".ttf")) {
                    return fontSerializer.read(json, data, type)
                }



                val generator = FreeTypeFontGenerator(Gdx.files.internal(file))


                val params = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                    size = json.readValue("size", Int::class.java, 16, data )
                    color = json.readValue("color", Color::class.java, Color.WHITE, data )
                    borderWidth = json.readValue("borderWidth", Float::class.java, 0.0f, data )
                    borderColor = json.readValue("borderColor", Color::class.java, Color.BLACK, data )
                    borderStraight = json.readValue("borderStraight", Boolean::class.java, false, data )
                    shadowOffsetX = json.readValue("shadowOffsetX", Int::class.java, 0, data )
                    shadowOffsetY = json.readValue("shadowOffsetY", Int::class.java, 0, data )
                    shadowColor = json.readValue("shadowColor", Color::class.java, Color(0.0f, 0.0f, 0.0f, 0.75f), data )
                    characters = json.readValue("characters", String::class.java, FreeTypeFontGenerator.DEFAULT_CHARS, data )
                    kerning = json.readValue("kerning", Boolean::class.java, true, data )
                    flip = json.readValue("flip", Boolean::class.java, false, data )
                    genMipMaps = json.readValue("genMipMaps", Boolean::class.java, false, data )
                }

                val font = generator.generateFont(params)
                generator.dispose()

                return font
            }
        })

        return loader
    }
}