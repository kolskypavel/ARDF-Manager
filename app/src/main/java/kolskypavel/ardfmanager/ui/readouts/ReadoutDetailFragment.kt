package kolskypavel.ardfmanager.ui.readouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import java.util.UUID

class ReadoutDetailFragment : Fragment() {

    private val dataProcessor = DataProcessor.get()
    private val args: ReadoutDetailFragmentArgs by navArgs()
    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()

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
        populateFields()
    }

    private fun populateFields() {
        val readoutDetail = args.readoutDetail

        readoutDetailToolbar.setNavigationIcon(R.drawable.ic_back)
        readoutDetailToolbar.setTitle(R.string.readout_detail_title)
        readoutDetailToolbar.subtitle = readoutDetail.readoutResult.readout.siNumber?.toString()
        readoutDetailToolbar.inflateMenu(R.menu.fragment_menu_readout_detail)

        readoutDetailToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        if (readoutDetail.competitorCategory?.competitor != null) {
            clubView.text = readoutDetail.competitorCategory!!.competitor.club
            indexView.text = readoutDetail.competitorCategory!!.competitor.index
            competitorNameView.text =
                "${readoutDetail.competitorCategory!!.competitor.firstName} ${readoutDetail.competitorCategory!!.competitor?.lastName}"
            pointsView.text = readoutDetail.readoutResult.result.points.toString()
        } else {
            competitorNameView.text = getText(R.string.unknown_competitor)
            pointsView.text = getText(R.string.unknown)
            clubView.text = getText(R.string.unknown)
            indexView.text = getText(R.string.unknown)
        }
        raceStatusView.text =
            dataProcessor.raceStatusToString(readoutDetail.readoutResult.result.raceStatus)

        if (readoutDetail.competitorCategory?.category != null) {
            categoryView.text = readoutDetail.competitorCategory!!.category!!.name
        } else {
            categoryView.text = getText(R.string.unknown)
        }

        siNumberView.text = if (readoutDetail.readoutResult.readout.siNumber != null) {
            readoutDetail.readoutResult.readout.siNumber.toString()
        } else {
            "-"
        }
        runTimeView.text =
            TimeProcessor.durationToMinuteString(readoutDetail.readoutResult.result.runTime)

        placeView.text = getText(R.string.unknown) //TODO: Place

        setMenuActions()
        setRecyclerViewAdapter(readoutDetail.readoutResult.readout.id)
    }

    private fun setMenuActions() {
        readoutDetailToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.readout_detail_menu_edit_readout -> {
                    true
                }

                R.id.readout_detail_menu_print_ticket -> {
                    true
                }

                R.id.readout_detail_menu_create_category -> {
                    true
                }

                R.id.readout_detail_menu_delete_readout -> {
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun setRecyclerViewAdapter(readoutId: UUID) {
        val punches = selectedRaceViewModel.getPunchesByReadout(readoutId)
        punchRecyclerView.adapter = PunchRecyclerViewAdapter(punches, requireContext())

    }
}