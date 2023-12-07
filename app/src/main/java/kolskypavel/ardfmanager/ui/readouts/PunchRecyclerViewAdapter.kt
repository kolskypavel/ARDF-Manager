package kolskypavel.ardfmanager.ui.readouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Punch

class PunchRecyclerViewAdapter(
    private var values: List<Punch>,
    private val context: Context
) :
    RecyclerView.Adapter<PunchRecyclerViewAdapter.PunchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PunchViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_punch, parent, false)

        return PunchViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: PunchViewHolder, position: Int) {
        val item = values[position]

        holder.punchOrder.text = position.toString()
        holder.punchSiCode.text = item.siCode.toString()
        holder.punchRealTime.text = item.siTime.time.toString()
        holder.punchSplit.text = item.split.toString()
    }

    inner class PunchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var punchOrder: TextView = view.findViewById(R.id.punch_order)
        var punchSiCode: TextView = view.findViewById(R.id.punch_si_code)
        var punchRealTime: TextView = view.findViewById(R.id.punch_real_time)
        var punchSplit: TextView = view.findViewById(R.id.punch_punch_split)
    }
}