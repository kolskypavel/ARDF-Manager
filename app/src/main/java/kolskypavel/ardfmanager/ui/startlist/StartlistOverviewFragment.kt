package kolskypavel.ardfmanager.ui.startlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kolskypavel.ardfmanager.ui.startlist.adapers.StartlistOverviewAdapter
import kotlinx.coroutines.launch

class StartlistOverviewFragment : Fragment() {

    private val viewModel: SelectedRaceViewModel by activityViewModels()

    private lateinit var categoryPicker: AutoCompleteTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StartlistOverviewAdapter

    private var categories: List<CategoryData> = emptyList()
    private var selectedCategory: CategoryData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_startlist_overview, container, false)

        categoryPicker = view.findViewById(R.id.startlist_overview_category_picker)
        recyclerView = view.findViewById(R.id.startlist_overview_recycler)

        setupRecyclerView()
        observeViewModel()

        return view
    }

    private fun setupRecyclerView() {
        adapter = StartlistOverviewAdapter(mutableListOf()) { reorderedList ->
            saveNewOrder(reorderedList)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                adapter.onDragFinished()
            }
        })
        touchHelper.attachToRecyclerView(recyclerView)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categoryList ->
                    // Filter out empty categories
                    categories = categoryList.filter { it.competitors.isNotEmpty() }
                    updateCategoryPicker()
                }
            }
        }
    }

    private fun updateCategoryPicker() {
        val names = categories.map { it.category.name }
        val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        categoryPicker.setAdapter(autocompleteAdapter)

        categoryPicker.setOnItemClickListener { _, _, position, _ ->
            val selectedName = autocompleteAdapter.getItem(position)
            selectedCategory = categories.find { it.category.name == selectedName }
            displayCategoryCompetitors()
        }

        // Keep current selection if data refreshes
        selectedCategory?.let { current ->
            val updated = categories.find { it.category.id == current.category.id }
            if (updated != null) {
                selectedCategory = updated
                displayCategoryCompetitors()
            } else {
                // If the previously selected category is now empty or deleted, clear selection
                selectedCategory = null
                adapter.updateItems(emptyList())
                categoryPicker.setText("", false)
            }
        }
    }

    private fun displayCategoryCompetitors() {
        // Show competitors sorted by their assigned start time
        val competitors = selectedCategory?.competitors?.sortedBy { it.drawnRelativeStartTime } ?: emptyList()
        adapter.updateItems(competitors)
    }

    private fun saveNewOrder(reorderedList: List<Competitor>) {
        val category = selectedCategory ?: return

        // We re-assign existing time slots to the new competitor order.
        val originalTimes = category.competitors
            .mapNotNull { it.drawnRelativeStartTime }
            .sorted()

        if (originalTimes.size != reorderedList.size) {
            // If the counts don't match (e.g. some competitors don't have times yet), 
            // we skip auto-assignment to avoid data corruption.
            return
        }

        reorderedList.forEachIndexed { index, competitor ->
            competitor.drawnRelativeStartTime = originalTimes[index]
        }
        viewModel.updateCompetitors(reorderedList)
    }
}
