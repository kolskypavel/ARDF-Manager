package kolskypavel.ardfmanager.ui.categories

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.databinding.FragmentCategoriesBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kolskypavel.ardfmanager.ui.event.EventCreateDialogFragment
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()

    private lateinit var categoryToolbar: Toolbar
    private lateinit var categoryAddFab: FloatingActionButton
    private lateinit var categoryRecyclerView: RecyclerView

    private var mLastClickTime: Long = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryToolbar = view.findViewById(R.id.category_toolbar)
        categoryAddFab = view.findViewById(R.id.category_btn_add)
        categoryRecyclerView = view.findViewById(R.id.category_recycler_view)

        categoryToolbar.inflateMenu(R.menu.category_fragment_menu)

        categoryAddFab.setOnClickListener {
            //Prevent accidental double click
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                findNavController().navigate(
                    CategoriesFragmentDirections.modifyCategory(
                        true,
                        -1, null,
                        selectedEventViewModel.event.value!!
                    )
                )
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        selectedEventViewModel.event.observe(viewLifecycleOwner) { event ->
            categoryToolbar.title = event.name
            categoryToolbar.subtitle = dataProcessor.eventTypeToString(event.eventType)
        }
        setFragmentListener()
        setRecyclerViewAdapter()
    }

    private fun setFragmentListener() {
        setFragmentResultListener(CategoryCreateDialogFragment.REQUEST_CATEGORY_MODIFICATION) { _, bundle ->
            val create = bundle.getBoolean(CategoryCreateDialogFragment.BUNDLE_KEY_CREATE)
            val category: Category
            val siCodes: String =
                bundle.getString(CategoryCreateDialogFragment.BUNDLE_KEY_SI_CODES)!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                category = bundle.getSerializable(
                    EventCreateDialogFragment.BUNDLE_KEY_EVENT,
                    Category::class.java
                )!!
            } else {
                category =
                    bundle.getSerializable(CategoryCreateDialogFragment.BUNDLE_KEY_CATEGORY) as Category
            }

            if (create) {
                selectedEventViewModel.createCategory(category, siCodes)
            } else {
                selectedEventViewModel.updateCategory(category, siCodes)
            }
        }
    }

    private fun setRecyclerViewAdapter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedEventViewModel.categories.collect { categories ->
                    categoryRecyclerView.adapter =
                        context?.let {
                            CategoryRecyclerViewAdapter(categories, { action, position, category ->
                                recyclerViewContextMenuActions(
                                    action,
                                    position,
                                    category
                                )
                            }, it)
                        }
                }
            }
        }
    }

    private fun recyclerViewContextMenuActions(action: Int, position: Int, category: Category) {
        when (action) {
            0 -> findNavController().navigate(
                CategoriesFragmentDirections.modifyCategory(
                    false,
                    position,
                    category,
                    selectedEventViewModel.event.value!!
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}