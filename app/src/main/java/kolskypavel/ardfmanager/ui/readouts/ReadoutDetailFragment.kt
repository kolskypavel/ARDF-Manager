package kolskypavel.ardfmanager.ui.readouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Punch

class ReadoutDetailFragment : Fragment() {

    private val dataProcessor = DataProcessor.get()
    private val args: ReadoutDetailFragmentArgs by navArgs()

    private lateinit var readoutDetailToolbar: Toolbar
    private lateinit var punchRecyclerView: RecyclerView
    private lateinit var competitorNameView: TextView
    private lateinit var siNumberView: TextView
    private lateinit var clubView: TextView
    private lateinit var indexView: TextView
    private lateinit var startTimeView: TextView
    private lateinit var finishTimeView: TextView
    private lateinit var runTimeView: TextView
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
        startTimeView = view.findViewById(R.id.readout_detail_start_time)
        finishTimeView = view.findViewById(R.id.readout_detail_finish_time)
        runTimeView = view.findViewById(R.id.readout_detail_run_time)
        categoryView = view.findViewById(R.id.readout_detail_category)
        pointsView = view.findViewById(R.id.readout_detail_points)
        placeView = view.findViewById(R.id.readout_detail_place)
        populateFields()
    }

    private fun populateFields() {
        val readoutDetail = args.readoutDetail

        readoutDetailToolbar.setNavigationIcon(R.drawable.ic_back)
        readoutDetailToolbar.setTitle(R.string.readout_detail_title)
        readoutDetailToolbar.subtitle = readoutDetail.readout.siNumber.toString()
        readoutDetailToolbar.inflateMenu(R.menu.fragment_menu_readout_detail)

        readoutDetailToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        if (readoutDetail.competitor != null) {
            clubView.text = readoutDetail.competitor?.club
            indexView.text = readoutDetail.competitor?.index
            competitorNameView.text =
                "${readoutDetail.competitor?.firstName} ${readoutDetail.competitor?.lastName}"
            pointsView.text = readoutDetail.readout.points.toString()
        } else {
            competitorNameView.text = getText(R.string.unknown_competitor)
            pointsView.text = getText(R.string.unknown)
            clubView.text = getText(R.string.unknown)
            indexView.text = getText(R.string.unknown)
        }

        if (readoutDetail.category != null) {
            categoryView.text = readoutDetail.category!!.name
        } else {
            categoryView.text = getText(R.string.unknown)
        }

        siNumberView.text = readoutDetail.readout.siNumber.toString()
        startTimeView.text = readoutDetail.readout.startTime?.getTime().toString()
        finishTimeView.text = readoutDetail.readout.finishTime?.getTime().toString()
        runTimeView.text =
            readoutDetail.readout.runTime?.let { dataProcessor.durationToString(it) }.orEmpty()

        placeView.text = getText(R.string.unknown) //TODO: Place

        setMenuActions()
        setRecyclerViewAdapter(readoutDetail.punches)
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

    private fun setRecyclerViewAdapter(punches: ArrayList<Punch>) {
        punchRecyclerView.adapter = PunchRecyclerViewAdapter(punches.toList(), requireContext())
    }
}