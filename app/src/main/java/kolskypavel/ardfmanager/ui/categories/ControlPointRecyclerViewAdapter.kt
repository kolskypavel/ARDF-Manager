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
import kolskypavel.ardfmanager.backend.wrappers.ControlPointItemWrapper
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import java.util.UUID

class ControlPointRecyclerViewAdapter(
    var values: ArrayList<ControlPointItemWrapper>,
    val eventId: UUID,
    val categoryId: UUID,
    private var eventType: EventType,
    private var selectedEventViewModel: SelectedEventViewModel
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

        if (item.controlPoint.siCode != null) {
            holder.siCode.setText(item.controlPoint.siCode.toString())
        }
        if (item.controlPoint.name != null) {
            holder.name.setText(item.controlPoint.name)
        }

        holder.points.setText(item.controlPoint.points.toString())
        holder.separator.isChecked = item.controlPoint.separator

        holder.siCode.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            codeWatcher(cs.toString(), holder.layoutPosition)
        }
        holder.name.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            nameWatcher(cs.toString(), holder.layoutPosition, holder.name)
        }

        holder.beacon.setOnCheckedChangeListener { _, checked ->
            values[holder.adapterPosition].controlPoint.beacon = checked
        }

        holder.addBtn.setOnClickListener {
            values.add(
                holder.adapterPosition + 1, ControlPointItemWrapper(
                    ControlPoint(
                        UUID.randomUUID(),
                        eventId,
                        categoryId,
                        null,
                        null,
                        item.controlPoint.order++,
                        0,
                        1,
                        beacon = false,
                        separator = false
                    ), isCodeValid = true,
                    isNameValid = true
                )
            )
            notifyItemInserted(holder.adapterPosition + 1)
        }

        holder.deleteBtn.setOnClickListener {
            if (holder.adapterPosition != 0) {

                //Change beacon
                if (holder.adapterPosition == values.size - 1 && values.size > 2) {
                    values[holder.adapterPosition - 1].controlPoint.beacon = holder.beacon.isChecked
                    notifyItemChanged(holder.adapterPosition - 1)
                }

                values.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        }


        if (holder.adapterPosition == 0) {
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
        if (eventType != EventType.ORIENTEERING && holder.adapterPosition != 0 && holder.adapterPosition == values.size - 1) {
            holder.beacon.visibility = View.VISIBLE
            holder.addBtn.visibility = View.GONE
            item.controlPoint.beacon = true
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
        return ControlPointItemWrapper.getControlPoints(cps)
    }

    private fun codeWatcher(string: String, position: Int) {
        if (string.isNotEmpty()) {
            values[position].controlPoint.siCode = string.toInt()
        } else {
            values[position].controlPoint.siCode = null
            values[position].isCodeValid = false
        }
    }

    private fun nameWatcher(string: String, position: Int, name: EditText) {
        if (string.isNotBlank()) {
            values[position].controlPoint.name = string

            //Already existing control point
            if (selectedEventViewModel.checkIfControlPointNameExists(
                    values[position].controlPoint.siCode,
                    values[position].controlPoint.name!!
                )
            ) {
                name.error = "Control point already exists"
                values[position].isNameValid = false
            }

        } else {
            values[position].controlPoint.name = null
            values[position].isNameValid = true
        }
    }

    inner class ControlPointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: EditText = view.findViewById(R.id.control_point_item_name)
        var siCode: EditText = view.findViewById(R.id.control_point_item_code)
        var points: EditText = view.findViewById(R.id.control_point_item_points)
        var separator: CheckBox = view.findViewById(R.id.control_point_item_separator)
        var beacon: CheckBox = view.findViewById(R.id.control_point_item_beacon)

        var addBtn: ImageButton = view.findViewById(R.id.control_point_item_add_btn)
        var deleteBtn: ImageButton =
            view.findViewById(R.id.control_point_item_delete_btn)
    }
}
