package kolskypavel.ardfmanager.ui.readouts

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import kolskypavel.ardfmanager.backend.sportident.SITime
import kolskypavel.ardfmanager.backend.wrappers.PunchEditItemWrapper
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class ReadoutEditDialogFragment : DialogFragment() {
    private val args: ReadoutEditDialogFragmentArgs by navArgs()
    private lateinit var selectedRaceViewModel: SelectedRaceViewModel
    private val dataProcessor = DataProcessor.get()

    private lateinit var readout: Readout
    private var origReadout: Readout? = null

    private lateinit var competitors: List<Competitor>
    private val competitorArr = ArrayList<String>()

    private lateinit var competitorPicker: MaterialAutoCompleteTextView
    private lateinit var competitorPickerLayout: TextInputLayout
    private lateinit var siNumberInput: TextInputEditText
    private lateinit var siNumberInputLayout: TextInputLayout
    private lateinit var raceStatusPicker: MaterialAutoCompleteTextView
    private val statusArr = ArrayList<String>()
    private lateinit var punchEditRecyclerView: RecyclerView
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_edit_readout, container, false)
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

        val sl: SelectedRaceViewModel by activityViewModels()
        selectedRaceViewModel = sl
        competitors = selectedRaceViewModel.getCompetitors().sortedBy { com -> com.lastName }

        competitorPicker = view.findViewById(R.id.readout_dialog_competitor)
        competitorPickerLayout = view.findViewById(R.id.readout_dialog_competitor_layout)
        siNumberInput = view.findViewById(R.id.readout_dialog_si_number)
        siNumberInputLayout = view.findViewById(R.id.readout_dialog_si_number_layout)
        raceStatusPicker = view.findViewById(R.id.readout_dialog_status)
        punchEditRecyclerView = view.findViewById(R.id.readout_dialog_punch_recycler_view)
        okButton = view.findViewById(R.id.readout_dialog_ok)
        cancelButton = view.findViewById(R.id.readout_dialog_cancel)

        populateFields()
        setButtons()
    }

    private fun populateFields() {
        if (args.create) {
            dialog?.setTitle(R.string.readout_create_readout)

            readout =
                Readout(
                    UUID.randomUUID(), null, 0,
                    selectedRaceViewModel.getCurrentRace().id,
                    null, null, null, null,
                    LocalDateTime.now(),
                    true
                )
            raceStatusPicker.setText(getString(R.string.automatic), false)
            competitorPicker.setText(getString(R.string.unknown_competitor), false)

        } else {
            dialog?.setTitle(R.string.readout_edit_readout)
            readout = args.readoutData!!.readoutResult.readout
            origReadout = readout

            if (readout.siNumber != null) {
                siNumberInput.setText(readout.siNumber.toString())
            }

            if (!args.readoutData!!.readoutResult.result.automaticStatus) {
                raceStatusPicker.setText(
                    dataProcessor.raceStatusToString(args.readoutData!!.readoutResult.result.raceStatus),
                    false
                )
            } else {
                raceStatusPicker.setText(getString(R.string.automatic), false)
            }

            if (readout.competitorID != null) {
                val competitor = selectedRaceViewModel.getCompetitor(readout.competitorID!!)!!
                competitorPicker.setText(competitor.getNameWithStartNumber())
                siNumberInputLayout.isEnabled = false
            } else {
                siNumberInputLayout.isEnabled = true
                competitorPicker.setText(getString(R.string.unknown_competitor), false)
            }
        }

        // Competitor setup
        for (comp in competitors) {
            competitorArr.add("${comp.getFullName()} (${comp.startNumber})")
        }
        competitorArr.add(
            0,
            getString(R.string.unknown_competitor)
        ) //Add the empty competitor option
        val competitorAdapter: ArrayAdapter<String> =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                competitorArr
            )

        competitorPicker.setAdapter(competitorAdapter)

        //Update the readout
        competitorPicker.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                competitorPickerLayout.error = ""
                val competitor = getCompetitorFromPicker()
                readout.competitorID = competitor?.id
                siNumberInput.setText("")

                siNumberInputLayout.isEnabled = competitor == null
            }

        //SINumber setup
        siNumberInput.doOnTextChanged { _, _, _, _ ->
            siNumberInputLayout.error = ""
            readout.siNumber = getSINumber()
        }

        // Punches setup
        var punchWrappers = ArrayList<PunchEditItemWrapper>()

        if (args.create || args.readoutData?.readoutResult?.punches?.isEmpty() == true) {
            punchWrappers.add(
                PunchEditItemWrapper(
                    Punch(
                        UUID.randomUUID(),
                        dataProcessor.getCurrentRace().id,
                        null,
                        null,
                        null,
                        0,
                        SITime(LocalTime.MIN),
                        SIRecordType.START,
                        0,
                        PunchStatus.VALID,
                        Duration.ZERO
                    ), true, true, true, true
                )
            )
            punchWrappers.add(
                PunchEditItemWrapper(
                    Punch(
                        UUID.randomUUID(),
                        dataProcessor.getCurrentRace().id,
                        null,
                        null,
                        null,
                        0,
                        SITime(LocalTime.MIN),
                        SIRecordType.FINISH,
                        0,
                        PunchStatus.VALID,
                        Duration.ZERO
                    ), true, true, true, true
                )
            )
        } else {
            punchWrappers =
                PunchEditItemWrapper.getWrappers(ArrayList(args.readoutData!!.readoutResult.punches))
        }

        punchEditRecyclerView.adapter =
            PunchEditRecyclerViewAdapter(punchWrappers)

        //Populate the status options
        for (status in RaceStatus.entries) {
            statusArr.add(dataProcessor.raceStatusToString(status))
        }

        statusArr.add(0, getString(R.string.automatic))
        val statusAdapter: ArrayAdapter<String> =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                statusArr
            )

        raceStatusPicker.setAdapter(statusAdapter)
    }

    private fun setButtons() {
        okButton.setOnClickListener {
            if (validateFields()) {

                if (siNumberInput.text?.isNotEmpty() == true) {
                    readout.siNumber = siNumberInput.text.toString().toInt()
                }
                val punches = PunchEditItemWrapper.getPunches(
                    (punchEditRecyclerView.adapter as PunchEditRecyclerViewAdapter).values
                )
                runBlocking {
                    selectedRaceViewModel.processManualPunches(
                        readout,
                        punches,
                        getRaceStatusFromPicker()
                    )
                }

                setFragmentResult(
                    REQUEST_READOUT_MODIFICATION, bundleOf(
                        BUNDLE_READOUT_ID to readout.id.toString()
                    )
                )
                dialog?.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }
    }

    private fun validateFields(): Boolean {
        var valid = true

        //Check competitor
        if (readout.competitorID != null
            && origReadout?.competitorID != readout.competitorID
            && selectedRaceViewModel.getReadoutByCompetitor(readout.competitorID!!) != null
        ) {
            competitorPickerLayout.error = getString(R.string.readout_competitor_exists)
            valid = false
        }

        //Check SI
        if (readout.siNumber != null) {

            if (readout.siNumber != origReadout?.siNumber) {

                //Check for duplicate readouts with same SI number
                if (selectedRaceViewModel.getReadoutBySINumber(
                        readout.siNumber!!,
                        selectedRaceViewModel.getCurrentRace().id
                    ) != null
                ) {
                    siNumberInputLayout.error = getString(R.string.readout_si_exists)
                    valid = false
                }
            }

        } else if (readout.competitorID == null) {
            siNumberInputLayout.error = getString(R.string.required)
            valid = false
        }

        //Check punches
        if (!(punchEditRecyclerView.adapter as PunchEditRecyclerViewAdapter).isValid()) {
            valid = false
        }

        return valid
    }

    private fun getCompetitorFromPicker(): Competitor? {
        val compText = competitorPicker.text.toString()
        val compPos = competitorArr.indexOf(compText)
        return if (compPos > 0) {
            competitors[compPos - 1]
        } else null
    }

    private fun getSINumber(): Int? {
        if (siNumberInput.text.toString().isNotEmpty()) {
            try {
                val siNumber = siNumberInput.text.toString().toInt()
                if (SIConstants.isSINumberValid(siNumber)) {
                    return siNumber
                }
                siNumberInputLayout.error = getString(R.string.si_number_invalid_range)

            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun getRaceStatusFromPicker(): RaceStatus? {
        val raceStatusString = raceStatusPicker.text.toString()
        return if (raceStatusString.isNotEmpty()
            && raceStatusString == requireContext().getString(R.string.automatic)
        ) {
            null
        } else {
            dataProcessor.raceStatusStringToEnum(raceStatusString)
        }
    }

    companion object {
        const val REQUEST_READOUT_MODIFICATION = "REQUEST_READOUT_MODIFICATION"
        const val BUNDLE_READOUT_ID = "BUNDLE_KEY_READOUT_ID"
    }
}