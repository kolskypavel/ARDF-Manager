package kolskypavel.ardfmanager.ui.races

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kotlin.getValue

class InternetImportDialogFragment : DialogFragment() {
    private val dataProcessor = DataProcessor.get()
    private lateinit var raceViewModel: RaceViewModel

    private lateinit var typePicker: MaterialAutoCompleteTextView
    private lateinit var apiKeyEditText: TextInputEditText
    private lateinit var errorTextView: TextView
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    private var raceData: RaceData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_internet_import, container, false)
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

        val sl: RaceViewModel by activityViewModels()
        raceViewModel = sl

        dialog?.setTitle(R.string.race_import_internet)

        typePicker = view.findViewById(R.id.internet_import_dialog_type)
        apiKeyEditText = view.findViewById(R.id.internet_import_dialog_apikey)
        errorTextView = view.findViewById(R.id.internet_import_dialog_error)
        cancelButton = view.findViewById(R.id.internet_import_dialog_cancel)
        okButton = view.findViewById(R.id.robis_import_dialog_ok)

        setButtons()
        setFragmentListener()
    }

    private fun setFragmentListener() {
        setFragmentResultListener(RaceEditDialogFragment.REQUEST_RACE_MODIFICATION) { _, bundle ->
            val action =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) bundle.getSerializable(
                    RaceEditDialogFragment.BUNDLE_KEY_ACTIONS,
                    RaceEditDialogFragment.RaceEditActions::class.java
                )
                else {
                    bundle.getSerializable(RaceEditDialogFragment.BUNDLE_KEY_ACTIONS) as Race

                }

            val race: Race = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    RaceEditDialogFragment.BUNDLE_KEY_RACE,
                    Race::class.java
                )!!
            } else {
                bundle.getSerializable(RaceEditDialogFragment.BUNDLE_KEY_RACE) as Race
            }
            if (action == RaceEditDialogFragment.RaceEditActions.IMPORT) {
                raceData?.let { raceData ->
                    raceData.race = race
                    raceViewModel.saveRaceData(raceData)
                    dialog?.dismiss()
                }
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        // Require both that the network advertises internet and that the system validated it.
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun setButtons() {
        typePicker.setText(requireContext().getString(R.string.provider_type_robis))

        okButton.setOnClickListener {

            val apiKey = apiKeyEditText.text.toString()
            if (apiKey.isNotBlank()) {
                if (isNetworkConnected()) {
                    val providerType =
                        dataProcessor.providerTypeFromString(typePicker.text.toString())

                    try {
                        raceData =
                            raceViewModel.fetchProviderRaceData(
                                providerType,
                                apiKey,
                                requireContext()
                            )
                        raceData?.race?.apiKey = apiKey         // Preset API key, because its not returned in response
                        findNavController().navigate(
                            InternetImportDialogFragmentDirections.importInternetRace(
                                RaceEditDialogFragment.RaceEditActions.IMPORT, -1, raceData!!.race
                            )
                        )

                    } catch (e: Exception) {
                        errorTextView.text = e.message
                    }

                } else {
                    errorTextView.text = getString(R.string.result_service_status_no_network)
                }
            } else {
                apiKeyEditText.error = getString(R.string.general_required)
            }
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }
}