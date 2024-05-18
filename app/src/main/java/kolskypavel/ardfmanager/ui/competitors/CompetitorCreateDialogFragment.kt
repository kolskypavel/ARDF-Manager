package kolskypavel.ardfmanager.ui.competitors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import kolskypavel.ardfmanager.backend.wrappers.PunchEditItemWrapper
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class CompetitorCreateDialogFragment : DialogFragment() {
    private val args: CompetitorCreateDialogFragmentArgs by navArgs()
    private lateinit var selectedEventViewModel: SelectedEventViewModel
    private val dataProcessor = DataProcessor.get()

    private lateinit var competitor: Competitor
    private var origSiNumber: Int? = null
    private var origCategory: UUID? = null
    private var modifiedPunches = false

    private lateinit var categories: List<Category>
    private val categoryArr = ArrayList<String>()
    private val statusArr = ArrayList<String>()

    private lateinit var firstNameTextView: TextInputEditText
    private lateinit var lastNameTextView: TextInputEditText
    private lateinit var clubTextView: TextInputEditText
    private lateinit var indexTextView: TextInputEditText
    private lateinit var birthYearTextView: TextInputEditText
    private lateinit var womanCheckBox: CheckBox
    private lateinit var categoryPicker: MaterialAutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var automaticCategoryButton: Button
    private lateinit var siNumberLayout: TextInputLayout
    private lateinit var siNumberTextView: TextInputEditText
    private lateinit var startNumberTextView: TextInputEditText
    private lateinit var startTimeTextView: TextInputEditText
    private lateinit var siRentCheckBox: CheckBox
    private lateinit var editPunchesSwitch: SwitchMaterial
    private lateinit var dataEditLinearLayout: LinearLayout
    private lateinit var raceStatusPicker: MaterialAutoCompleteTextView
    private lateinit var punchEditRecyclerView: RecyclerView

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_add_competitor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sl: SelectedEventViewModel by activityViewModels()
        selectedEventViewModel = sl
        categories = selectedEventViewModel.categories.value

        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)

        firstNameTextView = view.findViewById(R.id.competitor_dialog_first_name)
        lastNameTextView = view.findViewById(R.id.competitor_dialog_last_name)
        clubTextView = view.findViewById(R.id.competitor_dialog_club)
        indexTextView = view.findViewById(R.id.competitor_dialog_index_callsign)
        birthYearTextView = view.findViewById(R.id.competitor_dialog_year_of_birth)
        womanCheckBox = view.findViewById(R.id.competitor_dialog_woman_checkbox)
        categoryLayout = view.findViewById(R.id.competitor_dialog_category_layout)
        automaticCategoryButton =
            view.findViewById(R.id.competitor_dialog_automatic_category_checkbox)
        categoryPicker = view.findViewById(R.id.competitor_dialog_category)
        siNumberLayout = view.findViewById(R.id.competitor_dialog_si_layout)
        startTimeTextView = view.findViewById(R.id.competitor_dialog_start_time)
        siNumberTextView = view.findViewById(R.id.competitor_dialog_si_number)
        startNumberTextView = view.findViewById(R.id.competitor_dialog_start_number)
        siRentCheckBox = view.findViewById(R.id.competitor_dialog_si_rent)

        editPunchesSwitch = view.findViewById(R.id.competitor_dialog_edit_punches)
        dataEditLinearLayout = view.findViewById(R.id.competitor_dialog_data_edit_layout)
        raceStatusPicker = view.findViewById(R.id.competitor_dialog_status)
        punchEditRecyclerView = view.findViewById(R.id.competitor_dialog_punch_recycler_view)

        cancelButton = view.findViewById(R.id.competitor_dialog_cancel)
        okButton = view.findViewById(R.id.competitor_dialog_ok)

        populateFields()
        setButtons()
    }

    private fun populateFields() {
        if (args.create) {
            dialog?.setTitle(R.string.competitor_create)

            val event = selectedEventViewModel.getCurrentEvent()
            val startNumber = runBlocking {
                return@runBlocking dataProcessor.getHighestStartNumberByEvent(event.id)
            } + 1

            competitor = Competitor(
                UUID.randomUUID(),
                event.id,
                null,
                "", "", "", "",
                false,
                LocalDate.now().year,
                null,
                siRent = false,
                startNumber,
                null
            )
            categoryPicker.setText(getString(R.string.no_category), false)
        } else {
            dialog?.setTitle(R.string.competitor_edit)
            competitor = args.competitor!!

            firstNameTextView.setText(competitor.firstName)
            lastNameTextView.setText(competitor.lastName)
            clubTextView.setText(competitor.club)
            indexTextView.setText(competitor.index)
            birthYearTextView.setText(competitor.birthYear.toString())

            //Pre-set SI number
            if (competitor.siNumber != null) {
                siNumberTextView.setText(competitor.siNumber.toString())
            }
            startNumberTextView.setText(competitor.startNumber.toString())

            //Auto insertion of the last card read
            siNumberLayout.setEndIconOnClickListener {
                val last = selectedEventViewModel.getLastReadCard()
                if (last != null) {
                    siNumberTextView.setText(last.toString())
                }
            }

            //Preset gender
            if (competitor.isWoman) {
                womanCheckBox.isChecked = true
            }

            //Preset category
            if (competitor.categoryId != null) {
                runBlocking {
                    val category = selectedEventViewModel.getCategory(competitor.categoryId!!)
                    categoryPicker.setText(category.name, false)
                }

            } else {
                categoryPicker.setText(getString(R.string.no_category), false)
            }

            //Rented chip
            if (competitor.siRent) {
                siRentCheckBox.isChecked = true
            }

        }

        //Populate the list of categories
        for (cat in categories) {
            categoryArr.add(cat.name)
        }
        categoryArr.add(0, getString(R.string.no_category)) //Add the empty category option
        val categoriesAdapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryArr)

        categoryPicker.setAdapter(categoriesAdapter)
        startNumberTextView.setText(competitor.startNumber.toString())

        womanCheckBox.setOnCheckedChangeListener { _, checked ->
            competitor.isWoman = checked
        }

        //TODO: Enable the automatic category, based on the year of birth
        automaticCategoryButton.setOnClickListener {
            if (birthYearTextView.text.toString().isNotBlank()) {

                val formatter = DateTimeFormatter.ofPattern("yyyy")
                val year = birthYearTextView.text.toString()
                try {
                    formatter.parse(year)
                    if (year.toInt() > LocalDate.now().year) {
                        throw IllegalArgumentException("Invalid year")
                    }
                    runBlocking {
                        val calc = dataProcessor.getCategoryByBirthYear(
                            year.toInt(),
                            competitor.isWoman,
                            selectedEventViewModel.event.value!!.id
                        )
                        if (calc != null) {
                            categoryPicker.setText(calc.name, false)
                        } else {
                            categoryLayout.error = getString(R.string.automatic_category_not_found)
                        }
                    }

                } catch (e: Exception) {
                    birthYearTextView.error = getString(R.string.nonexistent_year)
                }
            }
        }

        //Set startTime
        if (competitor.drawnRelativeStartTime != null) {
            startTimeTextView.setText(
                TimeProcessor.getHoursMinutesFromTime(
                    TimeProcessor.getAbsoluteDateTimeFromRelativeTime(
                        dataProcessor.getCurrentEvent().startDateTime,
                        competitor.drawnRelativeStartTime!!
                    )
                )
            )
        }

        // Punches setup

        //Set up the punch edit recycler view
        punchEditRecyclerView.adapter =
            PunchEditRecyclerViewAdapter(
                selectedEventViewModel.getPunchRecordsForCompetitor(
                    args.create,
                    competitor
                )
            )

        dataEditLinearLayout.visibility = View.GONE
        //Toggle the visibility of the punch switch
        editPunchesSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                dataEditLinearLayout.visibility = View.VISIBLE
                modifiedPunches = true
            } else {
                dataEditLinearLayout.visibility = View.GONE
                modifiedPunches = false
            }
        }

        //Populate the status options
        for (status in RaceStatus.entries) {
            statusArr.add(dataProcessor.raceStatusToString(status))
        }

        statusArr.add(0, getString(R.string.automatic))
        val statusAdapter: ArrayAdapter<String> =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                statusArr
            )

        raceStatusPicker.setAdapter(statusAdapter)
        raceStatusPicker.setText(getString(R.string.automatic), false)

    }

    private fun setButtons() {

        okButton.setOnClickListener {
            if (validateFields(competitor.siNumber, competitor.startNumber)) {
                competitor.firstName = firstNameTextView.text.toString()
                competitor.lastName = lastNameTextView.text.toString()
                competitor.club = clubTextView.text.toString()
                competitor.index = indexTextView.text.toString()
                if (startTimeTextView.text.toString().isNotBlank()) {
                    competitor.drawnRelativeStartTime =
                        TimeProcessor.minuteStringToDuration(startTimeTextView.text.toString())
                }
                if (birthYearTextView.text.toString().isNotEmpty()) {
                    competitor.birthYear = birthYearTextView.text.toString().toInt()
                }

                if (siNumberTextView.text.toString().isNotEmpty()) {
                    competitor.siNumber = siNumberTextView.text.toString().toInt()
                }

                if (startNumberTextView.text.toString().isNotEmpty()) {
                    competitor.startNumber = startNumberTextView.text.toString().toInt()
                }

                //0 is reserved for no category
                val catPos = categoryArr.indexOf(categoryPicker.text.toString()).or(0)
                if (catPos > 0 && catPos <= categories.size) {
                    competitor.categoryId = categories[catPos - 1].id
                } else {
                    competitor.categoryId = null
                }

                selectedEventViewModel.createOrUpdateCompetitor(
                    competitor,
                    modifiedPunches,
                    PunchEditItemWrapper.getPunches((punchEditRecyclerView.adapter as PunchEditRecyclerViewAdapter).values),
                    getRaceStatusFromPicker()
                )
                //Send back the result to update the recycler view
                setFragmentResult(
                    REQUEST_COMPETITOR_MODIFICATION, bundleOf(
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

    private fun getRaceStatusFromPicker(): RaceStatus? {
        val raceStatusString = raceStatusPicker.text.toString()
        return if (raceStatusString == requireContext().getString(R.string.automatic)) {
            null
        } else {
            dataProcessor.raceStatusStringToEnum(raceStatusString)
        }
    }

    private fun validateFields(origSiNumber: Int?, origStartNumber: Int): Boolean {
        var valid = true

        if (firstNameTextView.text.toString().isBlank()) {
            valid = false
            firstNameTextView.error = getString(R.string.required)
        }
        if (lastNameTextView.text.toString().isBlank()) {
            valid = false
            lastNameTextView.error = getString(R.string.required)
        }

        //Check the birth year
        if (birthYearTextView.text.toString().isNotBlank()) {

            val formatter = DateTimeFormatter.ofPattern("yyyy")
            val year = birthYearTextView.text.toString()
            try {
                formatter.parse(year)
                if (year.toInt() > LocalDate.now().year) {
                    throw IllegalArgumentException("Invalid year")
                }
            } catch (e: Exception) {
                birthYearTextView.error = getString(R.string.nonexistent_year)
                valid = false
            }
        }

        //Check if the SI number is valid
        if (siNumberTextView.text.toString().isNotEmpty()) {
            try {
                val siNumber = siNumberTextView.text.toString().toInt()

                if (siNumber != origSiNumber) {
                    //Invalid range
                    if (siNumber < SIConstants.SI_MIN_NUMBER || siNumber > SIConstants.SI_MAX_NUMBER) {
                        valid = false
                        siNumberTextView.error =
                            getString(R.string.competitor_si_number_out_of_range)
                    }
                    //Already existing
                    else if (selectedEventViewModel.checkIfSINumberExists(siNumber)) {
                        valid = false
                        siNumberTextView.error =
                            getString(R.string.competitor_duplicate_si_number)
                    }
                }
            } catch (e: Exception) {
                valid = false
                siNumberTextView.error = getString(R.string.invalid)
            }
        }

        //Check if the start number is valid
        if (startNumberTextView.text.toString().isNotEmpty()) {
            try {
                val startNumber = startNumberTextView.text.toString().toInt()
                if (startNumber != origStartNumber && selectedEventViewModel.checkIfStartNumberExists(
                        startNumber
                    )
                ) {
                    valid = false
                    startNumberTextView.error =
                        getString(R.string.duplicate)
                }

            } catch (e: Exception) {
                valid = false
                startNumberTextView.error = getString(R.string.invalid)
            }
        } else {
            valid = false
            startNumberTextView.error = getString(R.string.required)
        }

        //Check the start time
        if (startTimeTextView.text.toString().isNotBlank()) {
            try {
                TimeProcessor.minuteStringToDuration(startTimeTextView.text.toString())
            } catch (e: Exception) {
                startTimeTextView.error = getString(R.string.invalid)
                valid = false
            }
        }

        //Check if the modified punches are valid
        if (modifiedPunches && !(punchEditRecyclerView.adapter as PunchEditRecyclerViewAdapter).isValid()) {
            valid = false
        }

        return valid
    }

    companion object {
        const val REQUEST_COMPETITOR_MODIFICATION = "REQUEST_COMPETITOR_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_POSITION = "BUNDLE_KEY_POSITION"
    }
}