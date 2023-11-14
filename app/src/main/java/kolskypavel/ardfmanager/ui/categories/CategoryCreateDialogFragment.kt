package kolskypavel.ardfmanager.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import java.time.format.DateTimeFormatter
import java.util.UUID


class CategoryCreateDialogFragment : DialogFragment() {

    private val args: CategoryCreateDialogFragmentArgs by navArgs()
    private val dataProcessor = DataProcessor.get()

    private lateinit var category: Category

    private lateinit var nameTextView: TextInputEditText
    private lateinit var sameTypeCheckBox: CheckBox
    private lateinit var eventTypeLayout: TextInputLayout
    private lateinit var eventTypePicker: MaterialAutoCompleteTextView
    private lateinit var ageBasedCheckBox: CheckBox
    private lateinit var minYearLayout: TextInputLayout
    private lateinit var maxYearLayout: TextInputLayout
    private lateinit var minYearTextView: TextInputEditText
    private lateinit var maxYearTextView: TextInputEditText
    private lateinit var siCodesTextView: TextInputEditText
    private lateinit var lengthTextView: TextInputEditText
    private lateinit var climbTextView: TextInputEditText

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
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)

        nameTextView = view.findViewById(R.id.category_dialog_name)
        sameTypeCheckBox = view.findViewById(R.id.category_dialog_same_type_checkbox)
        eventTypeLayout = view.findViewById(R.id.category_dialog_type_layout)
        eventTypePicker = view.findViewById(R.id.category_dialog_type)
        ageBasedCheckBox = view.findViewById(R.id.category_dialog_ageBased_checkbox)
        minYearLayout = view.findViewById(R.id.category_dialog_min_year_layout)
        maxYearLayout = view.findViewById(R.id.category_dialog_max_year_layout)
        minYearTextView = view.findViewById(R.id.category_dialog_min_year)
        maxYearTextView = view.findViewById(R.id.category_dialog_max_year)
        siCodesTextView = view.findViewById(R.id.category_dialog_si_codes)
        lengthTextView = view.findViewById(R.id.category_dialog_length)
        climbTextView = view.findViewById(R.id.category_dialog_climb)

        cancelButton = view.findViewById(R.id.category_dialog_cancel)
        okButton = view.findViewById(R.id.category_dialog_ok)

        populateFields()
        setButtons()
    }

    /**
     * Set the OK and Cancel buttons
     */
    private fun setButtons() {

        okButton.setOnClickListener {
            if (checkFields()) {
                category.name = nameTextView.text.toString()
                category.eventType =
                    dataProcessor.eventTypeStringToEnum(eventTypePicker.text.toString())
                category.ageBased = ageBasedCheckBox.isChecked

                if (category.ageBased) {
                    category.minYear = (minYearTextView.text.toString()).toInt()
                    category.maxYear = (maxYearTextView.text.toString()).toInt()
                }

                if (lengthTextView.text?.isBlank() == false) {
                    category.length = lengthTextView.text.toString().toFloat()
                }
                if (climbTextView.text?.isBlank() == false) {
                    category.climb = climbTextView.text.toString().toFloat()
                }

                setFragmentResult(
                    REQUEST_CATEGORY_MODIFICATION, bundleOf(
                        BUNDLE_KEY_CREATE to args.create,
                        BUNDLE_KEY_CATEGORY to category,
                        BUNDLE_KEY_SI_CODES to siCodesTextView.text.toString()
                    )
                )
                dialog?.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    /**
     * Populate the data fields - text views, pickers
     */
    private fun populateFields() {

        if (args.create) {
            dialog?.setTitle(R.string.category_create)
            category = Category(
                UUID.randomUUID(),
                args.event.id,
                "",
                false,
                -1,
                -1,
                args.event.eventType,
                0F,
                0F
            )

        } else {
            dialog?.setTitle(R.string.category_edit)
            category = args.category!!
        }

        //Preset the event type
        eventTypePicker.setText(
            dataProcessor.eventTypeToString(args.event.eventType),
            false
        )
        eventTypeLayout.isEnabled = false

        //Set the event type checkbox functionality
        sameTypeCheckBox.setOnClickListener {
            if (sameTypeCheckBox.isChecked) {
                eventTypePicker.setText(
                    dataProcessor.eventTypeToString(args.event.eventType),
                    false
                )
                eventTypeLayout.isEnabled = false
            }
            //Hide the shading and enable input
            else {
                eventTypeLayout.isEnabled = true
            }
        }

        //Enable the fields for age input
        minYearLayout.isEnabled = false
        maxYearLayout.isEnabled = false

        ageBasedCheckBox.setOnClickListener {
            if (ageBasedCheckBox.isChecked) {
                minYearLayout.isEnabled = true
                maxYearLayout.isEnabled = true
            } else {
                minYearLayout.isEnabled = false
                maxYearLayout.isEnabled = false
            }
        }
    }

    private fun checkFields(): Boolean {
        var valid = true

        if (nameTextView.text?.isBlank() == true) {
            nameTextView.error = getString(R.string.required)
            valid = false
        }
        if (ageBasedCheckBox.isChecked) {
            val minYear: String = minYearTextView.text.toString()
            val maxYear: String = maxYearTextView.text.toString()

            if (minYear.isBlank()) {
                minYearTextView.error = getString(R.string.required)
                valid = false
            }
            if (maxYear.isBlank()) {
                maxYearTextView.error = getString(R.string.required)
                valid = false
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy")
            try {
                formatter.parse(minYear)
            } catch (e: Exception) {
                minYearTextView.error = getString(R.string.nonexistent_year)
                valid = false
            }

            try {
                formatter.parse(maxYear)
            } catch (e: Exception) {
                maxYearTextView.error = getString(R.string.nonexistent_year)
                valid = false
            }

            if (maxYear < minYear) {
                maxYearTextView.error = getString(R.string.invalid_year_range)
                valid = false
            }
        }

        //Check SI codes
        val siCodes: String = siCodesTextView.text.toString()
        if (!dataProcessor.checkCodesString(siCodes)) {
            siCodesTextView.error = getString(R.string.invalid_codes)
            valid = false
        }

        return valid
    }

    companion object {
        const val REQUEST_CATEGORY_MODIFICATION = "REQUEST_CATEGORY_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_CATEGORY = "BUNDLE_KEY_CATEGORY"
        const val BUNDLE_KEY_SI_CODES = "BUNDLE_KEY_SI_CODES"
    }
}