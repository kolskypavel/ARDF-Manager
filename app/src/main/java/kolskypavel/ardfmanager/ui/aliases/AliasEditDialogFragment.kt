package kolskypavel.ardfmanager.ui.aliases

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor

class AliasEditDialogFragment : DialogFragment() {
    private val dataProcessor = DataProcessor.get()

    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_manage_aliases, container, false)
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
        setStyle(STYLE_NORMAL, R.style.add_dialog)
        setWidthPercent(95)

        cancelButton = view.findViewById(R.id.alias_dialog_cancel)
        okButton = view.findViewById(R.id.alias_dialog_ok)

        dialog?.setTitle(getString(R.string.category_manage_aliases))
        setAdapter()
        setButtons()
    }

    private fun setAdapter() {

    }

    private fun setButtons() {
        cancelButton.setOnClickListener {
            dialog?.cancel()
        }

        okButton.setOnClickListener {

            dialog?.dismiss()
        }
    }

}