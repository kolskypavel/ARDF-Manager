package kolskypavel.ardfmanager.ui.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.databinding.FragmentEventSelectionBinding
import kolskypavel.ardfmanager.room.entitity.Event

/**
 * [RecyclerView.Adapter] that can display a [Event].
 * TODO: Replace the implementation with code for your data type.
 */
class EventRecyclerViewAdapter(
    private val values: List<Event>
) : RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentEventSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = values[position]
//        holder.idView.text = item.name
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentEventSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        val idView: TextView = binding.itemNumber
//        val contentView: TextView = binding.content

//        override fun toString(): String {
//            return super.toString() + " '" + contentView.text + "'"
//        }
    }

}