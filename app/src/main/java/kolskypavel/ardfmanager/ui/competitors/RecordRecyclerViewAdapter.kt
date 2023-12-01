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
import kolskypavel.ardfmanager.backend.room.enums.RecordType
import kolskypavel.ardfmanager.backend.wrappers.RecordWrapper

class RecordRecyclerViewAdapter(
    private var values: ArrayList<RecordWrapper>,
    private val context: Context
) :
    RecyclerView.Adapter<RecordRecyclerViewAdapter.PunchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PunchViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_punch, parent, false)

        return PunchViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: PunchViewHolder, position: Int) {
        val item = values[position]

        holder.order.text = (position + 1).toString()
        holder.code.setText(item.siCode.toString())

        holder.time.setText("${item.siTime?.time?.hour}:${item.siTime?.time?.minute}:${item.siTime?.time?.second}")

        holder.addBtn.setOnClickListener {
            addPunchWrapper(position)
        }

        holder.deleteBtn.setOnClickListener {
            deletePunchWrapper(position)
        }
        //Set the start punch
        if (item.recordType == RecordType.START) {
            holder.code.setText("S")
            holder.code.isEnabled = false
            holder.order.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE
        }

        //Set the finish punch
        else if (item.recordType == RecordType.FINISH) {
            holder.code.setText("F")
            holder.code.isEnabled = false
            holder.order.visibility = View.GONE
            holder.addBtn.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE
        }
    }

    private fun addPunchWrapper(position: Int) {
//        values.add(
//            position + 1, SIPort.PunchData()
//        )
        notifyItemInserted(position + 1)
    }

    private fun deletePunchWrapper(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class PunchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var order: TextView = view.findViewById(R.id.punch_item_order)
        var code: EditText = view.findViewById(R.id.punch_item_si_code)
        var time: EditText = view.findViewById(R.id.punch_item_time)
        var addBtn: ImageButton = view.findViewById(R.id.punch_item_add_btn)
        var deleteBtn: ImageButton = view.findViewById(R.id.punch_item_delete_btn)
    }

}