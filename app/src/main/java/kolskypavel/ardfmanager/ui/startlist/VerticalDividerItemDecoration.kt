package kolskypavel.ardfmanager.ui.startlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R

class VerticalDividerItemDecoration(
    context: Context,
    private val color: Int,
    private val strokeWidthPx: Float = 2f,
    private val columns: Int = 1, // number of columns in the grid (including time column if present)
    private val rowHeightPx: Int = 48 // height of a single row in px (adapter provides this)
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        isAntiAlias = true
        this.color = this@VerticalDividerItemDecoration.color
        strokeWidth = strokeWidthPx
        style = Paint.Style.STROKE
    }

    private val dashed = DashPathEffect(floatArrayOf(strokeWidthPx * 4, strokeWidthPx * 4), 0f)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        if (childCount == 0 || columns <= 0 || rowHeightPx <= 0) return

        val leftBound = parent.paddingLeft.toFloat()
        val rightBound = (parent.width - parent.paddingRight).toFloat()
        val topBound = parent.paddingTop.toFloat()
        val bottomBound = (parent.height - parent.paddingBottom).toFloat()

        val usableWidth = rightBound - leftBound
        val colWidth = usableWidth / columns.toFloat()

        // draw vertical lines between columns (i from 1..columns-1)
        for (i in 1 until columns) {
            val x = leftBound + colWidth * i
            c.drawLine(x, topBound, x, bottomBound, paint)
        }

        // draw horizontal separators at row boundaries
        // compute how many visible rows fit into the viewport
        val visibleHeight = bottomBound - topBound
        val numVisibleRows = (visibleHeight / rowHeightPx).toInt() + 2 // +2 to cover partial

        // Build a map of rowIndex -> whether any cell in that row is a span end of a category
        val rowHasSpanEnd = mutableMapOf<Int, Boolean>()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val tag = child.getTag(R.id.grid_cell_text)
            if (tag is Pair<*, *>) {
                val isSpanEnd = tag.first as? Boolean ?: false
                val catId = tag.second
                // Only treat as span end if catId is non-null (time cells have null)
                if (isSpanEnd && catId != null) {
                    // compute row index of this child
                    val rowIndex = ((child.top - parent.paddingTop) / rowHeightPx.toFloat()).toInt()
                    rowHasSpanEnd[rowIndex] = true
                }
            }
        }

        // Determine topmost visible row index
        val firstVisibleRow = ((parent.computeVerticalScrollOffset() - parent.paddingTop) / rowHeightPx.toFloat()).toInt().coerceAtLeast(0)

        for (r in firstVisibleRow until firstVisibleRow + numVisibleRows) {
            val y = topBound + r * rowHeightPx
            if (y < topBound || y > bottomBound) continue

            // if any column has a span end at this row -> solid, else dashed
            val solid = rowHasSpanEnd[r] == true
            if (solid) {
                paint.pathEffect = null
            } else {
                paint.pathEffect = dashed
            }
            // draw full-width horizontal line
            c.drawLine(leftBound, y, rightBound, y, paint)
        }

        // reset pathEffect
        paint.pathEffect = null
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // no offsets; we don't want extra spacing introduced
        outRect.set(0, 0, 0, 0)
    }
}
