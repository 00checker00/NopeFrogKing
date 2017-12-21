package de.nopefrogking.android

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.AbsoluteLayout
import com.badlogic.gdx.math.Rectangle


class VideoView (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): AbsoluteLayout(context, attrs, defStyleAttr) {
    private var surface: SurfaceView? = null
    private val mediaPlayer = MediaPlayer()

    var onVideoFinishedCallback: (()->Unit)? = null

    init {
        setOnTouchListener { view, motionEvent ->
            mediaPlayer.stop()
            onVideoFinished()

            true
        }

        mediaPlayer.setOnCompletionListener {
            onVideoFinished()
        }

        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }

        this.visibility = View.GONE
    }

    fun onVideoFinished() {
        onVideoFinishedCallback?.invoke()
        this.visibility = View.GONE

        removeView(surface)
        surface = null

        mediaPlayer.reset()
    }

    fun showVideo(file: String, bounds: Rectangle) {
        val afd = context.assets.openFd(file)
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mediaPlayer.prepareAsync()

        surface = SurfaceView(context).apply {
            holder.addCallback(object: SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    mediaPlayer.setDisplay(null)
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    mediaPlayer.setDisplay(holder)
                }
            })
        }
        addView(surface, AbsoluteLayout.LayoutParams(
            bounds.width.toInt(),
            bounds.height.toInt(),
            bounds.x.toInt(),
            bounds.y.toInt()
        ))

        this.visibility = View.VISIBLE
    }
}