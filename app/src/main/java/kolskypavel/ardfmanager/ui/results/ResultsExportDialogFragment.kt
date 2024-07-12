package kolskypavel.ardfmanager.ui.results

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat

class ResultsExportDialogFragment : DialogFragment() {

    val dataProcessor = DataProcessor.get()
    private lateinit var dataFormatPicker: MaterialAutoCompleteTextView
    private lateinit var previewButton: Button
    private lateinit var exportButton: Button
    private lateinit var cancelButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_share_results, container, false)
    }

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
        dialog?.setTitle(R.string.results_share)

        dataFormatPicker = view.findViewById(R.id.results_data_format_picker)
        previewButton = view.findViewById(R.id.results_file_preview_btn)
        exportButton = view.findViewById(R.id.results_file_export_button)
        cancelButton = view.findViewById(R.id.results_file_cancel)

        setButtons()
    }

    private fun setButtons() {
        dataFormatPicker.setText(getText(R.string.data_format_csv), false)
        previewButton.setOnClickListener {

        }

        exportButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            setFlags(intent, getCurrentFormat())
            getResult.launch(intent)
        }


        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    private fun getCurrentFormat(): DataFormat {
        val text = dataFormatPicker.text.toString()
        return dataProcessor.dataFormatFromString(text)
    }

    private fun setFlags(intent: Intent, dataFormat: DataFormat) {
        when (dataFormat) {

            DataFormat.TXT -> {
                intent.type = "text/txt"
                intent.putExtra(Intent.EXTRA_TITLE, "results.txt")
            }

            DataFormat.CSV -> {
                intent.type = "text/csv"
                intent.putExtra(Intent.EXTRA_TITLE, "results.csv")
            }

            DataFormat.JSON -> {
                intent.type = "text/*"
                intent.putExtra(Intent.EXTRA_TITLE, "results.json")
            }

            DataFormat.IOF_XML -> {
                intent.type = "text/xml"
                intent.putExtra(Intent.EXTRA_TITLE, "results.xml")
            }

            DataFormat.PDF -> {
                intent.type = "application/pdf"
                intent.putExtra(Intent.EXTRA_TITLE, "results.pdf")
            }


            DataFormat.HTML -> {
                intent.type = "text/html"
                intent.putExtra(Intent.EXTRA_TITLE, "results.html")
            }

        }
    }
}