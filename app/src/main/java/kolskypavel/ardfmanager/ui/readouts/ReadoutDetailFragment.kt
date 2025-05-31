package kolskypavel.ardfmanager.ui.readouts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ResultData
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import java.util.UUID

class ReadoutDetailFragment : Fragment() {

    private val dataProcessor = DataProcessor.get()
    private val args: ReadoutDetailFragmentArgs by navArgs()
    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()
    private lateinit var resultData: ResultData

    private lateinit var readoutDetailToolbar: Toolbar
    private lateinit var punchRecyclerView: RecyclerView
    private lateinit var competitorNameView: TextView
    private lateinit var siNumberView: TextView
    private lateinit var clubView: TextView
    private lateinit var indexView: TextView
    private lateinit var runTimeView: TextView
    private lateinit var raceStatusView: TextView
    private lateinit var categoryView: TextView
    private lateinit var pointsView: TextView
    private lateinit var placeView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_readout_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resultData = args.resultData

        readoutDetailToolbar = view.findViewById(R.id.readout_detail_toolbar)
        punchRecyclerView = view.findViewById(R.id.readout_detail_punch_recycler_view)
        competitorNameView = view.findViewById(R.id.readout_detail_competitor_name)
        siNumberView = view.findViewById(R.id.readout_detail_si_number)
        clubView = view.findViewById(R.id.readout_detail_club)
        indexView = view.findViewById(R.id.readout_detail_index_callsign)
        runTimeView = view.findViewById(R.id.readout_detail_run_time)
        raceStatusView = view.findViewById(R.id.readout_detail_status)
        categoryView = view.findViewById(R.id.readout_detail_category)
        pointsView = view.findViewById(R.id.readout_detail_points)
        placeView = view.findViewById(R.id.readout_detail_place)

        readoutDetailToolbar.setNavigationIcon(R.drawable.ic_back)
        readoutDetailToolbar.setTitle(R.string.readout_detail_title)
        readoutDetailToolbar.subtitle =
            args.resultData.result.siNumber?.toString()
        readoutDetailToolbar.inflateMenu(R.menu.fragment_menu_readout_detail)

        readoutDetailToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        setResultListener()
        populateFields()
    }

    private fun populateFields() {

        if (resultData.competitorCategory?.competitor != null) {
            clubView.text = resultData.competitorCategory!!.competitor.club
            indexView.text = resultData.competitorCategory!!.competitor.index
            competitorNameView.text =
                "${resultData.competitorCategory!!.competitor.firstName} ${resultData.competitorCategory!!.competitor.lastName}"
            pointsView.text = resultData.result.points.toString()
        } else {
            competitorNameView.text = getText(R.string.unknown_competitor)
            pointsView.text = getText(R.string.unknown)
            clubView.text = getText(R.string.unknown)
            indexView.text = getText(R.string.unknown)
        }
        raceStatusView.text =
            dataProcessor.raceStatusToString(resultData.result.raceStatus)

        if (resultData.competitorCategory?.category != null) {
            categoryView.text = resultData.competitorCategory!!.category!!.name
        } else {
            categoryView.text = getText(R.string.unknown)
        }

        siNumberView.text = if (resultData.result.siNumber != null) {
            resultData.result.siNumber.toString()
        } else {
            "-"
        }
        runTimeView.text =
            TimeProcessor.durationToMinuteString(resultData.result.runTime)

        placeView.text = resultData.result.place?.toString()
            ?: getText(R.string.unknown) //TODO: Place

        setMenuActions()
        setRecyclerViewAdapter(resultData.punches)
    }

    private fun setMenuActions() {
        readoutDetailToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.readout_detail_menu_edit_readout -> {
                    findNavController().navigate(
                        ReadoutDetailFragmentDirections.editReadout(
                            false,
                            resultData, -1
                        )
                    )
                    true
                }

                R.id.readout_detail_menu_print_ticket -> {
                    true
                }

                R.id.readout_detail_menu_create_category -> {
                    findNavController().navigate(
                        ReadoutDetailFragmentDirections.createCategoryFromReadout(
                            true,
                            -1,
                            null,
                            dataProcessor.getStringFromPunches(resultData.getPunchList())
                        )
                    )
                    true
                }

                R.id.readout_detail_menu_delete_readout -> {
                    confirmReadoutDeletion(resultData)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun confirmReadoutDeletion(resultData: ResultData) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.readout_delete_readout))
        val message =
            getString(
                R.string.readout_delete_readout_confirmation,
                resultData.result.siNumber
            )
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedRaceViewModel.deleteResult(resultData.result.id)
            dialog.dismiss()
            parentFragmentManager.popBackStackImmediate();
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun setResultListener() {
        setFragmentResultListener(ReadoutEditDialogFragment.REQUEST_READOUT_MODIFICATION) { _, bundle ->
            val resultId = bundle.getString(
                ReadoutEditDialogFragment.BUNDLE_RESULT_ID
            )
            val newData =
                selectedRaceViewModel.getResultData(UUID.fromString(resultId))
                resultData = newData
                populateFields()

        }
    }

    private fun setRecyclerViewAdapter(punches: List<AliasPunch>) {
        punchRecyclerView.adapter = PunchRecyclerViewAdapter(punches, requireContext())
    }
}