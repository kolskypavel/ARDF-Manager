package kolskypavel.ardfmanager.ui.readouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import java.util.UUID

class ReadoutRecyclerViewAdapter(
    private var values: List<Readout>,
    private val context: Context,
    private val onReadoutClicked: (readoutId: UUID) -> Unit,
) : RecyclerView.Adapter<ReadoutRecyclerViewAdapter.ReadoutViewHolder>() {
    val dataProcessor = DataProcessor.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadoutViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_readout, parent, false)

        return ReadoutViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: ReadoutViewHolder, position: Int) {
        val item = values[position]

//        CoroutineScope(Dispatchers.IO).launch {
//            holder.competitor = dataProcessor.getCompetitorBySINumber(item.siNumber, item.eventId)
//            if (holder.competitor != null) {
//                holder.category = dataProcessor.getCategory(holder.competitor!!.categoryId!!)
//            }
//        }
//
//        if (holder.competitor != null) {
//            holder.competitorView.text = holder.competitor!!.name
//        } else {
//            holder.competitorView.setText(R.string.unknown_si)
//
//            if (holder.category != null) {
//                holder.categoryView.text = holder.category?.name
//            } else {
//                holder.categoryView.text = "?"
//            }
//        }

        holder.siNumberView.text = item.siNumber.toString()
        holder.runTimeView.text = item.runTime.toString()
        holder.placementView.text = "TODO"

        //Set readout detail navigation
        holder.itemView.setOnClickListener {
            onReadoutClicked(item.id)
        }
    }

    inner class ReadoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var competitor: Competitor? = null
        var category: Category? = null
        var competitorView: TextView = view.findViewById(R.id.readout_item_competitor)
        var siNumberView: TextView = view.findViewById(R.id.readout_item_si_number)
        var runTimeView: TextView = view.findViewById(R.id.readout_item_run_time)
        var categoryView: TextView = view.findViewById(R.id.readout_item_category)
        var placementView: TextView = view.findViewById(R.id.readout_item_placement)
    }
}