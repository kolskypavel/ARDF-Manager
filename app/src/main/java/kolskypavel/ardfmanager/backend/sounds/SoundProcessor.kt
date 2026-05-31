package kolskypavel.ardfmanager.backend.sounds

import android.content.Context
import android.media.MediaPlayer
import kolskypavel.ardfmanager.R

/** Plays short user-feedback sounds for readout outcomes. */
object SoundProcessor {
    /** Plays the sound associated with the supplied readout event type. */
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
