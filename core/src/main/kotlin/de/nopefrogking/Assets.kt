package de.nopefrogking

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.ObjectMap
import de.nopefrogking.utils.*
import ktx.assets.load
import ktx.assets.toInternalFile
import ktx.collections.toGdxArray
import ktx.collections.toGdxMap
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty
import com.badlogic.gdx.utils.Array as GdxArray

data class AnimationDescriptor(val region: String, val frameTime: Float = 0.13f, val playMode: Animation.PlayMode = Animation.PlayMode.NORMAL)

class LoadedAssets internal constructor() {
    fun findRegion(name: String, index: Int = -1): TextureRegion
            = Assets.findRegion(name, index) ?: throw RuntimeException("Couldn't find region $name with index $index")

    fun findRegions(name: String): GdxArray<TextureRegion>
            = Assets.findRegions(name) ?: throw RuntimeException("Couldn't find regions $name")
}

object Assets {
    val atlasFile = "spritesheets/$scaleString/sprites.atlas"
    val atlas = load<TextureAtlas>(atlasFile)
    val manager get() = ktx.assets.Assets.manager
    val fontManager = FreetypeFontManager()

    var onReloadCallback: (()->Unit)? = null

    internal val managedAssetDelegates = GdxArray<WeakReference<GetAssetDelegate<*>>>()
    internal val regions = ObjectMap<String, IntMap<out TextureRegion>>()

    init {
        manager.setLoader(ParticleEffect::class.java, ParticleEffectLoader(InternalFileHandleResolver()))
    }

    fun findRegion(name: String, index: Int = -1): TextureRegion? {
        if(atlas.isLoaded()) {

            if (!regions.containsKey(name)) {
                // Find and cache the regions corresponding to this name
                findRegions(name)
            }

            if (!regions.containsKey(name)) {
                error { "Couldn't find region $name" }
                return null
            }

            val map = regions[name]

            if (!map.containsKey(index)) {
                error { "Region $name doesn't have index $index" }
                return null
            }

            return map[index]
        } else {
            return null
        }
    }

    fun findRegions(name: String): GdxArray<TextureRegion>? {
        if(atlas.isLoaded()) {
            if(!regions.containsKey(name)) {
                val newRegions = atlas.asset.findRegions(name)
                if (newRegions.size == 0) {
                    error { "Couldn't find regions $name"}
                    return null
                }
                val map = IntMap<TextureRegion>()
                newRegions.forEach { map.put(it.index, it) }
                this.regions.put(name, map)
            }
            val foundRegions = regions.get(name).map { it.value }.filterNotNull().toGdxArray()

            @Suppress("UNCHECKED_CAST")
            return foundRegions
        } else {
            return null
        }
    }

    fun reloadAssets() {
        managedAssetDelegates.removeAll { it.get() == null }
        managedAssetDelegates.forEach {
            it.get()?.reload()
        }
        regions.clear()
        onReloadCallback?.invoke()
    }

    fun preloadAssets(vararg assets: AssetDescriptor<*>) {
        assets.forEach { manager.load(it) }
    }

    abstract class GetAssetDelegate<T> internal constructor(val callback: ((T?)->Unit)? = null) {
        var asset: T? = null

        var invalid = false

        abstract fun createAsset(): T?

        fun reload() {
            asset = null
            invalid = false
        }

        init {
            Assets.managedAssetDelegates.add(WeakReference(this))
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = when {
            !atlas.isLoaded() -> null
            else ->  kotlin.run {
                if (asset == null && !invalid) {
                    asset = createAsset().apply { callback?.invoke(this) }
                    if (asset == null)
                        invalid = true
                }
                asset
            }
        }
    }

    internal class GetAnimationDelegate(val name: String,
                                        val frameDuration: Float = 0.13f,
                                        val playMode: Animation.PlayMode = Animation.PlayMode.NORMAL,
                                        callback: ((Animation<TextureRegion>?)->Unit)? = null): GetAssetDelegate<Animation<TextureRegion>>(callback) {

        override fun createAsset(): Animation<TextureRegion>? {
            val regions = findRegions(name)
            return when {
                regions == null || regions.size == 0 -> null
                else -> Animation(frameDuration, regions, playMode)
            }
        }
    }

    internal class GetAnimationsByNameDelegate(val names: Array<out String>,
                                         val frameDuration: Float = 0.13f,
                                         val playMode: Animation.PlayMode = Animation.PlayMode.NORMAL,
                                         callback: ((ObjectMap<String, Animation<TextureRegion>?>?)->Unit)? = null): GetAssetDelegate<ObjectMap<String, Animation<TextureRegion>?>>(callback) {

        override fun createAsset(): ObjectMap<String, Animation<TextureRegion>?>? =
            names.map {
                Pair(it, findRegions(it)?.let { Animation(frameDuration, it, playMode) })
            }.toGdxMap(
                valueProvider = { (key, value) -> value},
                keyProvider =  { (key, value) -> key}
            )
    }

    internal class GetAnimationsByDescriptorDelegate(val animations: Array<out AnimationDescriptor>, callback: ((ObjectMap<String, Animation<TextureRegion>?>?)->Unit)? = null):
            GetAssetDelegate<ObjectMap<String, Animation<TextureRegion>?>>(callback) {

        override fun createAsset(): ObjectMap<String, Animation<TextureRegion>?>? =
            animations.map { (region, frameDuration, playMode) ->
                Pair(region, findRegions(region)?.let { Animation(frameDuration, it, playMode) })
            }.toGdxMap(
                valueProvider = { (key, value) -> value},
                keyProvider =  { (key, value) -> key}
            )
    }

