package kolskypavel.ardfmanager.ui.categories

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.ControlPointsHelper
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import java.time.Duration
import java.util.UUID


class CategoryCreateDialogFragment : DialogFragment() {

    private val args: CategoryCreateDialogFragmentArgs by navArgs()
    private lateinit var selectedRaceViewModel: SelectedRaceViewModel
    private val dataProcessor = DataProcessor.get()
    private lateinit var category: Category

    private lateinit var nameEditText: TextInputEditText
    private lateinit var samePropertiesCheckBox: CheckBox
    private lateinit var raceTypeLayout: TextInputLayout
    private lateinit var limitEditText: TextInputEditText
    private lateinit var limitLayout: TextInputLayout
    private lateinit var genderPicker: MaterialAutoCompleteTextView
    private lateinit var raceTypePicker: MaterialAutoCompleteTextView
    private lateinit var startTimeSourceLayout: TextInputLayout
    private lateinit var startTimeSourcePicker: MaterialAutoCompleteTextView
    private lateinit var finishTimeSourceLayout: TextInputLayout
    private lateinit var finishTimeSourcePicker: MaterialAutoCompleteTextView
    private lateinit var maxAgeLayout: TextInputLayout
    private lateinit var maxAgeEditText: TextInputEditText
    private lateinit var lengthEditText: TextInputEditText
    private lateinit var climbEditText: TextInputEditText
    private lateinit var controlPointsLayout: TextInputLayout
    private lateinit var controlPointsEditText: TextInputEditText

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_edit_category, container, false)
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)
        setWidthPercent(95)

        val sl: SelectedRaceViewModel by activityViewModels()
        selectedRaceViewModel = sl

        nameEditText = view.findViewById(R.id.category_dialog_name)
        samePropertiesCheckBox = view.findViewById(R.id.category_dialog_same_properties_checkbox)
        raceTypeLayout = view.findViewById(R.id.category_dialog_type_layout)
        limitEditText = view.findViewById(R.id.category_dialog_limit)
        limitLayout = view.findViewById(R.id.category_dialog_limit_layout)
        raceTypePicker = view.findViewById(R.id.category_dialog_type)
        genderPicker = view.findViewById(R.id.category_gender)
        startTimeSourceLayout = view.findViewById(R.id.category_dialog_start_time_source_layout)
        startTimeSourcePicker = view.findViewById(R.id.category_dialog_start_time_source)
        finishTimeSourceLayout = view.findViewById(R.id.category_dialog_finish_time_source_layout)
        finishTimeSourcePicker = view.findViewById(R.id.category_dialog_finish_time_source)
        maxAgeLayout = view.findViewById(R.id.category_dialog_max_age_layout)
        maxAgeEditText = view.findViewById(R.id.category_dialog_max_age)
        lengthEditText = view.findViewById(R.id.category_dialog_length)
        climbEditText = view.findViewById(R.id.category_dialog_climb)
        controlPointsLayout = view.findViewById(R.id.category_dialog_control_points_layout)
        controlPointsEditText =
            view.findViewById(R.id.category_dialog_control_points)

        cancelButton = view.findViewById(R.id.category_dialog_cancel)
        okButton = view.findViewById(R.id.category_dialog_ok)

        populateFields()
        setButtons()
    }

    /**
     * Populate the data fields - text views, pickers
     */
    private fun populateFields() {
        val race = selectedRaceViewModel.getCurrentRace()

        if (args.create) {
            val order = selectedRaceViewModel.getHighestCategoryOrder(race.id) + 1

            dialog?.setTitle(R.string.category_create)
            category = Category(
                UUID.randomUUID(),
                race.id,
                "", isMan = false,
                null,
                0F,
                0F,
                order,
                false,
                race.raceType,
                race.timeLimit,
                race.startTimeSource,
                race.finishTimeSource,
                args.controlPoints
            )

            //Preset the data from the race
            raceTypePicker.setText(
                dataProcessor.raceTypeToString(race.raceType),
                false
            )
            limitEditText.setText(race.timeLimit.toMinutes().toString())
            startTimeSourcePicker.setText(
                dataProcessor.startTimeSourceToString(race.startTimeSource),
                false
            )
            finishTimeSourcePicker.setText(
                dataProcessor.finishTimeSourceToString(race.finishTimeSource),
                false
            )

            raceTypeLayout.isEnabled = false
            limitLayout.isEnabled = false
            startTimeSourceLayout.isEnabled = false
            finishTimeSourceLayout.isEnabled = false

        }

        //Edit category
        else {
            dialog?.setTitle(R.string.category_edit)
            category = args.category!!
            nameEditText.setText(category.name)

            if (category.differentProperties) {
                samePropertiesCheckBox.isChecked = false
            } else {
                raceTypeLayout.isEnabled = false
                limitLayout.isEnabled = false
                startTimeSourceLayout.isEnabled = false
                finishTimeSourceLayout.isEnabled = false
            }

            raceTypePicker.setText(
                dataProcessor.raceTypeToString(category.raceType ?: race.raceType),
                false
            )
            limitEditText.setText(
                if (category.timeLimit != null) {
                    category.timeLimit!!.toMinutes().toString()
                } else {
                    race.timeLimit.toMinutes().toString()
                }
            )
            startTimeSourcePicker.setText(
                dataProcessor.startTimeSourceToString(
                    category.startTimeSource ?: race.startTimeSource
                ),
                false
            )
            finishTimeSourcePicker.setText(
                dataProcessor.finishTimeSourceToString(
                    category.finishTimeSource ?: race.finishTimeSource
                ),
                false
            )

            if (category.maxAge != null) {
                maxAgeEditText.setText(category.maxAge.toString())
            }

            if (category.length != 0F) {
                lengthEditText.setText(category.length.toString())
            }

            if (category.climb != 0F) {
                climbEditText.setText(category.climb.toString())
            }
        }

        //Set gender
        when (category.isMan) {
            true -> genderPicker.setText(getString(R.string.gender_woman), false)
            false -> genderPicker.setText(getString(R.string.gender_man), false)
        }

        controlPointsEditText.setText(category.controlPointsString)

        //TODO: Process the saving - this is just to preserve the filtering after screen rotation
        raceTypePicker.isSaveEnabled = false
        startTimeSourcePicker.isSaveEnabled = false
        finishTimeSourcePicker.isSaveEnabled = false
    }


    private fun raceTypeWatcher(position: Int) {
        category.raceType = RaceType.getByValue(position)!!
    }

    private fun checkFields(): Boolean {
        var valid = true

        if (nameEditText.text?.isBlank() == true) {
            nameEditText.error = getString(R.string.required)
            valid = false
        }
        //Check if the name is unique
        else {
            val name = nameEditText.text.toString()
            val orig = selectedRaceViewModel.getCategoryByName(name)
            if (orig != null && orig.id != category.id) {
                valid = false
                nameEditText.error = getString(R.string.category_exists)
            }
        }

        if (!samePropertiesCheckBox.isChecked) {
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
        }

        if (maxAgeEditText.text.toString().isNotBlank()) {
            val maxYear: String = maxAgeEditText.text.toString().trim()

            val orig = selectedRaceViewModel.getCategoryByMaxAge(maxYear.toInt())
            if (orig != null && orig.id != category.id) {
                maxAgeEditText.error = getString(R.string.invalid_max_age, orig.name)
                valid = false
            }
        }

        if (controlPointsEditText.text.toString().isNotBlank()) {
            val text = controlPointsEditText.text.toString().trim()

            try {
                ControlPointsHelper.getControlPointsFromString(
                    text,
                    category.id,
                    category.raceId,
                    category.raceType ?: selectedRaceViewModel.getCurrentRace().raceType,
                    requireContext()
                )
            } catch (e: Exception) {
                controlPointsLayout.error = e.message
                valid = false
            }
        }
        return valid
    }

    private fun setButtons() {
        controlPointsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                controlPointsLayout.error = ""
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //Set the race type checkbox functionality
        samePropertiesCheckBox.setOnClickListener {
            val race = selectedRaceViewModel.getCurrentRace()
            if (samePropertiesCheckBox.isChecked) {
                raceTypePicker.setText(
                    dataProcessor.raceTypeToString(race.raceType),
                    false
                )
                raceTypeWatcher(race.raceType.value)
                limitEditText.setText(race.timeLimit.toMinutes().toString())
                startTimeSourcePicker.setText(
                    dataProcessor.startTimeSourceToString(race.startTimeSource),
                    false
                )
                finishTimeSourcePicker.setText(
                    dataProcessor.finishTimeSourceToString(race.finishTimeSource),
                    false
                )

                raceTypeLayout.isEnabled = false
                limitLayout.isEnabled = false
                startTimeSourceLayout.isEnabled = false
                finishTimeSourceLayout.isEnabled = false
            }

            //Hide the shading and enable input
            else {
                raceTypeLayout.isEnabled = true
                limitLayout.isEnabled = true
                startTimeSourceLayout.isEnabled = true
                finishTimeSourceLayout.isEnabled = true
                raceTypePicker.setOnItemClickListener { _, _, position, _ ->
                    raceTypeWatcher(position)
                }
            }
        }

        okButton.setOnClickListener {
            if (checkFields()) {
                category.name = nameEditText.text.toString().trim()

                if (maxAgeEditText.text.toString().isNotBlank()) {
                    category.maxAge = (maxAgeEditText.text.toString().trim()).toInt()
                } else {
                    category.maxAge = null
                }

                if (lengthEditText.text?.isBlank() == false) {
                    category.length = lengthEditText.text.toString().trim().toFloat()
                }
                if (climbEditText.text?.isBlank() == false) {
                    category.climb = climbEditText.text.toString().trim().toFloat()
                }

                //Set the data from pickers
                category.isMan = dataProcessor.genderFromString(genderPicker.text.toString())

                category.differentProperties = !samePropertiesCheckBox.isChecked
                if (category.differentProperties) {
                    category.raceType =
                        dataProcessor.raceTypeStringToEnum(raceTypePicker.text.toString())
                    category.timeLimit = Duration.ofMinutes(limitEditText.text.toString().toLong())
                    category.startTimeSource =
                        dataProcessor.startTimeSourceStringToEnum(startTimeSourcePicker.text.toString())
                    category.finishTimeSource =
                        dataProcessor.finishTimeSourceStringToEnum(finishTimeSourcePicker.text.toString())
                } else {
                    category.raceType = null
                    category.timeLimit = null
                    category.startTimeSource = null
                    category.finishTimeSource = null
                }
                val controlPointsString =
                    controlPointsEditText.text.toString().trim()
                //Get control points
                val controlPoints = ControlPointsHelper.getControlPointsFromString(
                    controlPointsString,
                    category.raceId,
                    category.id,
                    category.raceType ?: selectedRaceViewModel.getCurrentRace().raceType,
                    requireContext()
                )
                selectedRaceViewModel.createOrUpdateCategory(category, controlPoints)
                category.controlPointsString = controlPointsString

                setFragmentResult(
                    REQUEST_CATEGORY_MODIFICATION, bundleOf(
                        BUNDLE_KEY_CREATE to args.create,
                        BUNDLE_KEY_POSITION to args.position
                    )
                )
                dialog?.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    companion object {
        const val REQUEST_CATEGORY_MODIFICATION = "REQUEST_CATEGORY_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_POSITION = "BUNDLE_KEY_POSITION"
    }
}