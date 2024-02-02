package kolskypavel.ardfmanager.ui.categories;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.enums.EventType
import java.util.UUID

class ControlPointRecyclerViewAdapter(
    var values: ArrayList<ControlPoint>,
    val eventId: UUID,
    val categoryId: UUID,
    private var eventType: EventType
) :
    RecyclerView.Adapter<ControlPointRecyclerViewAdapter.ControlPointViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControlPointViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_controlpoint, parent, false)

        return ControlPointViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: ControlPointViewHolder, position: Int) {
        val item = values[position]

        if (item.siCode != null) {
            holder.siCode.setText(item.siCode.toString())
        }
        if (item.name != null) {
            holder.name.setText(item.name)
        }

        holder.points.setText(item.points.toString())
        holder.separator.isChecked = item.separator

        holder.siCode.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            codeWatcher(cs.toString(), holder.layoutPosition)
        }
        holder.name.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            nameWatcher(cs.toString(), holder.layoutPosition)
        }

        holder.addBtn.setOnClickListener {
            values.add(
                holder.adapterPosition + 1,
                ControlPoint(
                    UUID.randomUUID(),
                    eventId,
                    categoryId,
                    null,
                    null,
                    item.order++,
                    0,
                    1,
                    beacon = false,
                    separator = false
                )
            )
            notifyItemInserted(position + 1)
        }

        holder.deleteBtn.setOnClickListener {
            if (holder.adapterPosition != 0) {
                values.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        }

        if (position == 0) {
            holder.deleteBtn.visibility = View.GONE
            holder.points.visibility = View.GONE
            holder.name.visibility = View.GONE
            holder.siCode.visibility = View.GONE
            holder.separator.visibility = View.GONE
        }
        if (eventType == EventType.CLASSICS || eventType == EventType.FOXORING || eventType == EventType.ORIENTEERING) {
            holder.separator.visibility = View.GONE
            holder.points.visibility = View.GONE
        } else if (eventType == EventType.SPRINT) {
            holder.points.visibility = View.GONE
        }

    }

    //TODO: Validate the control points
    fun checkCodes(): Boolean {
        return true
    }

    fun getOriginalValues() = values

    //Return the current control points
    fun getControlPoints(): ArrayList<ControlPoint> {
        val cps = values
        cps.removeAt(0)
        return cps
    }

    private fun codeWatcher(string: String, position: Int) {
        if (string.isNotEmpty()) {
            values[position].siCode = string.toInt()
        } else {
            values[position].siCode = null
        }
    }

    private fun nameWatcher(string: String, position: Int) {
        if (string.isNotBlank()) {
            values[position].name = string
        } else {
            values[position].name = null
        }
    }

    inner class ControlPointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: EditText = view.findViewById(R.id.control_point_item_name)
        var siCode: EditText = view.findViewById(R.id.control_point_item_code)
        var points: EditText = view.findViewById(R.id.control_point_item_points)
        var separator: CheckBox = view.findViewById(R.id.control_point_item_separator)

        var addBtn: ImageButton = view.findViewById(R.id.control_point_item_add_btn)
        var deleteBtn: ImageButton = view.findViewById(R.id.control_point_item_delete_btn)
    }
}
