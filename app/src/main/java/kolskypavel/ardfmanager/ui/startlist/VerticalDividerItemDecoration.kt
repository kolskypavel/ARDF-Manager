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
    private val columns: Int = 1,
    private val rowHeightPx: Int = 48
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
        if (childCount == 0 || columns <= 0) return

        // 1. Draw vertical dividers based on the first visible row of children
        // We calculate column widths once from the parent width
        val leftBound = parent.paddingLeft.toFloat()
        val rightBound = (parent.width - parent.paddingRight).toFloat()
        val usableWidth = rightBound - leftBound
        val colWidth = usableWidth / columns.toFloat()

        // We draw vertical lines spanning from the top of the first visible child to the bottom of the last one
        val firstChild = parent.getChildAt(0)
        val lastChild = parent.getChildAt(childCount - 1)
        val gridTop = firstChild.top.toFloat()
        val gridBottom = lastChild.bottom.toFloat()

        paint.pathEffect = null
        for (i in 0..columns) {
            val x = leftBound + colWidth * i
            c.drawLine(x, gridTop, x, gridBottom, paint)
        }

        // 2. Draw horizontal lines for each visible child
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val tag = child.getTag(R.id.grid_cell_text)
            
            var isSpanEnd = false
            var isCategory = false
            if (tag is Pair<*, *>) {
                isSpanEnd = tag.first as? Boolean ?: false
                isCategory = tag.second != null
            }

            // Draw horizontal line at the bottom of the cell if:
            // - It's not a category (time column or empty cell)
            // - It's a category and it's the end of its span
            if (!isCategory || isSpanEnd) {
                paint.pathEffect = if (!isCategory) dashed else null
                c.drawLine(child.left.toFloat(), child.bottom.toFloat(), child.right.toFloat(), child.bottom.toFloat(), paint)
            }
            
            // Special case: draw top line for the very first row of the entire grid if it's visible
            val position = parent.getChildAdapterPosition(child)
            if (position != RecyclerView.NO_POSITION && position < columns) {
                paint.pathEffect = null
                c.drawLine(child.left.toFloat(), child.top.toFloat(), child.right.toFloat(), child.top.toFloat(), paint)
            }
        }
        
        paint.pathEffect = null
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, 0)
    }
}
