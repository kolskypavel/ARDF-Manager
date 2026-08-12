package kolskypavel.ardfmanager.ui.startlist

import android.content.ClipData
import android.os.Bundle
import android.util.TypedValue
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.draw.StartlistProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kolskypavel.ardfmanager.ui.startlist.adapers.CategoryDrawWrapperAdapter
import kolskypavel.ardfmanager.ui.startlist.adapers.CellModel
import kolskypavel.ardfmanager.ui.startlist.adapers.GridAdapter
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kotlinx.coroutines.launch
import java.time.Duration

class StartlistDrawFragment : Fragment() {

    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()

    private lateinit var separateClubsCheckBox: CheckBox
    private lateinit var categoryRecycler: RecyclerView
    private lateinit var adapter: CategoryDrawWrapperAdapter
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var drawButton: FloatingActionButton
    private lateinit var intervalInput: TextInputEditText
    private lateinit var categoryIntervalInput: TextInputEditText
    private lateinit var intervalSetButton: Button
    private lateinit var intervalLayout: TextInputLayout
    private lateinit var categoryIntervalLayout: TextInputLayout

    private lateinit var gridRecycler: RecyclerView
    private lateinit var gridAdapter: GridAdapter

    private var categoriesObserver: RecyclerView.AdapterDataObserver? = null

    private var curInterval: Duration = StartlistConstants.DEFAULT_INTERVAL
    private var curCategoryInterval: Duration = StartlistConstants.DEFAULT_CATEGORY_INTERVAL
    private var currentRows: Int = StartlistConstants.STARTLIST_MIN_ROWS

    private val categoryColors = listOf(
        0xFFBBDEFB.toInt(), // Light Blue
        0xFFF8BBD0.toInt(), // Pink
        0xFFC8E6C9.toInt(), // Light Green
        0xFFFFF9C4.toInt(), // Light Yellow
        0xFFD1C4E9.toInt(), // Purple
        0xFFFFE0B2.toInt(), // Orange
        0xFFF5F5F5.toInt(), // Grey
        0xFFE1F5FE.toInt(), // Light Cyan
        0xFFFFCCBC.toInt(), // Deep Orange
        0xFFD7CCC8.toInt(), // Brown
        0xFFF0F4C3.toInt(), // Lime
        0xFFE0F2F1.toInt()  // Teal
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_startlist_draw, container, false)

        initViews(view)
        setupCategoryRecycler()
        setupGridRecycler(view)
        populateFields()

        drawButton.setOnClickListener { onDrawClicked() }
        intervalSetButton.setOnClickListener { onIntervalsChanged()}

