package kolskypavel.ardfmanager.ui.categories;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.enums.EventType

public class ControlPointRecyclerViewAdapter(var values: List<ControlPoint>) :
    RecyclerView.Adapter<ControlPointRecyclerViewAdapter.ControlPointViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControlPointViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_controlpoint, parent, false)

        return ControlPointViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: ControlPointViewHolder, position: Int) {
        val item = values[position]

        holder.name.setText(item.name)
        holder.siCode.setText(item.siCode.toString())
        holder.points.setText(item.points.toString())
        holder.separator.isChecked = item.separator
        holder.beacon.isChecked = item.beacon

        holder.addBtn.setOnClickListener {

        }

        holder.deleteBtn.setOnClickListener {

        }
    }

    fun checkCodes(eventType: EventType) {

    }

    inner class ControlPointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: EditText = view.findViewById(R.id.control_point_item_name)
        var siCode: EditText = view.findViewById(R.id.control_point_item_code)
        var points: EditText = view.findViewById(R.id.control_point_item_points)
        var separator: CheckBox = view.findViewById(R.id.control_point_item_separator)
        var beacon: CheckBox = view.findViewById(R.id.control_point_item_beacon)

        var addBtn: ImageButton = view.findViewById(R.id.control_point_item_add_btn)
        var deleteBtn: ImageButton = view.findViewById(R.id.control_point_item_delete_btn)
    }
}
