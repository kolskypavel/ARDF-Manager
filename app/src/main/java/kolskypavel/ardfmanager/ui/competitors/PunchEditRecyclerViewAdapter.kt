package kolskypavel.ardfmanager.ui.competitors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.time.LocalTime
import java.util.UUID

class PunchEditRecyclerViewAdapter(
    var values: ArrayList<Punch>,
    private val context: Context
) :
    RecyclerView.Adapter<PunchEditRecyclerViewAdapter.PunchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PunchViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_punch_edit, parent, false)

        return PunchViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size


    override fun onBindViewHolder(holder: PunchViewHolder, position: Int) {
        val item = values[position]

        if (item.siTime != null) {
            holder.time.setText(item.siTime?.getTimeString())
            holder.weekday.setText(item.siTime?.getDayOfWeek().toString())
            holder.week.setText(item.siTime?.getWeek().toString())
        } else {
            holder.weekday.setText("0")
            holder.week.setText("0")
        }

        holder.addBtn.setOnClickListener {
            addPunch(holder.layoutPosition)
        }

        holder.deleteBtn.setOnClickListener {
            deletePunch(holder.layoutPosition)
        }

        //Set the start punch
        when (item.punchType) {
            SIRecordType.CHECK -> {}

            SIRecordType.START -> {
                holder.code.setText("S")
                holder.code.isEnabled = false
                holder.deleteBtn.visibility = View.GONE
            }

            SIRecordType.FINISH -> {
                holder.code.setText("F")
                holder.code.isEnabled = false
                holder.addBtn.visibility = View.GONE
                holder.deleteBtn.visibility = View.GONE
            }

            SIRecordType.CONTROL -> {
                if (item.siCode != 0) {
                    holder.code.setText(item.siCode.toString())
                } else {
                    holder.code.setText("")
                }
                holder.code.isEnabled = true
                holder.addBtn.visibility = View.VISIBLE
                holder.deleteBtn.visibility = View.VISIBLE
            }
        }

        //Set watchers
        holder.code.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            if (!codeWatcher(position, cs.toString())) {
                holder.code.error = context.getString(R.string.invalid)
            }
        }

        holder.time.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            if (!timeWatcher(position, cs.toString())) {
                holder.time.error = context.getString(R.string.invalid)
            }
        }

        holder.weekday.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            if (!dayWatcher(position, cs.toString())) {
                holder.weekday.error = context.getString(R.string.invalid)
            }
        }

        holder.week.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            if (!weekWatcher(position, cs.toString())) {
                holder.week.error = context.getString(R.string.invalid)
            }
        }
    }

    private fun addPunch(position: Int) {
        values.add(
            position + 1, Punch(
                UUID.randomUUID(),
                values[0].eventId,
                null,
                values[0].competitorId,
                null,
                SIRecordType.CONTROL,
                0,
                values[position].order++,
                values[position].siTime,
                null,
                PunchStatus.UNKNOWN
            )
        )
        notifyItemInserted(position + 1)
    }

    private fun deletePunch(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    //Text watchers
    private fun codeWatcher(position: Int, text: String): Boolean {
        try {
            val code = text.toInt()
            if (code >= SIConstants.SI_MIN_CODE && code <= SIConstants.SI_MAX_CODE) {
                values[position].siCode = code
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }


    private fun timeWatcher(position: Int, text: String): Boolean {
        //Try parsing the time into SI time
        try {
            val time = LocalTime.parse(text)
            if (values[position].siTime != null) {
                values[position].siTime?.setTime(time)
            } else {
                values[position].siTime = SITime(time)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun dayWatcher(position: Int, text: String): Boolean {
        try {
            val day = text.toInt()
            if (day in 0..7) {
                if (values[position].siTime != null) {
                    values[position].siTime?.setDayOfWeek(day)
                } else {
                    values[position].siTime = SITime(LocalTime.MIDNIGHT, day)
                }
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun weekWatcher(position: Int, text: String): Boolean {
        try {
            val week = text.toInt()
            if (week in 0..3) {
                if (values[position].siTime != null) {
                    values[position].siTime?.setWeek(week)
                } else {
                    values[position].siTime = SITime(LocalTime.MIDNIGHT, 0, week)
                }
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun isValid(): Boolean {
        //TODO: Validate punches
        return true
    }

    inner class PunchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var code: EditText = view.findViewById(R.id.punch_edit_item_si_code)
        var time: EditText = view.findViewById(R.id.punch_edit_item_time)
        var weekday: EditText = view.findViewById(R.id.punch_edit_item_weekday)
        var week: EditText = view.findViewById(R.id.punch_edit_item_week)
        var addBtn: ImageButton = view.findViewById(R.id.punch_edit_item_add_btn)
        var deleteBtn: ImageButton = view.findViewById(R.id.punch_edit_item_delete_btn)
    }

}