package kolskypavel.ardfmanager.ui.categories

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
import kolskypavel.ardfmanager.backend.room.entitity.Category

class CategoryRecyclerViewAdapter(
    private var values: List<Category>,
    private val onMoreClicked: (action: Int, position: Int, category: Category) -> Unit,
    private val context: Context
) :
    RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {

    private val dataProcessor = DataProcessor.get()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_category, parent, false)

        return CategoryViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = values[position]
        holder.title.text = item.name
        holder.type.text = dataProcessor.eventTypeToString(
            item.eventType ?: dataProcessor.getCurrentEvent().eventType
        ) //TODO: fix crash
        holder.gender.text = dataProcessor.genderToString(item.isWoman)
        holder.siCodes.text = item.controlPointsCodes

        if (item.maxAge != null) {
            holder.maxAge.text = item.maxAge.toString()
        }

        holder.moreBtn.setOnClickListener {

            val popupMenu = PopupMenu(context, holder.moreBtn)
            popupMenu.inflate(R.menu.context_menu_category)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_edit_category -> {
                        onMoreClicked(0, position, item)
                        true
                    }

                    R.id.menu_item_edit_category -> {
                        onMoreClicked(1, position, item)
                        true
                    }

                    R.id.menu_item_delete_category -> {
                        onMoreClicked(2, position, item)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.category_item_title)
        var type: TextView = view.findViewById(R.id.category_item_type)
        var gender: TextView = view.findViewById(R.id.category_item_gender)
        var maxAge: TextView = view.findViewById(R.id.category_item_max_age)
        var siCodes: TextView = view.findViewById(R.id.category_item_codes)
        var moreBtn: ImageButton = view.findViewById(R.id.category_item_more_btn)
    }
}