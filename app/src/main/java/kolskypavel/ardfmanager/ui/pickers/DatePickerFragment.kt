package kolskypavel.ardfmanager.ui.pickers

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.time.LocalDate

/** DialogFragment wrapper that returns a selected date through the Fragment Result API. */
class DatePickerFragment : DialogFragment() {
    private val args: DatePickerFragmentArgs by navArgs()

    /** Builds a date picker initialized from navigation arguments. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dateListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                val resultDate = LocalDate.of(year, month + 1, day).toString()
                setFragmentResult(
                    REQUEST_KEY_DATE,
                    bundleOf(BUNDLE_KEY_DATE to resultDate)
                )
            }

        val dateNow = args.curDate
        val initialYear = dateNow.year
        val initialMonth = dateNow.month.value - 1 // DatePickerDialog months are zero-based.
        val initialDay = dateNow.dayOfMonth
        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    /** Fragment Result keys used to return the selected date. */
    companion object {
        const val REQUEST_KEY_DATE = "REQUEST_KEY_DATE"
        const val BUNDLE_KEY_DATE = "BUNDLE_KEY_DATE"
    }
}
