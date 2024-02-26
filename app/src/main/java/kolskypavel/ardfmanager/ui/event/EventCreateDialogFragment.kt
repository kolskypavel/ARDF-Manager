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
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.enums.EventBand
import kolskypavel.ardfmanager.backend.room.enums.EventLevel
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.FinishTimeSource
import kolskypavel.ardfmanager.backend.room.enums.StartTimeSource
import kolskypavel.ardfmanager.ui.pickers.DatePickerFragment
import kolskypavel.ardfmanager.ui.pickers.TimePickerFragment
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class EventCreateDialogFragment : DialogFragment() {
    private val args: EventCreateDialogFragmentArgs by navArgs()
    private val dataProcessor = DataProcessor.get()

    private lateinit var nameEditText: TextInputEditText
    private lateinit var externalIdEditText: TextInputEditText
    private lateinit var dateView: TextInputEditText
    private lateinit var startTimeView: TextInputEditText
    private lateinit var limitEditText: TextInputEditText
    private lateinit var eventTypePicker: MaterialAutoCompleteTextView
    private lateinit var eventLevelPicker: MaterialAutoCompleteTextView
    private lateinit var eventBandPicker: MaterialAutoCompleteTextView
    private lateinit var startTimeSourcePicker: MaterialAutoCompleteTextView
    private lateinit var finishTimeSourcePicker: MaterialAutoCompleteTextView

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    private lateinit var event: Event

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)

        nameEditText = view.findViewById(R.id.event_dialog_name)
        externalIdEditText = view.findViewById(R.id.event_dialog_external_id)
        dateView = view.findViewById(R.id.event_dialog_date)
        startTimeView = view.findViewById(R.id.event_dialog_start_time)
        limitEditText = view.findViewById(R.id.event_dialog_limit)
        eventTypePicker = view.findViewById(R.id.category_dialog_type)
        eventLevelPicker = view.findViewById(R.id.event_dialog_level)
        eventBandPicker = view.findViewById(R.id.event_dialog_band)
        startTimeSourcePicker = view.findViewById(R.id.event_dialog_start_time_source)
        finishTimeSourcePicker = view.findViewById(R.id.event_dialog_finish_time_source)
        cancelButton = view.findViewById(R.id.event_dialog_cancel)
        okButton = view.findViewById(R.id.event_dialog_ok)


        //TODO: Process the saving - this is just to prevent the filtering after screen rotation
        eventTypePicker.isSaveEnabled = false
        eventLevelPicker.isSaveEnabled = false
        eventBandPicker.isSaveEnabled = false
        startTimeSourcePicker.isSaveEnabled = false
        finishTimeSourcePicker.isSaveEnabled = false

        populateFields()
        setButtons()
        setPickers()
    }

    /**
     * Set the date and time picker in an external dialog
     */
    private fun setPickers() {
        dateView.setOnClickListener {
            findNavController().navigate(EventCreateDialogFragmentDirections.selectDate(event.date))
        }
        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->

            event.date = LocalDate.parse(bundle.getString(DatePickerFragment.BUNDLE_KEY_DATE))
            dateView.setText(event.date.toString())
        }

        startTimeView.setOnClickListener {
            findNavController().navigate(EventCreateDialogFragmentDirections.selectTime(event.startTime))
        }
        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_TIME) { _, bundle ->
            event.startTime = LocalTime.parse(bundle.getString(TimePickerFragment.BUNDLE_KEY_TIME))
            startTimeView.setText(event.startTime.toString())
        }
    }

    private fun populateFields() {

        //Create new event
        if (args.create) {
            dialog?.setTitle(R.string.event_create)
            event = Event(
                UUID.randomUUID(),
                "", null, LocalDate.now(),
                LocalTime.now(),
                EventType.CLASSICS,
                EventLevel.PRACTICE,
                EventBand.M80,
                Duration.ofMinutes(120),
                StartTimeSource.DRAWN_TIME,
                FinishTimeSource.FINISH_CONTROL
            )
        } else {
            event = args.event!!
            dialog?.setTitle(R.string.event_edit)
            nameEditText.setText(event.name)
        }

        dateView.setText(event.date.toString())
        if (event.externalId != null) {
            externalIdEditText.setText(event.externalId.toString())
        }
        startTimeView.setText(dataProcessor.getHoursMinutesFromTime(event.startTime))
        limitEditText.setText("120") //TODO: Fix with default values from settings

        eventTypePicker.setText(dataProcessor.eventTypeToString(event.eventType), false)
        eventLevelPicker.setText(dataProcessor.eventLevelToString(event.eventLevel), false)
        eventBandPicker.setText(dataProcessor.eventBandToString(event.eventBand), false)
        startTimeSourcePicker.setText(
            dataProcessor.startTimeSourceToString(event.startTimeSource),
            false
        )
        finishTimeSourcePicker.setText(
            dataProcessor.finishTimeSourceToString(event.finishTimeSource),
            false
        )

    }

    private fun setButtons() {
        okButton.setOnClickListener {

            //Send the arguments to create a new event
            if (checkValidity()) {

                event.name = nameEditText.text.toString()
                if (externalIdEditText.text.toString().isNotBlank()) {
                    event.externalId = externalIdEditText.text.toString().toInt()
                } else {
                    event.externalId = null
                }
                event.eventType =
                    dataProcessor.eventTypeStringToEnum(eventTypePicker.text.toString())
                event.eventLevel =
                    dataProcessor.eventLevelStringToEnum(eventLevelPicker.text.toString())
                event.eventBand =
                    dataProcessor.eventBandStringToEnum(eventBandPicker.text.toString())
                event.timeLimit = Duration.ofMinutes(limitEditText.text.toString().toLong())
                event.startTimeSource =
                    dataProcessor.startTimeSourceStringToEnum(startTimeSourcePicker.text.toString())
                event.finishTimeSource =
                    dataProcessor.finishTimeSourceStringToEnum(finishTimeSourcePicker.text.toString())

                setFragmentResult(
                    REQUEST_EVENT_MODIFICATION, bundleOf(
                        BUNDLE_KEY_CREATE to args.create,
                        BUNDLE_KEY_EVENT to event
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

        //Validate start time
        if (startTimeView.text.toString().isBlank()) {
            startTimeView.error = getString(R.string.required)
            valid = false
        } else {
            try {
                LocalTime.parse(startTimeView.text.toString())
            } catch (e: Exception) {
                startTimeView.error = getString(R.string.invalid)
                valid = false
            }
        }

        if (limitEditText.text?.isBlank() == false) {
            try {
                Duration.ofMinutes(limitEditText.text.toString().toLong())
            } catch (e: Exception) {
                limitEditText.error = getString(R.string.invalid)
                valid = false
            }
        } else {
            limitEditText.error = getString(R.string.required)
            valid = false
        }

        return valid
    }

    companion object {
        const val REQUEST_EVENT_MODIFICATION = "REQUEST_EVENT_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_EVENT = "BUNDLE_KEY_EVENT"
        const val BUNDLE_KEY_POSITION = "BUNDLE_KEY_POSITION"
    }
}