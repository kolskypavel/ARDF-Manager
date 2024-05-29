package kolskypavel.ardfmanager.ui.categories;

import android.content.Context
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
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.wrappers.ControlPointItemWrapper
import java.util.UUID

class ControlPointRecyclerViewAdapter(
    var values: ArrayList<ControlPointItemWrapper>,
    val raceId: UUID,
    val categoryId: UUID,
    private var raceType: RaceType,
    val context: Context
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

        if (item.controlPoint.siCode != -1) {
            holder.siCode.setText(item.controlPoint.siCode.toString())
        }
        if (item.controlPoint.name != null) {
            holder.name.setText(item.controlPoint.name)
        }

        holder.points.setText(item.controlPoint.points.toString())
        holder.separator.isChecked = item.controlPoint.separator

        holder.siCode.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            codeWatcher(cs.toString(), holder.layoutPosition, holder.siCode)
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
                        raceId,
                        categoryId,
                        -1,
                        item.controlPoint.order++,
                        null,
                        0,
                        1,
                        beacon = false,
                        separator = false
                    ), isCodeValid = false
                )
            )
            notifyItemInserted(holder.adapterPosition + 1)
        }

        holder.deleteBtn.setOnClickListener {
            //TODO: Fix the crash when deleting
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
        holder.beacon.visibility = View.GONE
        holder.addBtn.visibility = View.VISIBLE

        if (holder.adapterPosition == 0) {
            holder.deleteBtn.visibility = View.GONE
            holder.points.visibility = View.GONE
            holder.name.visibility = View.GONE
            holder.siCode.visibility = View.GONE
            holder.separator.visibility = View.GONE
        } else if (raceType == RaceType.CLASSICS ||
            raceType == RaceType.FOXORING
        ) {
            holder.separator.visibility = View.GONE
            holder.points.visibility = View.GONE
        } else if (raceType == RaceType.ORIENTEERING) {
            holder.separator.visibility = View.GONE
            holder.points.visibility = View.GONE
            holder.name.visibility = View.GONE
        } else if (raceType == RaceType.SPRINT) {
            holder.points.visibility = View.GONE
        }

        if (raceType != RaceType.ORIENTEERING &&
            holder.adapterPosition != 0 &&
            holder.layoutPosition == values.size - 1
        ) {
            holder.beacon.visibility = View.VISIBLE
            holder.addBtn.visibility = View.GONE
        }
    }

    //TODO: Validate the control points
    fun checkCodes(): Boolean {
        for (v in values) {
            if (!v.isCodeValid) {
                return false
            }
        }
        return true
    }

    fun getOriginalValues() = values

    //Return the current control points
    fun getControlPoints(): ArrayList<ControlPoint> {
        val cps = values
        cps.removeAt(0)
        return ControlPointItemWrapper.getControlPoints(cps)
    }

    //Returns true if a code is valid - not duplicate - does not matter in Orienteering or Custom
    //TODO: Further check for sprint
    private fun checkCodeDuplicate(code: Int, position: Int): Boolean {
        if (raceType == RaceType.CLASSICS || raceType == RaceType.FOXORING) {
            for ((counter, v) in values.withIndex()) {
                if (counter != position && v.controlPoint.siCode == code) {
                    return false
                }
            }
        }
        return true
    }

    private fun codeWatcher(string: String, position: Int, codeView: EditText) {
        if (string.isNotEmpty()) {
            try {
                val code = string.toInt()

                //Check for duplicates
                if (checkCodeDuplicate(code, position)) {
                    values[position].controlPoint.siCode = code
                    values[position].isCodeValid = true
                } else {
                    codeView.error = context.getString(R.string.duplicate)
                    values[position].isCodeValid = false
                }
            } catch (exception: Exception) {
                codeView.error = context.getString(R.string.invalid)
                values[position].isCodeValid = false
            }
        } else {
            values[position].controlPoint.siCode = -1
            values[position].isCodeValid = false
        }
    }

    private fun nameWatcher(string: String, position: Int, name: EditText) {
        if (string.isNotBlank()) {
            values[position].controlPoint.name = string
        } else {
            values[position].controlPoint.name = null
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
