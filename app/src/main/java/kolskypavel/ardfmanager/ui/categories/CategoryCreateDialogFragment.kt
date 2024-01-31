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
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import java.time.Duration
import java.time.format.DateTimeFormatter
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
    private lateinit var eventTypePicker: MaterialAutoCompleteTextView
    private lateinit var ageBasedCheckBox: CheckBox
    private lateinit var minYearLayout: TextInputLayout
    private lateinit var maxYearLayout: TextInputLayout
    private lateinit var minYearEditText: TextInputEditText
    private lateinit var maxYearEditText: TextInputEditText
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
        ageBasedCheckBox = view.findViewById(R.id.category_dialog_ageBased_checkbox)
        minYearLayout = view.findViewById(R.id.category_dialog_min_year_layout)
        maxYearLayout = view.findViewById(R.id.category_dialog_max_year_layout)
        minYearEditText = view.findViewById(R.id.category_dialog_min_year)
        maxYearEditText = view.findViewById(R.id.category_dialog_max_year)
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
     * Set the OK and Cancel buttons
     */

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
                "",
                false,
                -1,
                -1,
                true,
                event.eventType, event.timeLimit,
                "",
                0F,
                0F,
                0
            )

            //Preset the event type
            eventTypePicker.setText(
                dataProcessor.eventTypeToString(event.eventType),
                false
            )
            limitEditText.setText(event.timeLimit.toMinutes().toString())

            eventTypeLayout.isEnabled = false
            limitLayout.isEnabled = false
            minYearLayout.isEnabled = false
            maxYearLayout.isEnabled = false

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
                limitLayout.isEnabled = true
            }

            eventTypePicker.setText(
                dataProcessor.eventTypeToString(category.eventType),
                false
            )
            limitEditText.setText(category.timeLimit.toMinutes().toString())

            //Preset the age pickers
            if (category.ageBased) {
                minYearEditText.setText(category.minYear.toString())
                maxYearEditText.setText(category.maxYear.toString())
                ageBasedCheckBox.isChecked = true
                minYearLayout.isEnabled = true
                maxYearLayout.isEnabled = true
            } else {
                ageBasedCheckBox.isChecked = false
                minYearLayout.isEnabled = false
                maxYearLayout.isEnabled = false
            }

            if (category.length != 0F) {
                lengthEditText.setText(category.length.toString())
            }

            if (category.climb != 0F) {
                climbEditText.setText(category.climb.toString())
            }
        }

        //Set the event type checkbox functionality
        sameTypeCheckBox.setOnClickListener {
            if (sameTypeCheckBox.isChecked) {
                eventTypePicker.setText(
                    dataProcessor.eventTypeToString(event.eventType),
                    false
                )
                limitEditText.setText(event.timeLimit.toMinutes().toString())

                eventTypeLayout.isEnabled = false
                limitLayout.isEnabled = false
            }
            //Hide the shading and enable input
            else {
                eventTypeLayout.isEnabled = true
                limitLayout.isEnabled = true
            }
        }

        //Set the minimal check box functionality
        ageBasedCheckBox.setOnClickListener {
            if (ageBasedCheckBox.isChecked) {
                minYearLayout.isEnabled = true
                maxYearLayout.isEnabled = true
            } else {
                minYearEditText.setText("")
                maxYearEditText.setText("")
                minYearLayout.isEnabled = false
                maxYearLayout.isEnabled = false
            }
        }

        //Set the punches
        val controlPoints =
            ArrayList(selectedEventViewModel.getControlPointsByCategory(category.id))
        controlPointRecyclerView.adapter = ControlPointRecyclerViewAdapter(controlPoints)
    }

    private fun checkFields(): Boolean {
        var valid = true

        if (nameEditText.text?.isBlank() == true) {
            nameEditText.error = getString(R.string.required)
            valid = false
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

        if (ageBasedCheckBox.isChecked) {
            val minYear: String = minYearEditText.text.toString()
            val maxYear: String = maxYearEditText.text.toString()

            if (minYear.isBlank()) {
                minYearEditText.error = getString(R.string.required)
                valid = false
            }
            if (maxYear.isBlank()) {
                maxYearEditText.error = getString(R.string.required)
                valid = false
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy")
            try {
                formatter.parse(minYear)
            } catch (e: Exception) {
                minYearEditText.error = getString(R.string.nonexistent_year)
                valid = false
            }

            try {
                formatter.parse(maxYear)
            } catch (e: Exception) {
                maxYearEditText.error = getString(R.string.nonexistent_year)
                valid = false
            }

            if (maxYear < minYear) {
                maxYearEditText.error = getString(R.string.invalid_year_range)
                valid = false
            }
        }

        //Check control points
        val eventType = dataProcessor.eventTypeStringToEnum(eventTypePicker.text.toString())


        return valid
    }

    private fun setButtons() {

        okButton.setOnClickListener {
            if (checkFields()) {
                category.name = nameEditText.text.toString()
                category.differentProperties = !sameTypeCheckBox.isChecked
                category.eventType =
                    dataProcessor.eventTypeStringToEnum(eventTypePicker.text.toString())
                category.timeLimit = Duration.ofMinutes(limitEditText.text.toString().toLong())
                category.ageBased = ageBasedCheckBox.isChecked

                if (category.ageBased) {
                    category.minYear = (minYearEditText.text.toString()).toInt()
                    category.maxYear = (maxYearEditText.text.toString()).toInt()
                }

                if (lengthEditText.text?.isBlank() == false) {
                    category.length = lengthEditText.text.toString().toFloat()
                }
                if (climbEditText.text?.isBlank() == false) {
                    category.climb = climbEditText.text.toString().toFloat()
                }

                if (args.create) {
                    selectedEventViewModel.createCategory(category)
                } else {
                    selectedEventViewModel.updateCategory(category)
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