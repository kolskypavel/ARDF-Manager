package kolskypavel.ardfmanager.ui.startlist.adapers

import android.content.ClipData
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.ui.startlist.CategoryDrawWrapper
import java.util.UUID

// Simple cell model: a cell may be empty, a start of a spanned categoryDrawWrapper, or a placeholder pointing to a start index
data class CellModel(
    var categoryDrawWrapper: CategoryDrawWrapper? = null,
    var spanStartIndex: Int? = null,
    var isSpanEnd: Boolean = false, // mark last row of a span so we can draw a bottom separator
    var timeText: String? = null // if non-null, this cell is a time label (first column)
)

class GridAdapter(
    private val columns: Int,
    private var cells: MutableList<CellModel>,
    private val baseRowHeightPx: Int = 64 // default px; fragment should pass converted dp value
) : RecyclerView.Adapter<GridAdapter.VH>() {

    // track spans for start indices (startIndex -> spanRows)
    private val spans = mutableMapOf<Int, Int>()

    data class Placement(val category: CategoryDrawWrapper, val startIndex: Int)

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.grid_cell_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_cell, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cell = cells[position]
        val lp = holder.itemView.layoutParams
        // Ensure every cell has the base row height (uniform rows)
        if (lp != null) {
            lp.height = baseRowHeightPx
            holder.itemView.layoutParams = lp
        }

        // Time cell has priority
        val time = cell.timeText
        if (time != null) {
            holder.text.text = time
            holder.text.setBackgroundResource(android.R.color.transparent)
            // align to top-left and add small inset
            holder.text.gravity = Gravity.START or Gravity.TOP
            val topDp = 4f
            val leftDp = 8f
            val density = holder.itemView.resources.displayMetrics.density
            val topPx = (topDp * density).toInt()
            val leftPx = (leftDp * density).toInt()
            holder.text.setPadding(
                leftPx,
                topPx,
                holder.text.paddingRight,
                holder.text.paddingBottom
            )
        } else {
            val category = cell.categoryDrawWrapper
            holder.text.gravity = Gravity.CENTER
            if (category != null) {
                // show category color for every cell covered by the span
                holder.text.setBackgroundColor(category.color)
                // show text only on the span start cell
                if (cell.spanStartIndex == position) holder.text.text =
                    category.getCategoryName() else holder.text.text = ""
                // reset padding for category cells
                holder.text.setPadding(0, 0, 0, 0)
            } else {
                holder.text.text = ""
                holder.text.setBackgroundResource(android.R.color.transparent)
                holder.text.setPadding(0, 0, 0, 0)
            }
        }

        // expose whether this cell is the end of a span so decoration can draw a bottom separator
        val catIdTag = cell.categoryDrawWrapper?.getCategoryId()
        holder.itemView.setTag(R.id.grid_cell_text, Pair(cell.isSpanEnd, catIdTag))

        // Start drag on press from any cell that belongs to a placed category
        holder.itemView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val cat = cell.categoryDrawWrapper
                val startIdx = cell.spanStartIndex ?: position
                if (cat != null && startIdx >= 0) {
                    // prevent parent RecyclerView from intercepting touch so drag starts reliably
                    var parent = v.parent
                    while (parent is View) {
                        if (parent is RecyclerView) {
                            parent.requestDisallowInterceptTouchEvent(true)
                            break
                        }
                        parent = parent.parent
                    }
                    val clip = ClipData.newPlainText("categoryId", cat.getCategoryId().toString())
                    v.startDragAndDrop(clip, View.DragShadowBuilder(v), Pair(cat, startIdx), 0)
                    v.performClick()
                    true
                } else false
            } else false
        }
    }

    override fun getItemCount(): Int = cells.size

    fun updateCells(newCells: MutableList<CellModel>) {
        this.cells = newCells
        this.spans.clear()
        notifyDataSetChanged()
    }

    fun getPlacements(): List<Placement> {
        return spans.mapNotNull { (idx, _) ->
            val cat = cells.getOrNull(idx)?.categoryDrawWrapper
            if (cat != null) Placement(cat, idx) else null
        }
    }

    // remove an existing span by its start index
    fun removeSpan(startIndex: Int) {
        val spanRows = spans[startIndex] ?: return
        val startCol = startIndex % columns
        val startRow = startIndex / columns
        for (r in 0 until spanRows) {
            val idx = (startRow + r) * columns + startCol
            if (idx >= 0 && idx < cells.size) {
                cells[idx].categoryDrawWrapper = null
                cells[idx].spanStartIndex = null
                cells[idx].isSpanEnd = false
            }
        }
        spans.remove(startIndex)
        val first = startIndex
        val last = ((startRow + spanRows - 1) * columns + startCol).coerceAtMost(cells.size - 1)
        notifyItemRangeChanged(first, last - first + 1)
    }

    // Check if a category spanning `spanRows` rows can be placed starting at `startIndex`.
    fun canPlaceAt(startIndex: Int, spanRows: Int, ignoreStartIndex: Int? = null): Boolean {
        if (startIndex < 0 || startIndex >= cells.size) return false
        if (spanRows <= 0) return false
        val startCol = startIndex % columns
        val startRow = startIndex / columns
        for (r in 0 until spanRows) {
            val idx = (startRow + r) * columns + startCol
            if (idx < 0 || idx >= cells.size) return false
            val c = cells[idx]
            // don't allow placement into a time column cell
            if (c.timeText != null) return false
            if (c.categoryDrawWrapper != null) {
                // allow if this cell belongs to the span we're moving (ignoreStartIndex)
                if (ignoreStartIndex != null && c.spanStartIndex == ignoreStartIndex) {
                    continue
                }
                return false
            }
        }
        return true
    }

    // Place a categoryDrawWrapper spanning multiple rows starting at startIndex.
    fun setCellSpan(startIndex: Int, categoryDrawWrapper: CategoryDrawWrapper?, spanRows: Int) {
        if (startIndex < 0 || startIndex >= cells.size) return
        if (spanRows <= 0) return
        val startCol = startIndex % columns
        val startRow = startIndex / columns
        spans[startIndex] = spanRows
        // set categoryDrawWrapper for every cell in the spanned rows of the same column
        for (r in 0 until spanRows) {
            val idx = (startRow + r) * columns + startCol
            if (idx >= 0 && idx < cells.size) {
                cells[idx].categoryDrawWrapper = categoryDrawWrapper
                // mark span start index on the first cell; others point to start index
                cells[idx].spanStartIndex = startIndex
                // mark whether this cell is the last row of the span
                cells[idx].isSpanEnd = (r == spanRows - 1)
            } else break
        }
        // notify affected items (the rows in this column)
        val first = startIndex
        val last = ((startRow + spanRows - 1) * columns + startCol).coerceAtMost(cells.size - 1)
        notifyItemRangeChanged(first, last - first + 1)
    }

    fun clearAll() {
        for (i in cells.indices) {
            cells[i].categoryDrawWrapper = null
            cells[i].spanStartIndex = null
            cells[i].isSpanEnd = false
            cells[i].timeText = null
        }
        spans.clear()
        notifyDataSetChanged()
    }
}
