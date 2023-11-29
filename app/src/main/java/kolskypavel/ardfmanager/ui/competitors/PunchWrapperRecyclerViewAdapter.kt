package kolskypavel.ardfmanager.ui.competitors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.enums.PunchType
import kolskypavel.ardfmanager.backend.wrappers.PunchRecordsWrapper
import java.time.format.DateTimeFormatter

class PunchWrapperRecyclerViewAdapter(
    private var values: ArrayList<PunchRecordsWrapper>,
    private val context: Context
) :
    RecyclerView.Adapter<PunchWrapperRecyclerViewAdapter.PunchWrapperViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PunchWrapperViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_punch, parent, false)

        return PunchWrapperViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: PunchWrapperViewHolder, position: Int) {
        val item = values[position]

        holder.order.text = (position + 1).toString()
        if (item.siCode != null) {
            holder.code.setText(item.siCode.toString())
        }

        if (item.time != null) {
            val formatter = DateTimeFormatter.ofPattern("HH:MM:SS")
            holder.hours.setText(item.time!!.format(formatter))
        }

        holder.addBtn.setOnClickListener {
            addPunchWrapper(position)
        }

        holder.deleteBtn.setOnClickListener {
            deletePunchWrapper(position)
        }
        //Set the start punch
        if (item.punchType == PunchType.START) {
            holder.code.setText("S")
            holder.code.isEnabled = false
            holder.order.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE
        }

        //Set the finish punch
        else if (item.punchType == PunchType.FINISH) {
            holder.code.setText("F")
            holder.code.isEnabled = false
            holder.order.visibility = View.GONE
            holder.addBtn.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE
        }
    }

    private fun addPunchWrapper(position: Int) {
        values.add(position + 1, PunchRecordsWrapper(null, null, PunchType.CONTROL))
        notifyItemInserted(position + 1)
    }

    private fun deletePunchWrapper(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class PunchWrapperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var order: TextView = view.findViewById(R.id.punch_item_order)
        var code: EditText = view.findViewById(R.id.punch_item_si_code)
        var hours: EditText = view.findViewById(R.id.punch_item_time)
        var addBtn: ImageButton = view.findViewById(R.id.punch_item_add_btn)
        var deleteBtn: ImageButton = view.findViewById(R.id.punch_item_delete_btn)
    }

}