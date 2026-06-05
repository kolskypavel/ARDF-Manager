package kolskypavel.ardfmanager.ui.startlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import java.util.UUID

class CategoryDrawWrapperAdapter(
    private val items: MutableList<CategoryDrawWrapper>,
    private val crossDragStart: (View, CategoryDrawWrapper) -> Unit
) : RecyclerView.Adapter<CategoryDrawWrapperAdapter.VH>() {

    // optional callback set by the owner to start ItemTouchHelper drag for reorder
    var itemDragStart: ((RecyclerView.ViewHolder) -> Unit)? = null

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.category_card_name)
        val count: TextView = itemView.findViewById(R.id.category_card_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category_chip, parent, false)
        return VH(v)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: VH, position: Int) {
        // use holder.adapterPosition to be robust against adapter changes
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) return
        val c = items[pos]
        holder.name.text = c.getCategoryName()
        // show competitor count if available
        holder.count.text = if (c.getCompetitorCount() > 0) "${c.getCompetitorCount()}" else ""

        // Start drag on press
        holder.itemView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                itemDragStart?.invoke(holder)
                crossDragStart(v, c)
                v.performClick()
                true
            } else false
        }
    }

    override fun getItemCount(): Int = items.size

    fun moveItem(from: Int, to: Int) {
        if (from == to) return
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    fun getItems(): List<CategoryDrawWrapper> = items

    // Remove the first item with matching id and return it, or null if not found
    fun removeById(id: UUID): CategoryDrawWrapper? {
        val idx = items.indexOfFirst { it.getCategoryId() == id }
        return if (idx >= 0) {
            val removed = items.removeAt(idx)
            notifyItemRemoved(idx)
            removed
        } else null
    }

    // Add item at the end (used when returning a category from the grid)
    fun addItem(item: CategoryDrawWrapper) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
}
