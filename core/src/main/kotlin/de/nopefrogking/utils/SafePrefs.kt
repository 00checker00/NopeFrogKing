package de.nopefrogking.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Base64Coder
import de.nopefrogking.Item
import kotlin.reflect.KProperty

internal class TypedPreference<T> (val default: T) {
    fun put(prefs: Preferences, key: String, value: T) {
        prefs.put(mapOf(key to value))
    }

    fun get(prefs: Preferences, key: String): T {
        return prefs.get()[key] as? T ?: default
    }

    internal class Delegate<T>(val prefs: Preferences, val pref: TypedPreference<T>, val onChange: (()->Unit)? = null) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return pref.get(prefs, Base64Coder.encodeString(property.name))
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            pref.put(prefs, Base64Coder.encodeString(property.name), value)
            onChange?.invoke()
        }
    }

    companion object {
        internal fun <T> fromPrefs(prefs: Preferences, default: T, onChange: (()->Unit)? = null)
                = Delegate(prefs, TypedPreference(default), onChange)
    }
}

internal fun <T> Preferences.bind(default: T, onChange: (()->Unit)? = null)
        = TypedPreference.fromPrefs(this, default, onChange)

class SafePreferencesWrapper(private val prefs: Preferences) {
    private var hash by TypedPreference.fromPrefs(prefs, 0)
    var changed: Boolean = false
        private set

    var gold by prefs.bind(0L) { changed = true }
    private var itemFlask by prefs.bind(0) { changed = true }
    private var itemOrb by prefs.bind(0) { changed = true }
    private var itemStorm by prefs.bind(0) { changed = true }
    private var itemUmbrella by prefs.bind(0) { changed = true }
    var presents by prefs.bind(0) { changed = true }
    var highscore by prefs.bind(0L) { changed = true }

    interface Items {
        operator fun get(item: Item): Int
        operator fun set(item: Item, value: Int)
    }
    val item = object: Items {
        override operator fun get(item: Item) = when (item) {
            Item.Flask -> itemFlask
            Item.Orb -> itemOrb
            Item.Storm -> itemStorm
            Item.Umbrella -> itemUmbrella
        }

        override operator fun set(item: Item, value: Int) {
            when (item) {
                Item.Flask -> itemFlask = value
                Item.Orb -> itemOrb = value
                Item.Storm -> itemStorm = value
                Item.Umbrella -> itemUmbrella = value
            }
        }
    }

    fun reset() {
        gold = 0
        itemFlask = 0
        itemOrb = 0
        itemStorm = 0
        itemUmbrella = 0
        presents = 0
        highscore = 0

        changed = true
    }

    private fun calcHash() = arrayOf(gold, itemFlask, itemOrb, itemStorm, itemUmbrella, presents, highscore)
            .fold(17) { cur, next -> cur * 31 + next.hashCode()}

    internal fun flush() {
        prefs.flush()

        if (changed) {
            hash = calcHash()
            prefs.flush()
        }

    }

    internal fun checkHash() {
        if (calcHash() != hash) {
            debug { "Hash invalid, resetting preferences (${calcHash()} != $hash)" }
            reset()
            flush()
        }
    }
}

fun <T> SafePreferences(init: SafePreferencesWrapper.()->T): T {
    val prefs = Gdx.app.getPreferences("de.nopefrogking.settings")
    return SafePreferencesWrapper(prefs).let {
        it.checkHash()
        val result = it.init()
        it.flush()
        result
    }
}