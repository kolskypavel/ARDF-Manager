package kolskypavel.ardfmanager.ui.startlist

import android.content.ClipData
import android.os.Bundle
import android.util.TypedValue
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import java.util.UUID
import java.time.Duration

class StartlistDrawFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CategoryDrawWrapperAdapter
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var drawButton: FloatingActionButton
    private lateinit var intervalInput: TextInputEditText
    private lateinit var categoryIntervalInput: TextInputEditText


    private lateinit var gridRecycler: RecyclerView
    private lateinit var gridAdapter: GridAdapter

    // keep top categories visible even when adapter is empty
    private var categoriesObserver: RecyclerView.AdapterDataObserver? = null

    private var curInterval: Duration = Duration.ofMinutes(5L)
    private var curCategoryInterval: Duration = Duration.ofMinutes(5L)
    private var currentRows: Int = 20

    private fun createSample(name: String, count: Int, color: Int): CategoryDrawWrapper {
        val cat = Category(name)
        val comps = List(count) { Competitor() }
        val catData = CategoryData(cat, emptyList(), comps)
        return CategoryDrawWrapper(catData, color)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_startlist, container, false)

        intervalInput = view.findViewById(R.id.startlist_interval_input)
        categoryIntervalInput = view.findViewById(R.id.startlist_category_interval_input)
        recycler = view.findViewById(R.id.startlist_categories_recycler)
        drawButton = view.findViewById(R.id.startlist_btn_draw)

        // use a grid for categories: 5 columns to fit more items per row
        val categoryColumns = 5
        recycler.layoutManager = GridLayoutManager(requireContext(), categoryColumns)

        // sample categories for preview; replace with real data from ViewModel
        val sample = mutableListOf(
            createSample("M21", 4, 0xFF8EFAC8.toInt()),
            createSample("W21", 2, 0xFF8EFAC8.toInt()),
            createSample("M19", 1, 0xFF8EFAC8.toInt()),
            createSample("W19", 3, 0xFF8EFAC8.toInt()),
            createSample("M40", 2, 0xFF8EFAC8.toInt()),
            createSample("W40", 1, 0xFF8EFAC8.toInt()),
            createSample("M50", 1, 0xFF8EFAC8.toInt()),
            createSample("W50", 2, 0xFF8EFAC8.toInt()),
        )

        adapter = CategoryDrawWrapperAdapter(sample) { v, cat ->
            val clip = ClipData.newPlainText("categoryId", cat.getCategoryId().toString())
            v.startDragAndDrop(clip, View.DragShadowBuilder(v), cat, 0)
        }

        recycler.adapter = adapter

        // keep a small minimum height for the top categories list when it's empty
        val minTopListHeightDp = 64f
        val minTopListHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            minTopListHeightDp,
            resources.displayMetrics
        ).toInt()

        categoriesObserver = object : RecyclerView.AdapterDataObserver() {
            private fun update() {
                recycler.minimumHeight = if (adapter.itemCount == 0) minTopListHeightPx else 0
            }

            override fun onChanged() = update()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = update()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = update()
        }
        adapter.registerAdapterDataObserver(categoriesObserver!!)
        if (adapter.itemCount == 0) recycler.minimumHeight = minTopListHeightPx

        val callback = CategoryTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recycler)

        // wire adapter to request ItemTouchHelper to start a drag when a chip is pressed
        adapter.itemDragStart = { holder ->
            touchHelper.startDrag(holder)
        }

        // grid wiring
        gridRecycler = view.findViewById(R.id.startlist_grid_recycler)

        // total columns includes the time column as first column
        val totalColumns = StartlistConstants.STARTLIST_COLUMNS + 1

        // initial placeholder rows (ensure minimum for easy scrolling)
        val rows = currentRows.coerceAtLeast(StartlistConstants.STARTLIST_MIN_ROWS)
        val cellsCount = rows * totalColumns
        // create CellModel list (so only start cells render the category block); set timeText in first column
        val cells = MutableList<CellModel>(cellsCount) { CellModel(null, null, false, null) }
        // fill time labels using currentIntervalMinutes (default 5)
        var acc = Duration.ZERO
        for (r in 0 until rows) {
            acc += curInterval
            val timeLabel = TimeProcessor.durationToFormattedString(acc, true)
            val idx = r * totalColumns // first column
            if (idx in cells.indices) cells[idx].timeText = timeLabel
        }

        // convert dp->px once and pass to adapter
        val cellHeightDp = 32f
        val metrics = resources.displayMetrics
        val cellHeightPx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cellHeightDp, metrics).toInt()

        // grid adapter: drag is started directly by the GridAdapter cell touch handler
        gridAdapter = GridAdapter(totalColumns, cells, cellHeightPx)
        gridRecycler.layoutManager = GridLayoutManager(requireContext(), totalColumns)
        gridRecycler.adapter = gridAdapter

        val dividerColor = resources.getColor(android.R.color.darker_gray, requireContext().theme)
        gridRecycler.addItemDecoration(
            VerticalDividerItemDecoration(
                requireContext(),
                dividerColor,
                2f,
                totalColumns, // ensure decoration knows the column count
                cellHeightPx
            )
        )
        gridRecycler.isNestedScrollingEnabled = false

        drawButton.setOnClickListener {

        }

        // Allow dropping a category back to the categories recycler: re-add only if missing
        recycler.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DROP -> {
                    val local = event.localState
                    if (local is Pair<*, *>) {
                        val cat = local.first as? CategoryDrawWrapper
                        val startIndex = local.second as? Int
                        if (cat != null) {
                            // only add back if not already present (avoid duplicates)
                            val exists =
                                adapter.getItems().any { it.getCategoryId() == cat.getCategoryId() }
                            if (!exists) adapter.addItem(cat)
                            // remove original span if present
                            if (startIndex != null) gridAdapter.removeSpan(startIndex)
                            true
                        } else false
                    } else if (local is CategoryDrawWrapper) {
                        // dragged directly from top list and dropped back - nothing to do
                        true
                    } else false
                }

                else -> false
            }
        }

        gridRecycler.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    event.clipDescription != null
                }

                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DROP -> {
                    val clip = event.clipData
                    if (clip != null && clip.itemCount > 0) {
                        val idText = clip.getItemAt(0).text.toString()
                        val catId = try {
                            UUID.fromString(idText)
                        } catch (_: Exception) {
                            null
                        }
                        val local = event.localState
                        var categoryDrawWrapper: CategoryDrawWrapper? = null
                        var originStartIndex: Int? = null
                        if (local is Pair<*, *>) {
                            categoryDrawWrapper = local.first as? CategoryDrawWrapper
                            originStartIndex = local.second as? Int
                        } else if (local is CategoryDrawWrapper) categoryDrawWrapper = local
                        else if (catId != null) categoryDrawWrapper =
                            sample.find { it.getCategoryId() == catId }

                        if (categoryDrawWrapper != null) {
                            // compute target index based on x,y
                            val rx = event.x.toInt()
                            val ry = event.y.toInt()
                            val cellWidth = v.width / totalColumns
                            val col = (rx / cellWidth).coerceIn(0, totalColumns - 1)
                            // use the actual number of rows used to build the grid to avoid OOB
                            val row = (ry / cellHeightPx).coerceIn(0, rows - 1)
                            val index = row * totalColumns + col

                            // determine span rows based on number of competitors in the categoryDrawWrapper
                            val spanRows = categoryDrawWrapper.getCompetitorCount().coerceAtLeast(1)

                            // forbid placing into the time column (col == 0)
                            if (col == 0) {
                                Toast.makeText(
                                    requireContext(),
                                    "Cannot place on time column",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnDragListener false
                            }

                            // check availability: if originStartIndex != null (moving within grid), allow its original cells
                            val canPlace = gridAdapter.canPlaceAt(index, spanRows, originStartIndex)
                            if (!canPlace) {
                                Toast.makeText(
                                    requireContext(),
                                    requireContext().getString(R.string.startlist_space_occupied),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnDragListener false
                            }

                            // now commit: if dragged from top list, remove from adapter; if from grid, remove original span
                            if (local is CategoryDrawWrapper) {
                                adapter.removeById(categoryDrawWrapper.getCategoryId())
                            }
                            if (originStartIndex != null) {
                                // if moving to the same start index, no-op; otherwise remove original span before placing
                                if (originStartIndex != index) gridAdapter.removeSpan(
                                    originStartIndex
                                )
                            }

                            gridAdapter.setCellSpan(index, categoryDrawWrapper, spanRows)
                            true
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Unknown categoryDrawWrapper dropped",
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        }
                    } else false
                }

                else -> false
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        categoriesObserver?.let {
            try {
                if (::adapter.isInitialized) adapter.unregisterAdapterDataObserver(it)
            } catch (_: Exception) {
            }
            categoriesObserver = null
        }
    }

    fun populateFields() {
        // Preset interval
    }

    fun validateInterval(text: String): Boolean {
        try {
            Duration.parse(text)
            return true
        } catch (_: Exception) {
        }
        return false
    }

    // Constants used for display
    object StartlistConstants {
        const val STARTLIST_MIN_ROWS = 20
        const val STARTLIST_COLUMNS = 5
        val DEFAULT_INTERVAL = Duration.ofMinutes(5)
        val DEFAULT_CATEGORY_INTERVAL = Duration.ofMinutes(5)

    }
}
