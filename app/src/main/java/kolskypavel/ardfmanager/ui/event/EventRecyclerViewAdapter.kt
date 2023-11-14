package kolskypavel.ardfmanager.ui.event

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import java.util.UUID

/**
 * [RecyclerView.Adapter] that can display a [Event].
 * TODO: Replace the implementation with code for your data type.
 */
class EventRecyclerViewAdapter(
    private var values: List<Event>, private val onEventClicked: (eventId: UUID) -> Unit,
    private val onMoreClicked: (action: Int, position: Int, event: Event) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder>() {

    private val dataProcessor = DataProcessor.get()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_event, parent, false)

        return EventViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = values[position]
        holder.title.text = item.name
        holder.date.text =
            item.date.toString() + " " + dataProcessor.getHoursMinutesFromTime(item.startTime)
        holder.type.text = dataProcessor.eventTypeToString(item.eventType)
        holder.level.text = dataProcessor.eventLevelToString(
            item.eventLevel
        )
        holder.itemView.setOnClickListener {
            onEventClicked(item.id)
        }
        holder.moreBtn.setOnClickListener {

            val popupMenu = PopupMenu(context, holder.moreBtn)
            popupMenu.inflate(R.menu.event_item_menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_edit_event -> {
                        onMoreClicked(0, position, item)
                        true
                    }

                    R.id.menu_item_delete_event -> {
                        onMoreClicked(1, position, item)
                        true
                    }

                    else -> {
                        onMoreClicked(2, position, item)
                        true
                    }
                }
            }
            popupMenu.show()
        }
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