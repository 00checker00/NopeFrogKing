package de.nopefrogking.utils

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array

class ParticleEffect: com.badlogic.gdx.graphics.g2d.ParticleEffect() {
    override fun loadEmitterImages(atlas: TextureAtlas, atlasPrefix: String?) {
        for (emitter in emitters) {
            var imagePath = emitter.imagePath ?: continue
            val lastDotIndex = imagePath.lastIndexOf('.')
            if (lastDotIndex != -1) imagePath = imagePath.substring(0, lastDotIndex)
            if (atlasPrefix != null) imagePath = atlasPrefix + imagePath
            val sprite = atlas.createSprite(imagePath) ?: throw IllegalArgumentException("SpriteSheet missing image: " + imagePath)
            emitter.sprite = sprite
        }
    }
}

class ParticleEffectLoader(resolver: FileHandleResolver) : SynchronousAssetLoader<ParticleEffect, ParticleEffectLoader.ParticleEffectParameter>(resolver) {

    override fun load(am: AssetManager, fileName: String, file: FileHandle, param: ParticleEffectParameter?): ParticleEffect {
        val effect = ParticleEffect()
        if (param != null && param.atlasFile != null)
            effect.load(file, am.get(param.atlasFile, TextureAtlas::class.java), param.atlasPrefix)
        else if (param != null && param.imagesDir != null)
            effect.load(file, param.imagesDir)
        else
            effect.load(file, file.parent())
        return effect
    }

    override fun getDependencies(fileName: String, file: FileHandle, param: ParticleEffectParameter?): Array<AssetDescriptor<*>> {
        var deps: Array<AssetDescriptor<*>>? = null
        if (param != null && param.atlasFile != null) {
            deps = Array()
            deps.add(AssetDescriptor(param.atlasFile!!, TextureAtlas::class.java))
        }
        return deps!!
    }

    /** Parameter to be passed to [AssetManager.load] if additional configuration is
     * necessary for the [ParticleEffect].  */
    class ParticleEffectParameter : AssetLoaderParameters<ParticleEffect>() {
        /** Atlas file name.  */
        var atlasFile: String? = null
        /** Optional prefix to image names  */
        var atlasPrefix: String? = null
        /** Image directory.  */
        var imagesDir: FileHandle? = null
    }
}
