package kolskypavel.ardfmanager.ui.competitors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import kolskypavel.ardfmanager.R

class CompetitorCreateDialogFragment : DialogFragment() {
    private val args: CompetitorCreateDialogFragmentArgs by navArgs()

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
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.add_dialog)

        cancelButton = view.findViewById(R.id.competitor_dialog_cancel)
        okButton = view.findViewById(R.id.competitor_dialog_ok)

        populateFields()
    }

    private fun populateFields() {
        if (args.create) {
            dialog?.setTitle(R.string.competitor_create)
        } else {
            dialog?.setTitle(R.string.competitor_edit)
        }

    }

    private fun setButtons() {
        okButton.setOnClickListener {

        }

        cancelButton.setOnClickListener {
            dialog?.dismiss()
        }
    }


    companion object {
        const val REQUEST_COMPETITOR_MODIFICATION = "REQUEST_COMPETITOR_MODIFICATION"
        const val BUNDLE_KEY_CREATE = "BUNDLE_KEY_CREATE"
        const val BUNDLE_KEY_COMPETITOR = "BUNDLE_KEY_COMPETITOR"
    }
}