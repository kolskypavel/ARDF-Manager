package kolskypavel.ardfmanager.ui.competitors

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import de.codecrafters.tableview.TableDataAdapter
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.CompetitorCategory


class CompetitorTableViewAdapter(
    private var values: List<CompetitorCategory>,
    private val context: Context,
    private val onMoreClicked: (action: Int, position: Int, competitor: CompetitorCategory) -> Unit,
) : TableDataAdapter<CompetitorCategory>(context, values) {

    override fun getCellView(rowIndex: Int, columnIndex: Int, parentView: ViewGroup?): View {
        val item = values[rowIndex]
        val view = layoutInflater.inflate(R.layout.competitor_table_cell, parentView, false)
        val text: TextView = view.findViewById(R.id.competitor_table_cell_text)

        when (columnIndex) {
            0 -> text.text = item.competitor.firstName
            1 -> text.text = item.competitor.lastName
            2 -> text.text = item.competitor.club
            3 -> text.text = item.category?.name ?: context.getString(R.string.no_category)
        }

        //Set context menu
        view.setOnLongClickListener { w ->
            val popupMenu = PopupMenu(context, w)
            popupMenu.inflate(R.menu.context_menu_competitor)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_edit_competitor -> {
                        onMoreClicked(0, rowIndex, item)
                        true
                    }

                    R.id.menu_item_edit_competitor -> {
                        onMoreClicked(1, rowIndex, item)
                        true
                    }

                    R.id.menu_item_delete_competitor -> {
                        onMoreClicked(2, rowIndex, item)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
            true
        }
        return view
    }
}