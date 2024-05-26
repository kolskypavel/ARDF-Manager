package kolskypavel.ardfmanager.ui.categories

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import kolskypavel.ardfmanager.R

class CategoryFileDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_files_categories, container, false)
    }

    private lateinit var importButton: Button
    private lateinit var exportButton: Button
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    private val getResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val value = it.data
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)
        dialog?.setTitle(R.string.category_file_import_export)

        importButton = view.findViewById(R.id.category_file_import_btn)
        exportButton = view.findViewById(R.id.category_file_export_button)
        okButton = view.findViewById(R.id.category_file_ok)
        cancelButton = view.findViewById(R.id.category_file_cancel)

        setButtons()
    }

    private fun setButtons() {
        importButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"

            getResult.launch(intent)
        }

        exportButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"

            getResult.launch(intent)
        }

        okButton.setOnClickListener {
            dialog?.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }
}