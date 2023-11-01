package kolskypavel.ardfmanager.ui.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.room.entitity.Event
import kolskypavel.ardfmanager.room.entitity.EventBand
import kolskypavel.ardfmanager.room.entitity.EventLevel
import kolskypavel.ardfmanager.room.entitity.EventType
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * [RecyclerView.Adapter] that can display a [Event].
 * TODO: Replace the implementation with code for your data type.
 */
class EventRecyclerViewAdapter(
    private var values: List<Event>, private val onEventClicked: (eventId: UUID) -> Unit,
    private val onMoreClicked: (
        position: Int, name: String, date: LocalDate, startTime: LocalTime,
        eventType: EventType, eventLevel: EventLevel, eventBand: EventBand
    ) -> Unit
) : RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_event, parent, false)

        return EventViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = values[position]
        holder.title.text = item.name
        holder.date.text = item.date.toString()
        holder.level.text = item.level.toString()
        holder.type.text = item.eventType.toString()
        holder.itemView.setOnClickListener { onEventClicked(item.id) }

    }


    override fun getItemCount(): Int = values.size

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.event_item_title)
        val date: TextView = view.findViewById(R.id.event_item_date)
        val level: TextView = view.findViewById(R.id.event_item_level)
        val type: TextView = view.findViewById(R.id.event_item_type)
        val moreBtn: ImageButton = view.findViewById(R.id.event_item_more_btn)
    }
}