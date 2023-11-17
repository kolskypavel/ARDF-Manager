package kolskypavel.ardfmanager.ui.competitors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class CompetitorCreateDialogFragment : DialogFragment() {
    private val args: CompetitorCreateDialogFragmentArgs by navArgs()
    private lateinit var selectedEventViewModel: SelectedEventViewModel

    private lateinit var competitor: Competitor
    private lateinit var categories: List<Category>
    private val categoryArr = ArrayList<String>()
    private var punches = ArrayList<Punch>()

    private lateinit var nameTextView: TextInputEditText
    private lateinit var clubTextView: TextInputEditText
    private lateinit var indexTextView: TextInputEditText
    private lateinit var birthYearTextView: TextInputEditText
    private lateinit var womanCheckBox: CheckBox
    private lateinit var categoryPicker: MaterialAutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var automaticCategoryCheckBox: CheckBox
    private lateinit var siNumberTextView: TextInputEditText
    private lateinit var siRentCheckBox: CheckBox
    private lateinit var editPunchesSwitch: SwitchMaterial

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

        nameTextView = view.findViewById(R.id.competitor_dialog_name)
        clubTextView = view.findViewById(R.id.competitor_dialog_club)
        indexTextView = view.findViewById(R.id.competitor_dialog_index_callsign)
        birthYearTextView = view.findViewById(R.id.competitor_dialog_year_of_birth)
        womanCheckBox = view.findViewById(R.id.competitor_dialog_woman_checkbox)
        categoryLayout = view.findViewById(R.id.competitor_dialog_category_layout)
        automaticCategoryCheckBox =
            view.findViewById(R.id.competitor_dialog_automatic_category_checkbox)
        categoryPicker = view.findViewById(R.id.competitor_dialog_category)
        siNumberTextView = view.findViewById(R.id.competitor_dialog_si_number)
        siRentCheckBox = view.findViewById(R.id.competitor_dialog_si_rent)
        editPunchesSwitch = view.findViewById(R.id.competitor_dialog_edit_punches)

        cancelButton = view.findViewById(R.id.competitor_dialog_cancel)
        okButton = view.findViewById(R.id.competitor_dialog_ok)

        populateFields()
        setButtons()
    }

    private fun populateFields() {
        if (args.create) {
            dialog?.setTitle(R.string.competitor_create)
            competitor = Competitor(
                UUID.randomUUID(),
                selectedEventViewModel.event.value!!.id,
                null,
                "",
                "",
                "",
                false,
                LocalDate.now().year,
                0,
                siRent = false,
                automaticCategory = true,
                LocalDateTime.now()
            )
            categoryPicker.setText(getString(R.string.no_category), false)
        } else {
            dialog?.setTitle(R.string.competitor_edit)
            competitor = args.competitor!!
            nameTextView.setText(competitor.name)
            clubTextView.setText(competitor.club)
            indexTextView.setText(competitor.index)
            birthYearTextView.setText(competitor.birthYear.toString())
            siNumberTextView.setText(competitor.siNumber.toString())

            if (competitor.isWoman) {
                womanCheckBox.isChecked = true
            }

            if (competitor.automaticCategory) {
                automaticCategoryCheckBox.isChecked = true
                categoryLayout.isEnabled = false
            }
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
    }

    private fun setButtons() {
        okButton.setOnClickListener {
            if (validateFields()) {
                competitor.name = nameTextView.text.toString()
                competitor.club = clubTextView.text.toString()
                competitor.index = indexTextView.text.toString()
                if (birthYearTextView.text.toString().isNotEmpty()) {
                    competitor.birthYear = birthYearTextView.text.toString().toInt()
                }
                competitor.automaticCategory = automaticCategoryCheckBox.isChecked

                if (siNumberTextView.text.toString().isNotEmpty()) {
                    competitor.siNumber = siNumberTextView.text.toString().toInt()
                }

                if (!automaticCategoryCheckBox.isChecked) {
                    //0 is reserved for no category
                    val catPos = categoryArr.indexOf(categoryPicker.text.toString()).or(0)
                    if (catPos != 0) {
                        competitor.categoryId = categories[catPos - 1].id
                    }
                }
                if (args.create) {
                    selectedEventViewModel.createCompetitor(competitor)
                } else {
                    selectedEventViewModel.updateCompetitor(competitor)
                    //TODO: Update the recycler view
                }
                dialog?.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    private fun validateFields(): Boolean {
        var valid = true

        if (nameTextView.text.toString().isBlank()) {
            valid = false
            nameTextView.error = getString(R.string.required)
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
        return valid
    }
}