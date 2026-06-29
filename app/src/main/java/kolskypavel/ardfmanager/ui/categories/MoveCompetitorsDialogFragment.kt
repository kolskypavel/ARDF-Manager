package kolskypavel.ardfmanager.ui.categories

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import java.util.UUID

class MoveCompetitorsDialogFragment : DialogFragment() {

    private val args: MoveCompetitorsDialogFragmentArgs by navArgs()
    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_move_competitors, container, false)
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
        setWidthPercent(95)

        val originTextView = view.findViewById<TextView>(R.id.move_competitors_dialog_origin)
        val categoryLayout = view.findViewById<TextInputLayout>(R.id.move_competitors_dialog_category_layout)
        val categoryAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.move_competitors_dialog_category)
        val okButton = view.findViewById<Button>(R.id.assign_dialog_ok)
        val cancelButton = view.findViewById<Button>(R.id.assign_dialog_cancel)

        dialog?.setTitle(getString(R.string.category_move_competitors))
        originTextView.text = getString(R.string.category_original_category, args.sourceCategoryName)

        val sourceCategoryId = UUID.fromString(args.sourceCategoryId)
        val categories = selectedRaceViewModel.getCategories().filter { it.id != sourceCategoryId }

        val noCategoryString = getString(R.string.no_category)
        val categoryNames = mutableListOf(noCategoryString)
        categoryNames.addAll(categories.map { it.name })

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        categoryAutoComplete.setAdapter(adapter)

        var selectedDestinationCategoryId: UUID? = null
        var selectionMade = false

        categoryAutoComplete.setOnItemClickListener { _, _, position, _ ->
            selectedDestinationCategoryId = if (position == 0) {
                null
            } else {
                categories[position - 1].id
            }
            selectionMade = true
            categoryLayout.error = null
        }

        okButton.setOnClickListener {
            if (selectionMade) {
                selectedRaceViewModel.moveCompetitorsToCategory(sourceCategoryId, selectedDestinationCategoryId)
                dismiss()
            } else {
                categoryLayout.error = getString(R.string.general_required)
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
}
