/*
 * Copyright 2018 Jeremy Patrick Pacabis
 * Copyright 2017-2018 Evren Coşkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ph.ingenuity.tableview.util

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.widget.LinearLayout

/**
 * Created by jeremypacabis on February 28, 2018.
 * @author Jeremy Patrick Pacabis <jeremy@ingenuity.ph>
 * ph.ingenuity.tableview.util <android-tableview-kotlin>
 */
object TableViewUtils {

    fun setWidth(view: View, width: Int) {
        val params = view.layoutParams as RecyclerView.LayoutParams
        params.width = width
        view.layoutParams = params
        val widthMeasureSpec = makeMeasureSpec(width, EXACTLY)
        val heightMeasureSpec = makeMeasureSpec(
                view.measuredHeight,
                EXACTLY
        )

        view.measure(widthMeasureSpec, heightMeasureSpec)
        view.requestLayout()
    }

    fun getWidth(view: View): Int {
        view.measure(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                makeMeasureSpec(
                        view.measuredHeight,
                        EXACTLY
                )
        )

        return view.measuredWidth
    }
}
