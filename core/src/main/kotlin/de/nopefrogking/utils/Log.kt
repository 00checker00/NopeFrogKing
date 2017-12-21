package de.nopefrogking.utils

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx

object LOG {
    var debugTag = "[DEBUG]"
    var infoTag  = " [INFO]"
    var errorTag = "[ERROR]"
}

inline fun <reified T: Any> T?.debug(msg: () -> String) { if (Gdx.app.logLevel >= Application.LOG_DEBUG) Gdx.app.debug("${LOG.debugTag} ${T::class.java.name}", msg())}
inline fun <reified T: Any> T?.debug(cause: Throwable, msg: () -> String) { if (Gdx.app.logLevel >= Application.LOG_DEBUG) Gdx.app.debug("${LOG.debugTag} ${T::class.java.name}", msg(), cause)}

inline fun <reified T: Any> T?.info(msg: () -> String) { if (Gdx.app.logLevel >= Application.LOG_DEBUG) Gdx.app.log("${LOG.infoTag} ${T::class.java.name}", msg())}
inline fun <reified T: Any> T?.info(cause: Throwable, msg: () -> String) { if (Gdx.app.logLevel >= Application.LOG_DEBUG) Gdx.app.log("${LOG.infoTag} ${T::class.java.name}", msg(), cause)}

inline fun <reified T: Any> T?.error(msg: () -> String) { if (Gdx.app.logLevel >= Application.LOG_DEBUG) Gdx.app.error("${LOG.errorTag} ${T::class.java.name}", msg())}
inline fun <reified T: Any> T?.error(cause: Throwable, msg: () -> String) { if (Gdx.app.logLevel >= Application.LOG_DEBUG) Gdx.app.error("${LOG.errorTag} ${T::class.java.name}", msg(), cause)}