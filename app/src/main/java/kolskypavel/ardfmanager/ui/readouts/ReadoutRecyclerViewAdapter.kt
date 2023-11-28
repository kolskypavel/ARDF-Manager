package kolskypavel.ardfmanager.ui.readouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Readout

class ReadoutRecyclerViewAdapter(
    private var values: List<Readout>,
    private val context: Context
) : RecyclerView.Adapter<ReadoutRecyclerViewAdapter.ReadoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadoutViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_readout, parent, false)

        return ReadoutViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: ReadoutViewHolder, position: Int) {
        val item = values[position]

        holder.competitor.text = "TODO"
        holder.siNumber.text = item.siNumber.toString()
        holder.runTime.text = item.runTime.toString()
        holder.category.text = "TODO"
        holder.placement.text = "TODO"
    }

    inner class ReadoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var competitor: TextView = view.findViewById(R.id.readout_item_competitor)
        var siNumber: TextView = view.findViewById(R.id.readout_item_si_number)
        var runTime: TextView = view.findViewById(R.id.readout_item_run_time)
        var category: TextView = view.findViewById(R.id.readout_item_category)
        var placement: TextView = view.findViewById(R.id.readout_item_placement)
    }
}