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
import kolskypavel.ardfmanager.backend.room.entitity.Punch

class ReadoutDetailFragment : Fragment() {

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

        competitorNameView.text = readoutDetail.competitor?.name
        siNumberView.text = readoutDetail.readout.siNumber.toString()
        clubView.text = readoutDetail.competitor?.club
        indexView.text = readoutDetail.competitor?.index
        startTimeView.text = readoutDetail.readout.startTime?.time.toString()
        finishTimeView.text = readoutDetail.readout.finishTime?.time.toString()
        runTimeView.text = readoutDetail.readout.runTime.toString()
        categoryView.text = readoutDetail.category?.name

        if (readoutDetail.competitor != null) {
            pointsView.text = readoutDetail.competitor!!.points.toString()
        } else {
            pointsView.text = "0"
        }

        //TODO: Place

        setRecyclerViewAdapter(readoutDetail.punches)
    }

    private fun setRecyclerViewAdapter(punches: ArrayList<Punch>) {
        punchRecyclerView.adapter = PunchRecyclerViewAdapter(punches.toList(), requireContext())
    }
}