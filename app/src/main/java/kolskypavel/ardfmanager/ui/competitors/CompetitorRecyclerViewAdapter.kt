package kolskypavel.ardfmanager.ui.competitors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Competitor

class CompetitorRecyclerViewAdapter(
    private var values: List<Competitor>, private val context: Context
) : RecyclerView.Adapter<CompetitorRecyclerViewAdapter.CompetitorViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompetitorViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_competitor, parent, false)

        return CompetitorViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: CompetitorViewHolder, position: Int) {
        val item = values[position]
        holder.name.text = item.name
        holder.club.text = item.club
        holder.index.text = item.index
    }

    override fun getItemCount(): Int = values.size
    inner class CompetitorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.competitor_item_name)
        val club: TextView = view.findViewById(R.id.competitor_item_club)
        val index: TextView = view.findViewById(R.id.competitor_item_index)
        val category: TextView = view.findViewById(R.id.competitor_item_category)
    }
}