    internal class GetRegionDelegate(val name: String, val index: Int = -1, callback: ((TextureRegion?)->Unit)? = null): GetAssetDelegate<TextureRegion>(callback) {
        override fun createAsset(): TextureRegion? = findRegion(name, index)
    }

    internal class GetRegionsDelegate(val name: String, callback: ((GdxArray<TextureRegion>?)->Unit)? = null): GetAssetDelegate<GdxArray<TextureRegion>>(callback) {
        override fun createAsset(): GdxArray<TextureRegion>? = findRegions(name)
    }

    internal class GetMultipleRegionsDelegate(val names: Array<out String>, callback: ((ObjectMap<String, GdxArray<TextureRegion>>?) -> Unit)? = null): GetAssetDelegate<ObjectMap<String, GdxArray<TextureRegion>>>(callback) {
        override fun createAsset(): ObjectMap<String, GdxArray<TextureRegion>>?
            = names.map { Pair(it, findRegions(it)?:GdxArray()) }.toGdxMap(
                valueProvider = { it -> it.second},
                keyProvider =  { it -> it.first}
        )
    }

    internal class GetParticlePoolDelegate(val file: FileHandle,
                                           val initialCapacity: Int = 16,
                                           val max: Int = Integer.MAX_VALUE,
                                           callback: ((ParticleEffectPool?)->Unit)? = null): GetAssetDelegate<ParticleEffectPool>(callback) {

        override fun createAsset(): ParticleEffectPool? {
            val effect = manager.get(file.path(), ParticleEffect::class.java)
            return ParticleEffectPool(effect, initialCapacity, max)
        }
    }

    internal class GetMusicDelegate(val file: FileHandle,
                                           callback: ((Music?)->Unit)? = null): GetAssetDelegate<Music>(callback) {

        override fun createAsset(): Music? {
            val music = manager.get(file.path(), Music::class.java)
            return music
        }
    }

    internal class GetSoundDelegate(val file: FileHandle,
                                           callback: ((Sound?)->Unit)? = null): GetAssetDelegate<Sound>(callback) {

        override fun createAsset(): Sound? {
            return manager.get(file.path(), Sound::class.java)
        }
    }


    fun getAnimation(name: String,
                     frameDuration: Float = 0.04f,
                     playMode: Animation.PlayMode = Animation.PlayMode.NORMAL,
                     callback: ((Animation<TextureRegion>?) -> Unit)? = null): GetAssetDelegate<Animation<TextureRegion>>
            = GetAnimationDelegate(name, frameDuration, playMode, callback)

    fun getAnimations(vararg names: String,
                     frameDuration: Float = 0.04f,
                     playMode: Animation.PlayMode = Animation.PlayMode.NORMAL,
                     callback: ((ObjectMap<String, Animation<TextureRegion>?>?) -> Unit)? = null): GetAssetDelegate<ObjectMap<String, Animation<TextureRegion>?>>
            = GetAnimationsByNameDelegate(names, frameDuration, playMode, callback)

    fun getAnimations(vararg animations: AnimationDescriptor, callback: ((ObjectMap<String, Animation<TextureRegion>?>?) -> Unit)? = null): GetAssetDelegate<ObjectMap<String, Animation<TextureRegion>?>>
            = GetAnimationsByDescriptorDelegate(animations, callback)

    fun getRegion(name: String, index: Int = -1, callback: ((TextureRegion?) -> Unit)? = null): GetAssetDelegate<TextureRegion>
            = GetRegionDelegate(name, index, callback)

    fun getRegions(name: String, callback: ((GdxArray<TextureRegion>?) -> Unit)? = null): GetAssetDelegate<GdxArray<TextureRegion>>
            = GetRegionsDelegate(name, callback)

    fun getRegions(vararg names: String, callback: ((ObjectMap<String, GdxArray<TextureRegion>>?) -> Unit)? = null): GetAssetDelegate<ObjectMap<String, GdxArray<TextureRegion>>>
            = GetMultipleRegionsDelegate(names, callback)

    fun getParticleEffect(file: FileHandle,
                          initialCapacity: Int = 16,
                          max: Int = Integer.MAX_VALUE,
                          callback: ((ParticleEffectPool?) -> Unit)? = null): GetAssetDelegate<ParticleEffectPool>
            = GetParticlePoolDelegate(file, initialCapacity, max, callback)

    fun getParticleEffect(file: String,
                          initialCapacity: Int = 16,
                          max: Int = Integer.MAX_VALUE,
                          callback: ((ParticleEffectPool?) -> Unit)? = null): GetAssetDelegate<ParticleEffectPool>
            = GetParticlePoolDelegate(file.toInternalFile(), initialCapacity, max, callback)

    fun getMusic(file: String,
                          callback: ((Music?) -> Unit)? = null): GetAssetDelegate<Music>
            = GetMusicDelegate(file.toInternalFile(), callback)

    fun getSound(file: String,
                          callback: ((Sound?) -> Unit)? = null): GetAssetDelegate<Sound>
            = GetSoundDelegate(file.toInternalFile(), callback)

    fun <T> getAsset(create: LoadedAssets.()->T, callback: ((T?) -> Unit)? = null)
        = object: GetAssetDelegate<T>(callback) {
        override fun createAsset() = LoadedAssets().create()
    }

    fun dispose() {
        fontManager.dispose()
    }
}

infix fun <T> Class<T>.assetFrom(name: String) = AssetDescriptor<T>(name, this)
infix fun <T> AssetDescriptor<T>.with(params: AssetLoaderParameters<T>) = AssetDescriptor(fileName, type, params)