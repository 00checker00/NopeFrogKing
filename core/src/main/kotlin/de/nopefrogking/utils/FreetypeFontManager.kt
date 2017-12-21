package de.nopefrogking.utils

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Array as GdxArray


class FreetypeFontManager: Disposable {
    class FontParams: FreeTypeFontGenerator.FreeTypeFontParameter() {
        var name: String = "unnamed"
        var file: FileHandle? = null
    }
    data class FreetypeFont(val params: FontParams): Disposable {
        internal var _font: BitmapFont? = null

        val font
            get() = _font ?: throw kotlin.RuntimeException("Trying to access \"${params.name}\" font: ${params.file?.path()}, but it isn't loaded yet")

        val isLoaded get() = _font != null

        operator fun invoke() = font

        override fun dispose() {
            _font?.dispose()
        }

        internal val packer = PixmapPacker(FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, Pixmap.Format.RGBA8888, 2, false)

        internal val regions: GdxArray<TextureRegion> by lazy {
            val array = GdxArray<TextureRegion>()

            packer.updateTextureRegions(array, Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false)

            array
        }
    }
    private class GenerateTask(val font: FreetypeFont)
    private class PackTask(val font: FreetypeFont, val fontData: BitmapFont.BitmapFontData)

    private var disposed = false

    private val generateLock = Object()
    private val generateQueue = Queue<GenerateTask>(QUEUE_SIZE)

    private val packLock = Object()
    private val packQueue = Queue<PackTask>(QUEUE_SIZE)

    private val managedFonts = GdxArray<FreetypeFont>()

    private val worker = Thread({
        run loop@ {
            while (true) {
                var task: GenerateTask? = null
                synchronized(generateLock) {
                    while (generateQueue.size == 0 && !disposed) {
                        generateLock.wait()
                    }
                    if (disposed) return@loop
                    task = generateQueue.first()
                }
                task?.let { task ->
                    val start = System.currentTimeMillis()
                    val gen = FreeTypeFontGenerator(task.font.params.file)

                    val params = task.font.params
                    params.packer = task.font.packer
                    val data = gen.generateData(params)

                    gen.dispose()

                    val time = System.currentTimeMillis() - start
                    debug { "Generating font \"${task.font.params.file?.name()}\" took ${time}ms" }

                    synchronized(packLock) {
                        packQueue.addLast(PackTask(task.font, data))
                        packLock.notifyAll()
                    }
                }
                generateQueue.removeFirst()
            }
        }
    }, "Font Generator Thread")

    init {
        worker.start()
    }

    fun load(params: FontParams): FreetypeFont {
        val font = FreetypeFont(params)

        managedFonts.add(font)

        synchronized(generateLock) {
            generateQueue.addLast(GenerateTask(font))
            generateLock.notifyAll()
        }

        return font
    }

    val progress: Float get() {
        return managedFonts.count { it.isLoaded }.toFloat()/managedFonts.count().toFloat()
    }

    fun load(params: FontParams.()->Unit)
            = load(FontParams().apply { params() })

    private val isFinished: Boolean get() {
        synchronized(packLock) {
            synchronized(generateLock) {
                if (packQueue.size == 0 && generateQueue.size == 0) {
                    return true
                }
            }
        }
        return false
    }

    fun update(): Boolean {
        var task: PackTask? = null
        synchronized(packLock) {
            if (packQueue.size > 0) {
                task = packQueue.removeFirst()
            }
        }
        return task?.let { task ->
            val start = System.currentTimeMillis()

            val data = task.fontData

            val font = BitmapFont(data, task.font.regions, false)
            task.font._font = font

            task.font.packer.dispose()

            val time = System.currentTimeMillis() - start
            debug { "Packing font \"${task.font.params.file?.name()}\" took ${time}ms" }

            false
        } ?: isFinished
    }

    override fun dispose() {
        disposed = true
        synchronized(generateLock) {
            generateLock.notifyAll()
        }
    }

    companion object {
        val FONT_ATLAS_WIDTH = 1024
        val FONT_ATLAS_HEIGHT = 512

        val QUEUE_SIZE = 20
    }
}