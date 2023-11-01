package kolskypavel.ardfmanager.ui.pickers

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.time.LocalTime

class TimePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val timeListener =
            TimePickerDialog.OnTimeSetListener { _: TimePicker, hour: Int, minute: Int ->
                val resTime = LocalTime.of(hour, minute).toString()
                setFragmentResult(
                    REQUEST_KEY_TIME,
                    bundleOf(BUNDLE_KEY_TIME to resTime)
                )
            }
        val localTime = LocalTime.now()
        val hour: Int = localTime.hour
        val minute: Int = localTime.minute

        return TimePickerDialog(
            requireContext(),
            timeListener,
            hour,
            minute,
            true
        )
    }

    companion object {
        const val REQUEST_KEY_TIME = "REQUEST_KEY_TIME"
        const val BUNDLE_KEY_TIME = "BUNDLE_KEY_TIME"
    }
}