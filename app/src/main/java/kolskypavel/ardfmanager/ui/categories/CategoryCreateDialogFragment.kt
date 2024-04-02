package kolskypavel.ardfmanager.ui.categories

import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.wrappers.ControlPointItemWrapper
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import java.time.Duration
import java.util.UUID


class CategoryCreateDialogFragment : DialogFragment() {

    private val args: CategoryCreateDialogFragmentArgs by navArgs()
    private lateinit var selectedEventViewModel: SelectedEventViewModel
    private val dataProcessor = DataProcessor.get()
    private lateinit var category: Category

    private lateinit var nameEditText: TextInputEditText
    private lateinit var sameTypeCheckBox: CheckBox
    private lateinit var eventTypeLayout: TextInputLayout
    private lateinit var limitEditText: TextInputEditText
    private lateinit var limitLayout: TextInputLayout
    private lateinit var genderPicker: MaterialAutoCompleteTextView
    private lateinit var eventTypePicker: MaterialAutoCompleteTextView
    private lateinit var startTimeSourceLayout: TextInputLayout
    private lateinit var startTimeSourcePicker: MaterialAutoCompleteTextView
    private lateinit var finishTimeSourceLayout: TextInputLayout
    private lateinit var finishTimeSourcePicker: MaterialAutoCompleteTextView
    private lateinit var maxAgeLayout: TextInputLayout
    private lateinit var maxAgeEditText: TextInputEditText
    private lateinit var lengthEditText: TextInputEditText
    private lateinit var climbEditText: TextInputEditText
    private lateinit var controlPointRecyclerView: RecyclerView

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_add_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sl: SelectedEventViewModel by activityViewModels()
        selectedEventViewModel = sl

        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)

        nameEditText = view.findViewById(R.id.category_dialog_name)
        sameTypeCheckBox = view.findViewById(R.id.category_dialog_same_type_checkbox)
        eventTypeLayout = view.findViewById(R.id.category_dialog_type_layout)
        limitEditText = view.findViewById(R.id.category_dialog_limit)
        limitLayout = view.findViewById(R.id.category_dialog_limit_layout)
        eventTypePicker = view.findViewById(R.id.category_dialog_type)
        genderPicker = view.findViewById(R.id.category_gender)
        startTimeSourceLayout = view.findViewById(R.id.category_dialog_start_time_source_layout)
        startTimeSourcePicker = view.findViewById(R.id.category_dialog_start_time_source)
        finishTimeSourceLayout = view.findViewById(R.id.category_dialog_finish_time_source_layout)
        finishTimeSourcePicker = view.findViewById(R.id.category_dialog_finish_time_source)
        maxAgeLayout = view.findViewById(R.id.category_dialog_max_age_layout)
        maxAgeEditText = view.findViewById(R.id.category_dialog_max_age)
        lengthEditText = view.findViewById(R.id.category_dialog_length)
        climbEditText = view.findViewById(R.id.category_dialog_climb)
        controlPointRecyclerView =
            view.findViewById(R.id.category_dialog_control_point_recycler_view)

        cancelButton = view.findViewById(R.id.category_dialog_cancel)
        okButton = view.findViewById(R.id.category_dialog_ok)

        populateFields()
        setButtons()

    }


    /**
     * Populate the data fields - text views, pickers
     */
    private fun populateFields() {
        val event = selectedEventViewModel.event.value!!

        if (args.create) {
            dialog?.setTitle(R.string.category_create)
            category = Category(
                UUID.randomUUID(),
                event.id,
                "", isWoman = null,
                null,
                true,
                event.eventType,
                event.timeLimit,
                event.startTimeSource,
                event.finishTimeSource,
                "",
                "",
                0F,
                0F,
                0
            )

            //Preset the data from the event
            eventTypePicker.setText(
                dataProcessor.eventTypeToString(event.eventType),
                false
            )
            limitEditText.setText(event.timeLimit.toMinutes().toString())
            startTimeSourcePicker.setText(
                dataProcessor.startTimeSourceToString(event.startTimeSource),
                false
            )
            finishTimeSourcePicker.setText(
                dataProcessor.finishTimeSourceToString(event.finishTimeSource),
                false
            )

            eventTypeLayout.isEnabled = false
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
                sameTypeCheckBox.isChecked = false
            } else {
                eventTypeLayout.isEnabled = false
                limitLayout.isEnabled = false
            }

            eventTypePicker.setText(
                dataProcessor.eventTypeToString(category.eventType),
                false
            )
            limitEditText.setText(category.timeLimit.toMinutes().toString())
            startTimeSourcePicker.setText(
                dataProcessor.startTimeSourceToString(category.startTimeSource),
                false
            )
            finishTimeSourcePicker.setText(
                dataProcessor.finishTimeSourceToString(category.finishTimeSource),
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
        when (category.isWoman) {
            null -> genderPicker.setText(getString(R.string.gender_not_specified), false)
            true -> genderPicker.setText(getString(R.string.gender_woman), false)
            false -> genderPicker.setText(getString(R.string.gender_man), false)
        }

        //Set the event type checkbox functionality
        sameTypeCheckBox.setOnClickListener {
            if (sameTypeCheckBox.isChecked) {
                eventTypePicker.setText(
                    dataProcessor.eventTypeToString(event.eventType),
                    false
                )
                eventTypeWatcher(event.eventType.value)
                limitEditText.setText(event.timeLimit.toMinutes().toString())
                startTimeSourcePicker.setText(
                    dataProcessor.startTimeSourceToString(event.startTimeSource),
                    false
                )
                finishTimeSourcePicker.setText(
                    dataProcessor.finishTimeSourceToString(event.finishTimeSource),
                    false
                )

                eventTypeLayout.isEnabled = false
                limitLayout.isEnabled = false
                startTimeSourceLayout.isEnabled = false
                finishTimeSourceLayout.isEnabled = false
            }

            //Hide the shading and enable input
            else {
                eventTypeLayout.isEnabled = true
                limitLayout.isEnabled = true
                startTimeSourceLayout.isEnabled = true
                finishTimeSourceLayout.isEnabled = true
                eventTypePicker.setOnItemClickListener { _, _, position, _ ->
                    eventTypeWatcher(position)
                }
            }
            setAdapter(null)
        }

        //Set the punches
        setAdapter(ArrayList(selectedEventViewModel.getControlPointsByCategory(category.id)))

        //TODO: Process the saving - this is just to prevent the filtering after screen rotation
        eventTypePicker.isSaveEnabled = false
        startTimeSourcePicker.isSaveEnabled = false
        finishTimeSourcePicker.isSaveEnabled = false
    }

    private fun setAdapter(values: ArrayList<ControlPoint>?) {
        if (values != null) {
            controlPointRecyclerView.adapter =
                ControlPointRecyclerViewAdapter(
                    ControlPointItemWrapper.getWrappers(values),
                    selectedEventViewModel.event.value!!.id,
                    category.id,
                    category.eventType, requireContext()
                )
        } else {
            controlPointRecyclerView.adapter =
                ControlPointRecyclerViewAdapter(
                    (controlPointRecyclerView.adapter as ControlPointRecyclerViewAdapter).getOriginalValues(),
                    selectedEventViewModel.event.value!!.id,
                    category.id,
                    category.eventType, requireContext()
                )
        }
    }

    private fun eventTypeWatcher(position: Int) {
        category.eventType = EventType.getByValue(position)!!
        setAdapter(null)
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
            val orig = selectedEventViewModel.getCategoryByName(name)
            if (orig != null && orig.id != category.id) {
                valid = false
                nameEditText.error = getString(R.string.category_exists)
            }
        }

        if (!sameTypeCheckBox.isChecked) {
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
            val maxYear: String = maxAgeEditText.text.toString()


            val orig = selectedEventViewModel.getCategoryByMaxAge(maxYear.toInt())
            if (orig != null && orig.id != category.id) {
                maxAgeEditText.error = getString(R.string.invalid_max_age, orig.name)
                valid = false
            }
        }

        //Check control points
        if (!(controlPointRecyclerView.adapter as ControlPointRecyclerViewAdapter).checkCodes()) {
            valid = false
        }

        return valid
    }

    private fun setButtons() {

        okButton.setOnClickListener {
            if (checkFields()) {
                category.name = nameEditText.text.toString()
                category.differentProperties = !sameTypeCheckBox.isChecked

                if (maxAgeEditText.text.toString().isNotBlank()) {
                    category.maxAge = (maxAgeEditText.text.toString()).toInt()
                } else {
                    category.maxAge = null
                }

                if (lengthEditText.text?.isBlank() == false) {
                    category.length = lengthEditText.text.toString().toFloat()
                }
                if (climbEditText.text?.isBlank() == false) {
                    category.climb = climbEditText.text.toString().toFloat()
                }

                //Set the data from pickers
                category.isWoman = dataProcessor.genderFromString(genderPicker.text.toString())
                category.eventType =
                    dataProcessor.eventTypeStringToEnum(eventTypePicker.text.toString())
                category.timeLimit = Duration.ofMinutes(limitEditText.text.toString().toLong())
                category.startTimeSource =
                    dataProcessor.startTimeSourceStringToEnum(startTimeSourcePicker.text.toString())
                category.finishTimeSource =
                    dataProcessor.finishTimeSourceStringToEnum(finishTimeSourcePicker.text.toString())

                //Get control points
                val parsed = selectedEventViewModel.adjustControlPoints(
                    (controlPointRecyclerView.adapter as ControlPointRecyclerViewAdapter).getControlPoints(),
                    category.eventType
                )

                val names = selectedEventViewModel.getCodesNameFromControlPoints(parsed)
                //Set the code names
                category.controlPointsNames = names.first
                category.controlPointsCodes = names.second

                //Create or update the category
                if (args.create) {
                    selectedEventViewModel.createCategory(category, parsed)
                } else {
                    selectedEventViewModel.updateCategory(category, parsed)
                }
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