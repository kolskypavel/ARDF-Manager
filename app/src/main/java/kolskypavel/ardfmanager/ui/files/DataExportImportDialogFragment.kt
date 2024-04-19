package kolskypavel.ardfmanager.ui.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import kolskypavel.ardfmanager.R

class DataExportImportDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_import_export_data, container, false)
    }

    private lateinit var importButton: Button
    private lateinit var exportButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)
        dialog?.setTitle(R.string.file_import_export_title)

        importButton = view.findViewById(R.id.file_button_import)
        exportButton = view.findViewById(R.id.file_button_export)

//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.type = "text/*"
//
//                getResult.launch(intent)
    }
}