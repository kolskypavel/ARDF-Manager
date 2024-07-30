package kolskypavel.ardfmanager.ui.races

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
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.enums.FinishTimeSource
import kolskypavel.ardfmanager.backend.room.enums.RaceBand
import kolskypavel.ardfmanager.backend.room.enums.RaceLevel
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.StartTimeSource
import kolskypavel.ardfmanager.ui.pickers.DatePickerFragment
import kolskypavel.ardfmanager.ui.pickers.TimePickerFragment
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class RaceCreateDialogFragment : DialogFragment() {
    private val args: RaceCreateDialogFragmentArgs by navArgs()
    private val dataProcessor = DataProcessor.get()

    private lateinit var nameEditText: TextInputEditText
    private lateinit var externalIdEditText: TextInputEditText
    private lateinit var dateView: TextInputEditText
    private lateinit var startTimeView: TextInputEditText
    private lateinit var limitEditText: TextInputEditText
    private lateinit var raceTypePicker: MaterialAutoCompleteTextView
    private lateinit var raceLevelPicker: MaterialAutoCompleteTextView
    private lateinit var raceBandPicker: MaterialAutoCompleteTextView
    private lateinit var startTimeSourcePicker: MaterialAutoCompleteTextView
    private lateinit var finishTimeSourcePicker: MaterialAutoCompleteTextView

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    private lateinit var race: Race

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_edit_race, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)

        nameEditText = view.findViewById(R.id.race_dialog_name)
        externalIdEditText = view.findViewById(R.id.race_dialog_external_id)
        dateView = view.findViewById(R.id.race_dialog_date)
        startTimeView = view.findViewById(R.id.race_dialog_start_time)
        limitEditText = view.findViewById(R.id.race_dialog_limit)
        raceTypePicker = view.findViewById(R.id.category_dialog_type)
        raceLevelPicker = view.findViewById(R.id.race_dialog_level)
        raceBandPicker = view.findViewById(R.id.race_dialog_band)
        startTimeSourcePicker = view.findViewById(R.id.race_dialog_start_time_source)
        finishTimeSourcePicker = view.findViewById(R.id.race_dialog_finish_time_source)
        cancelButton = view.findViewById(R.id.race_dialog_cancel)
        okButton = view.findViewById(R.id.race_dialog_ok)


        //TODO: Process the saving - this is just to prrace the filtering after screen rotation
        raceTypePicker.isSaveEnabled = false
        raceLevelPicker.isSaveEnabled = false
        raceBandPicker.isSaveEnabled = false
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
            findNavController().navigate(RaceCreateDialogFragmentDirections.selectDate(race.startDateTime.toLocalDate()))
        }
        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->

            race.startDateTime =
                LocalDateTime.of(
                    LocalDate.parse(bundle.getString(DatePickerFragment.BUNDLE_KEY_DATE)),
                    race.startDateTime.toLocalTime()
                )

            dateView.setText(race.startDateTime.toLocalDate().toString())
        }

        startTimeView.setOnClickListener {
            findNavController().navigate(RaceCreateDialogFragmentDirections.selectTime(race.startDateTime.toLocalTime()))
        }
        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_TIME) { _, bundle ->
            race.startDateTime = race.startDateTime.with(
                LocalTime.parse(bundle.getString(TimePickerFragment.BUNDLE_KEY_TIME))
            )
            startTimeView.setText(race.startDateTime.toLocalTime().toString())
        }
    }

    private fun populateFields() {

        //Create new race
        if (args.create) {
            dialog?.setTitle(R.string.race_create)
            race = Race(
                UUID.randomUUID(),
                "", null,
                LocalDateTime.now(),
                RaceType.CLASSICS,
                RaceLevel.PRACTICE,
                RaceBand.M80,
                Duration.ofMinutes(120),
                StartTimeSource.DRAWN_TIME,
                FinishTimeSource.FINISH_CONTROL
            )
        } else {
            race = args.race!!
            dialog?.setTitle(R.string.race_edit)
            nameEditText.setText(race.name)
        }

        dateView.setText(race.startDateTime.toLocalDate().toString())
        if (race.externalId != null) {
            externalIdEditText.setText(race.externalId.toString())
        }
        startTimeView.setText(TimeProcessor.hoursMinutesFormatter(race.startDateTime))
        limitEditText.setText("120") //TODO: Fix with default values from settings

        raceTypePicker.setText(dataProcessor.raceTypeToString(race.raceType), false)
        raceLevelPicker.setText(dataProcessor.raceLevelToString(race.raceLevel), false)
        raceBandPicker.setText(dataProcessor.raceBandToString(race.raceBand), false)
        startTimeSourcePicker.setText(
            dataProcessor.startTimeSourceToString(race.startTimeSource),
            false
        )
        finishTimeSourcePicker.setText(
            dataProcessor.finishTimeSourceToString(race.finishTimeSource),
            false
        )

    }

    private fun setButtons() {
        okButton.setOnClickListener {

            //Send the arguments to create a new race
            if (checkValidity()) {

                race.name = nameEditText.text.toString()
                if (externalIdEditText.text.toString().isNotBlank()) {
                    race.externalId = externalIdEditText.text.toString().toLong()
                } else {
                    race.externalId = null
                }
                race.raceType =
                    dataProcessor.raceTypeStringToEnum(raceTypePicker.text.toString())
                race.raceLevel =
                    dataProcessor.raceLevelStringToEnum(raceLevelPicker.text.toString())
                race.raceBand =
                    dataProcessor.raceBandStringToEnum(raceBandPicker.text.toString())
                race.timeLimit = Duration.ofMinutes(limitEditText.text.toString().toLong())
                race.startTimeSource =
                    dataProcessor.startTimeSourceStringToEnum(startTimeSourcePicker.text.toString())
                race.finishTimeSource =
                    dataProcessor.finishTimeSourceStringToEnum(finishTimeSourcePicker.text.toString())

                setFragmentResult(
                    REQUEST_RACE_MODIFICATION, bundleOf(
                        BUNDLE_KEY_CREATE to args.create,
                        BUNDLE_KEY_RACE to race
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
        const val REQUEST_RACE_MODIFICATION = "REQUEST_RACE_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_RACE = "BUNDLE_KEY_RACE"
        const val BUNDLE_KEY_POSITION = "BUNDLE_KEY_POSITION"
    }
}