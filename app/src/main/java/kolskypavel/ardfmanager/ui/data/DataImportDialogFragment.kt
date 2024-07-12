package kolskypavel.ardfmanager.ui.data

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel

class DataImportDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_data_import, container, false)
    }

    private val dataProcessor = DataProcessor.get()
    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()

    private lateinit var dataTypePicker: MaterialAutoCompleteTextView
    private lateinit var dataFormatPicker: MaterialAutoCompleteTextView
    private lateinit var importButton: Button
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    private val getResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val value = it.data
            val uri = value?.data

            if (uri != null) {
                importData(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)
        dialog?.setTitle(R.string.data_import_data)

        dataTypePicker = view.findViewById(R.id.data_import_type)
        dataFormatPicker = view.findViewById(R.id.data_import_format)
        importButton = view.findViewById(R.id.data_import_import_btn)
        okButton = view.findViewById(R.id.data_import_ok)
        cancelButton = view.findViewById(R.id.data_import_cancel)

        dataTypePicker.setText(getString(R.string.data_type_categories), false)
        dataFormatPicker.setText(getString(R.string.data_format_csv), false)
        importButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            setFlags(intent, getCurrentFormat())
            getResult.launch(intent)
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    private fun setFlags(intent: Intent, dataFormat: DataFormat) {
        when (dataFormat) {
            DataFormat.CSV -> {
                intent.type = "text/csv"
            }

            DataFormat.JSON -> {
                intent.type = "text/json"
            }

            DataFormat.IOF_XML -> {
                intent.type = "text/xml"
            }

            else -> {}
        }
    }

    private fun getCurrentType(): DataType {
        val text = dataTypePicker.text.toString()
        return dataProcessor.dataTypeFromString(text)
    }

    private fun getCurrentFormat(): DataFormat {
        val text = dataFormatPicker.text.toString()
        return dataProcessor.dataFormatFromString(text)
    }

    private fun importData(uri: Uri) {
        val currType = getCurrentType()
        val format = getCurrentFormat()
        when (currType) {
            DataType.CATEGORIES -> importCategories(uri, format)
            DataType.C0MPETITORS -> importCompetitors(uri, format)
            DataType.COMPETITOR_STARTS_TIME -> importStarts(uri, format)
            else -> {}
        }
    }

    private fun importCompetitors(uri: Uri, format: DataFormat) {
        val competitorWrapper = selectedRaceViewModel.importCompetitors(
            uri,
            format
        )
        if (competitorWrapper != null) {

        } else {

        }
    }

    private fun importCategories(uri: Uri, format: DataFormat) {

    }


    private fun importStarts(uri: Uri, format: DataFormat) {

    }
}