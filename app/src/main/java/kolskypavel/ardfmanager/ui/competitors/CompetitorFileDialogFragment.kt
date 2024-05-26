package kolskypavel.ardfmanager.ui.competitors

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import kolskypavel.ardfmanager.R

class CompetitorFileDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_files_competitors, container, false)
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

        importButton = view.findViewById(R.id.competitor_file_import_btn)
        exportButton = view.findViewById(R.id.competitor_file_export_button)
        okButton = view.findViewById(R.id.competitor_file_ok)
        cancelButton = view.findViewById(R.id.competitor_file_cancel)


//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.type = "text/*"
//
//                getResult.launch(intent)

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }
}