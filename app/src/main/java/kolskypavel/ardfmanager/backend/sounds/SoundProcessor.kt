package kolskypavel.ardfmanager.backend.sounds

import android.content.Context
import android.media.MediaPlayer
import kolskypavel.ardfmanager.R

object SoundProcessor {
    fun makeSound(context: Context, type: SoundType) {
        val sound = when (type) {
            SoundType.ERROR_UNKNOWN -> R.raw.si_error
            SoundType.DUPLICATE -> R.raw.si_duplicate
            SoundType.RENT -> R.raw.si_rent
        }

        val mediaPlayer = MediaPlayer.create(context, sound)
        mediaPlayer.start()
    }
}