        return view
    }

    private fun initViews(view: View) {
        separateClubsCheckBox = view.findViewById(R.id.startlist_separate_same_club_checkbox)
        intervalInput = view.findViewById(R.id.startlist_interval_input)
        categoryIntervalInput = view.findViewById(R.id.startlist_category_interval_input)
        intervalLayout = view.findViewById(R.id.startlist_interval_layout)
        categoryIntervalLayout = view.findViewById(R.id.startlist_category_interval_layout)
        categoryRecycler = view.findViewById(R.id.startlist_categories_recycler)
        intervalSetButton = view.findViewById(R.id.startlist_set_interval_btn)
        drawButton = view.findViewById(R.id.startlist_btn_draw)
    }

    private fun setupCategoryRecycler() {
        categoryRecycler.layoutManager =
            GridLayoutManager(requireContext(), StartlistConstants.CATEGORY_COLUMNS)

        adapter = CategoryDrawWrapperAdapter(mutableListOf()) { v, cat ->
            val clip = ClipData.newPlainText("categoryId", cat.getCategoryId().toString())
            v.startDragAndDrop(clip, View.DragShadowBuilder(v), cat, 0)
        }

        categoryRecycler.adapter = adapter

        val minTopListHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 64f, resources.displayMetrics
        ).toInt()

        categoriesObserver = object : RecyclerView.AdapterDataObserver() {
            private fun update() {
                categoryRecycler.minimumHeight =
                    if (adapter.itemCount == 0) minTopListHeightPx else 0
            }

            override fun onChanged() = update()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = update()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = update()
        }
        adapter.registerAdapterDataObserver(categoriesObserver!!)
        if (adapter.itemCount == 0) categoryRecycler.minimumHeight = minTopListHeightPx

        val callback = CategoryTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(categoryRecycler)

        adapter.itemDragStart = { holder -> touchHelper.startDrag(holder) }
        categoryRecycler.setOnDragListener { _, event -> handleCategoryListDrag(event) }

        // Observe categories and restore placements
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedRaceViewModel.categories.collect { categories ->
                    // Only perform initial placement if we haven't loaded anything yet
                    if (adapter.isEmpty() && gridAdapter.getPlacements().isEmpty()) {
                        val activeCategories = categories.filter { it.competitors.isNotEmpty() }
                        if (activeCategories.isEmpty()) return@collect

                        val totalColumns = StartlistConstants.STARTLIST_COLUMNS + 1

                        // Recalculate and update grid size before placing categories
                        val requiredRows = calculateRequiredRows(activeCategories)
                        if (requiredRows > currentRows) {
                            currentRows = requiredRows
                            gridAdapter.updateCells(
                                createGridCells(
                                    currentRows,
                                    totalColumns,
                                    curInterval
                                )
                            )
                        }

                        activeCategories.forEachIndexed { index, categoryData ->
                            val cat = categoryData.category
                            val color = cat.color ?: categoryColors[index % categoryColors.size]
                            val wrapper = CategoryDrawWrapper(categoryData, color)

                            val startTime = cat.startListStartTime
                            val column = cat.startListColumn

                            if (startTime != null && column != null && column > 0 && column < totalColumns) {
                                val row = (startTime.toMillis() / curInterval.toMillis()).toInt()
                                val startIndex = row * totalColumns + column
                                val spanRows = calculateSpanRows(wrapper)

                                if (gridAdapter.canPlaceAt(startIndex, spanRows)) {
                                    gridAdapter.setCellSpan(startIndex, wrapper, spanRows)
                                } else {
                                    adapter.addItem(wrapper)
                                }
                            } else {
                                adapter.addItem(wrapper)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupGridRecycler(view: View) {
        gridRecycler = view.findViewById(R.id.startlist_grid_recycler)
        val totalColumns = StartlistConstants.STARTLIST_COLUMNS + 1

        // Adjust initial rows based on currently available categories (if any)
        val cats = selectedRaceViewModel.categories.value
        if (cats.isNotEmpty()) {
            currentRows = calculateRequiredRows(cats)
        }

        val cellHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 32f, resources.displayMetrics
        ).toInt()

        val initialCells = createGridCells(currentRows, totalColumns, curInterval)
        gridAdapter = GridAdapter(totalColumns, initialCells, cellHeightPx)
        gridRecycler.layoutManager = GridLayoutManager(requireContext(), totalColumns)
        gridRecycler.adapter = gridAdapter

        val dividerColor = resources.getColor(android.R.color.darker_gray, requireContext().theme)
        gridRecycler.addItemDecoration(
            VerticalDividerItemDecoration(
                requireContext(),
                dividerColor,
                2f,
                totalColumns,
                cellHeightPx
            )
        )
        gridRecycler.isNestedScrollingEnabled = false
        gridRecycler.setOnDragListener { v, event ->
            handleGridDrag(v, event, totalColumns, cellHeightPx)
        }
    }

    private fun onIntervalsChanged() {
        val intervalStr = intervalInput.text?.toString() ?: ""
        val catIntervalStr = categoryIntervalInput.text?.toString() ?: ""

        val interval = parseDuration(intervalStr)
        val catInterval = parseDuration(catIntervalStr)

        intervalLayout.error = if (intervalStr.isNotEmpty() && interval == null)
            getString(R.string.general_invalid) else null
        categoryIntervalLayout.error = if (catIntervalStr.isNotEmpty() && catInterval == null)
            getString(R.string.general_invalid) else null

        if (interval != null && catInterval != null && !interval.isZero && !catInterval.isZero) {
            if (catInterval.toMillis() % interval.toMillis() != 0L) {
                categoryIntervalLayout.error =
                    getString(R.string.startlist_invalid_category_interval)
            }

            curInterval = interval
            curCategoryInterval = catInterval
            refreshGrid()
        }
    }

    private fun parseDuration(input: String): Duration? {
        return try {
            TimeProcessor.minuteStringToDuration(input.trim())
        } catch (_: Exception) {
            null
        }
    }

    private fun refreshGrid() {
        val placements = gridAdapter.getPlacements()
        val totalColumns = StartlistConstants.STARTLIST_COLUMNS + 1

        val allCategories =
            adapter.getItems().map { it.catData } + placements.map { it.category.catData }
        currentRows = calculateRequiredRows(allCategories)

        val newCells = createGridCells(currentRows, totalColumns, curInterval)
        gridAdapter.updateCells(newCells)

        for (p in placements) {
            val spanRows = calculateSpanRows(p.category)
            if (gridAdapter.canPlaceAt(p.startIndex, spanRows)) {
                gridAdapter.setCellSpan(p.startIndex, p.category, spanRows)
            } else {
                gridAdapter.removeSpan(p.startIndex)
                if (!adapter.getItems().any { it.getCategoryId() == p.category.getCategoryId() }) {
                    adapter.addItem(p.category)
                    saveCategoryPosition(p.category, null, null)
                }
            }
        }
    }

    private fun createGridCells(
        rows: Int,
        columns: Int,
        interval: Duration
    ): MutableList<CellModel> {
        val cellsCount = rows * columns
        val cells = MutableList(cellsCount) { CellModel(null, null, false, null) }
        var acc = Duration.ZERO
        for (r in 0 until rows) {
            val timeLabel = TimeProcessor.durationToFormattedString(acc, true)
            val idx = r * columns
            if (idx in cells.indices) cells[idx].timeText = timeLabel
            acc += interval
        }
        return cells
    }

    private fun calculateRequiredRows(categories: List<CategoryData>): Int {
        val activeCategories = categories.filter { it.competitors.isNotEmpty() }
        val estimatedRows = activeCategories.sumOf { calculateSpanRows(it.competitors.size) }

        var maxSavedRow = 0
        activeCategories.forEach { catData ->
            val startTime = catData.category.startListStartTime
            if (startTime != null) {
                val row = (startTime.toMillis() / curInterval.toMillis()).toInt()
                val span = calculateSpanRows(catData.competitors.size)
                maxSavedRow = maxOf(maxSavedRow, row + span)
            }
        }
        return maxOf(StartlistConstants.STARTLIST_MIN_ROWS, maxSavedRow + 10, estimatedRows + 20)
    }

    private fun calculateSpanRows(competitorCount: Int): Int {
        val ratio = curCategoryInterval.toMillis().toDouble() / curInterval.toMillis().toDouble()
        return (competitorCount * ratio).toInt().coerceAtLeast(1)
    }

    private fun calculateSpanRows(category: CategoryDrawWrapper): Int {
        return calculateSpanRows(category.getCompetitorCount())
    }

    private fun onDrawClicked() {
        val placements = gridAdapter.getPlacements()
        val anyHasStartTime = placements.any { p ->
            p.category.catData.competitors.any { it.drawnRelativeStartTime != null }
        }

        if (!adapter.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.startlist_confirm_partial_draw_title)
                .setMessage(R.string.startlist_confirm_partial_draw_text)
                .setPositiveButton(R.string.general_ok) { _, _ ->
                    if (anyHasStartTime) showOverwriteConfirmation() else performDraw()
                }
                .setNegativeButton(R.string.general_cancel, null)
                .show()
        } else if (anyHasStartTime) {
            showOverwriteConfirmation()
        } else {
            performDraw()
        }
    }

    private fun showOverwriteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.startlist_confirm_overwrite_title)
            .setMessage(R.string.startlist_confirm_overwrite_text)
            .setPositiveButton(R.string.general_ok) { _, _ -> performDraw() }
            .setNegativeButton(R.string.general_cancel, null)
            .show()
    }

    // Performs the actual draw
    private fun performDraw() {
        val totalColumns = StartlistConstants.STARTLIST_COLUMNS + 1
        val placements = gridAdapter.getPlacements()

        for (p in placements) {
            val row = p.startIndex / totalColumns
            p.category.startPoint = curInterval.multipliedBy(row.toLong())
        }

        StartlistProcessor.drawStartTimes(
            placements.map { it.category },
            curCategoryInterval,
            separateClubsCheckBox.isChecked
        )

        // Collect all competitors from all categories and save them
        val competitorsToSave = placements.flatMap { it.category.catData.competitors }
        selectedRaceViewModel.updateCompetitors(competitorsToSave)

        Toast.makeText(requireContext(), R.string.startlist_draw_completed, Toast.LENGTH_SHORT)
            .show()
    }

    private fun handleCategoryListDrag(event: DragEvent): Boolean {
        if (event.action == DragEvent.ACTION_DROP) {
            val local = event.localState
            if (local is Pair<*, *>) {
                val cat = local.first as? CategoryDrawWrapper
                val startIndex = local.second as? Int
                if (cat != null) {
                    if (adapter.getItems().none { it.getCategoryId() == cat.getCategoryId() }) {
                        adapter.addItem(cat)
                    }
                    if (startIndex != null) gridAdapter.removeSpan(startIndex)
                    saveCategoryPosition(cat, null, null)
                    return true
                }
            }
        }
        return event.action == DragEvent.ACTION_DRAG_STARTED
    }

    private fun handleGridDrag(
        v: View,
        event: DragEvent,
        totalColumns: Int,
        cellHeightPx: Int
    ): Boolean {
        if (event.action == DragEvent.ACTION_DROP) {
            val local = event.localState
            var category: CategoryDrawWrapper? = null
            var originStartIndex: Int? = null

            if (local is Pair<*, *>) {
                category = local.first as? CategoryDrawWrapper
                originStartIndex = local.second as? Int
            } else if (local is CategoryDrawWrapper) {
                category = local
            }

            category?.let { cat ->
                val rx = event.x.toInt()
                val ry = event.y.toInt()
                val col = (rx / (v.width / totalColumns)).coerceIn(0, totalColumns - 1)
                val row = (ry / cellHeightPx).coerceIn(0, currentRows - 1)
                val index = row * totalColumns + col

                if (col == 0) {
                    Toast.makeText(
                        requireContext(),
                        "Cannot place on time column",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }

                val spanRows = calculateSpanRows(cat)
                if (gridAdapter.canPlaceAt(index, spanRows, originStartIndex)) {
                    if (local is CategoryDrawWrapper) adapter.removeById(cat.getCategoryId())
                    if (originStartIndex != null && originStartIndex != index) gridAdapter.removeSpan(
                        originStartIndex
                    )

                    gridAdapter.setCellSpan(index, cat, spanRows)

                    val startTime = curInterval.multipliedBy(row.toLong())
                    saveCategoryPosition(cat, col, startTime)
                    return true
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.startlist_space_occupied,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        return event.action == DragEvent.ACTION_DRAG_STARTED || event.action == DragEvent.ACTION_DRAG_LOCATION
    }

    private fun saveCategoryPosition(
        wrapper: CategoryDrawWrapper,
        column: Int?,
        startTime: Duration?
    ) {
        val cat = wrapper.catData.category
        cat.startListColumn = column
        cat.startListStartTime = startTime
        cat.color = wrapper.color
        selectedRaceViewModel.createOrUpdateCategory(cat, null, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        categoriesObserver?.let {
            if (::adapter.isInitialized) adapter.unregisterAdapterDataObserver(it)
            categoriesObserver = null
        }
    }

    private fun populateFields() {
        intervalInput.setText(TimeProcessor.durationToFormattedString(curInterval, true))

        categoryIntervalInput.setText(
            TimeProcessor.durationToFormattedString(
                curCategoryInterval,
                true
            )
        )
    }

    object StartlistConstants {
        const val STARTLIST_MIN_ROWS = 30
        const val CATEGORY_COLUMNS = 5
        const val STARTLIST_COLUMNS = 5
        val DEFAULT_INTERVAL: Duration = Duration.ofMinutes(5)
        val DEFAULT_CATEGORY_INTERVAL: Duration = Duration.ofMinutes(5)
    }
}
