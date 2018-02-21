package de.nopefrogking


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

/** Generated from assets/sounds dir. */
enum class Sounds(val path: String) {
    click("sounds/click.wav"),
    Crush_01("sounds/Crush_01.wav"),
    Crush_02("sounds/Crush_02.wav"),
    Crush_03("sounds/Crush_03.wav"),
    Crush_04("sounds/Crush_04.wav"),
    Crush_05("sounds/Crush_05.wav"),
    Crush_06("sounds/Crush_06.wav"),
    Crush_07("sounds/Crush_07.wav"),
    frog_ribit("sounds/frog_ribit.wav"),
    hinweis("sounds/hinweis.mp3"),
    item_orb("sounds/item_orb.wav"),
    item_storm_hit("sounds/item_storm_hit.wav"),
    item_storm_start("sounds/item_storm_start.wav"),
    kiss("sounds/kiss.wav"),
    princess_cry("sounds/princess_cry.wav"),
    princess_get_hit("sounds/princess_get_hit.mp3"),
    princ_get_hit("sounds/princ_get_hit.wav"),
    Scratch_01("sounds/Scratch_01.wav"),
    Scratch_02("sounds/Scratch_02.wav"),
    time_normal("sounds/time_normal.mp3"),
    time_slow("sounds/time_slow.mp3"),
    ;

    val sound: Sound by lazy { Assets.manager.get(path, Sound::class.java) }

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun invoke() = sound
}

