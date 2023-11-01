package kolskypavel.ardfmanager.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.room.entitity.EventBand
import kolskypavel.ardfmanager.room.entitity.EventLevel
import kolskypavel.ardfmanager.room.entitity.EventType
import kolskypavel.ardfmanager.ui.pickers.DatePickerFragment
import kolskypavel.ardfmanager.ui.pickers.TimePickerFragment
import java.time.LocalDate
import java.time.LocalTime

class EventCreateDialogFragment : DialogFragment() {
    private val args: EventCreateDialogFragmentArgs by navArgs()
    private lateinit var nameEditText: TextInputEditText
    private lateinit var dateView: TextInputEditText
    private lateinit var startTimeView: TextInputEditText
    private lateinit var eventTypePicker: MaterialAutoCompleteTextView
    private lateinit var eventLevelPicker: MaterialAutoCompleteTextView
    private lateinit var eventBandPicker: MaterialAutoCompleteTextView

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    private lateinit var curDate: LocalDate
    private lateinit var curStartTime: LocalTime

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.event_dialog)

        nameEditText = view.findViewById(R.id.event_dialog_name)
        dateView = view.findViewById(R.id.event_dialog_date)
        startTimeView = view.findViewById(R.id.event_dialog_start_time)
        eventTypePicker = view.findViewById(R.id.event_dialog_type)
        eventLevelPicker = view.findViewById(R.id.event_dialog_level)
        eventBandPicker = view.findViewById(R.id.event_dialog_band)
        cancelButton = view.findViewById(R.id.event_dialog_cancel)
        okButton = view.findViewById(R.id.event_dialog_ok)

        eventTypePicker.isSaveEnabled = false
        eventLevelPicker.isSaveEnabled = false
        eventBandPicker.isSaveEnabled = false


        populateFields()
        setButtons()
        setPickers()
    }

    private fun setPickers() {
        dateView.setOnClickListener {
            findNavController().navigate(EventCreateDialogFragmentDirections.selectDate(curDate))
        }
        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->

            curDate = LocalDate.parse(bundle.getString(DatePickerFragment.BUNDLE_KEY_DATE))
            dateView.setText(curDate.toString())
        }

        startTimeView.setOnClickListener {
            findNavController().navigate(EventCreateDialogFragmentDirections.selectTime(curStartTime))
        }
        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_TIME) { _, bundle ->
            curStartTime = LocalTime.parse(bundle.getString(TimePickerFragment.BUNDLE_KEY_TIME))
            startTimeView.setText(curStartTime.toString())
        }
    }

    private fun populateFields() {

        //Create new event
        if (args.create) {
            curDate = LocalDate.now()
            curStartTime = LocalTime.now()

            dialog?.setTitle(R.string.create_event)
            dateView.setText(curDate.toString())
            startTimeView.setText(LocalTime.now().hour.toString() + ":" + LocalTime.now().minute.toString())
            eventTypePicker.setText(getText(R.string.event_type_0), false)
            eventLevelPicker.setText(getText(R.string.event_level_3), false)
            eventBandPicker.setText(getText(R.string.band_80m), false)
        }

        //Edit existing event
        else {
            curDate = args.eventDate!!
            curStartTime = args.eventStartTime!!

            dialog?.setTitle(R.string.edit_event)
            nameEditText.setText(args.eventName)
            dateView.setText(args.eventDate.toString())
            startTimeView.setText(args.eventStartTime.toString())
            eventTypePicker.setText(args.eventType.toString())
            eventLevelPicker.setText(args.eventLevel.toString())
            eventBandPicker.setText(args.eventBand.toString())
        }
    }

    private fun setButtons() {
        okButton.setOnClickListener {

            //Send the arguments to create a new event
            if (checkValidity()) {

                val eventTypePos =
                    requireActivity().resources.getStringArray(R.array.event_types)
                        .indexOf(eventTypePicker.text.toString())

                val eventLevelPos = requireActivity().resources.getStringArray(R.array.levels)
                    .indexOf(eventLevelPicker.text.toString())

                val eventBandPos =
                    requireActivity().resources.getStringArray(R.array.bands)
                        .indexOf(eventBandPicker.text.toString())

                val eventType = EventType.getByValue(eventTypePos)
                val eventLevel = EventLevel.getByValue(eventLevelPos)
                val eventBand = EventBand.getByValue(eventBandPos)

                setFragmentResult(
                    REQUEST_EVENT_MODIFICATION, bundleOf(
                        BUNDLE_KEY_CREATE to args.create,
                        BUNDLE_KEY_POSITION to args.position,
                        BUNDLE_KEY_EVENT_NAME to nameEditText.text.toString(),
                        BUNDLE_KEY_EVENT_DATE to curDate.toString(),
                        BUNDLE_KEY_EVENT_START_TIME to curStartTime.toString(),
                        BUNDLE_KEY_EVENT_TYPE to eventType,
                        BUNDLE_KEY_EVENT_LEVEL to eventLevel,
                        BUNDLE_KEY_EVENT_BAND to eventBand
                    )
                )
                //End the dialog
                dialog?.dismiss()
            }
        }
        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    /**
     * Check if all the provided fields are valid
     */
    private fun checkValidity(): Boolean {
        var valid = true

        if (nameEditText.text?.isBlank() == true) {
            nameEditText.error = getString(R.string.required)
            valid = false
        }
        return valid
    }

    companion object {
        const val REQUEST_EVENT_MODIFICATION = "REQUEST_EVENT_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_POSITION = "BUNDLE_KEY_POSITION"
        const val BUNDLE_KEY_EVENT_NAME = "BUNDLE_KEY_EVENT_NAME"
        const val BUNDLE_KEY_EVENT_DATE = "BUNDLE_KEY_EVENT_DATE"
        const val BUNDLE_KEY_EVENT_START_TIME = "BUNDLE_KEY_EVENT_EVENT_START_TIME"
        const val BUNDLE_KEY_EVENT_TYPE = "BUNDLE_KEY_EVENT_EVENT_TYPE"
        const val BUNDLE_KEY_EVENT_LEVEL = "BUNDLE_KEY_EVENT_EVENT_LEVEL"
        const val BUNDLE_KEY_EVENT_BAND = "BUNDLE_KEY_EVENT_EVENT_BAND"
    }
}