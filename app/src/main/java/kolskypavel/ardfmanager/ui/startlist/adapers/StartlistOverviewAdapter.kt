package kolskypavel.ardfmanager.ui.startlist.adapers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import java.util.Collections

class StartlistOverviewAdapter(
    private var items: MutableList<Competitor>,
    private val onItemsReordered: (List<Competitor>) -> Unit
) : RecyclerView.Adapter<StartlistOverviewAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.startlist_overview_item_time)
        val name: TextView = itemView.findViewById(R.id.startlist_overview_item_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_startlist_overview, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val competitor = items[position]
        holder.time.text = competitor.drawnRelativeStartTime?.let {
            TimeProcessor.durationToFormattedString(it, true)
        } ?: "--:--"
        holder.name.text = "${competitor.getFullName()} (${competitor.index})"
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Competitor>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        // Swap the start times so they stay fixed to the positions
        val fromTime = items[fromPosition].drawnRelativeStartTime
        val toTime = items[toPosition].drawnRelativeStartTime
        
        items[fromPosition].drawnRelativeStartTime = toTime
        items[toPosition].drawnRelativeStartTime = fromTime

        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        // Notify changes to update the time text views
        notifyItemChanged(fromPosition)
        notifyItemChanged(toPosition)
    }

    fun onDragFinished() {
        onItemsReordered(items)
    }
}
