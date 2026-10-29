package aeeL.theoSol.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import aeeL.theoSol.R

@Composable
fun rememberTopUpSuccessSound(): TopUpSuccessSound? {
    if (LocalInspectionMode.current) return null
    val context = LocalContext.current.applicationContext
    val sound = remember(context) { TopUpSuccessSound(context) }
    DisposableEffect(sound) {
        onDispose(sound::release)
    }
    return sound
}

class TopUpSuccessSound internal constructor(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private var sampleId = 0

    @Volatile
    private var released = false

    @Volatile
    private var loaded = false

    @Volatile
    private var playWhenReady = false

    init {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loaded = true
                if (playWhenReady) {
                    playWhenReady = false
                    playLoaded()
                }
            }
        }
        sampleId = soundPool.load(context, R.raw.berhasil, 1)
    }

    fun play() {
        if (released) return
        if (loaded) {
            playLoaded()
        } else {
            playWhenReady = true
        }
    }

    fun release() {
        released = true
        playWhenReady = false
        soundPool.release()
    }

    private fun playLoaded() {
        if (released) return
        soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
    }
}
