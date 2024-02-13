package kolskypavel.ardfmanager.ui.competitors

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.codecrafters.tableview.SortableTableView
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders
import kolskypavel.ardfmanager.BottomNavDirections
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.comparators.CompetitorCategoryComparator
import kolskypavel.ardfmanager.backend.comparators.CompetitorClubComparator
import kolskypavel.ardfmanager.backend.comparators.CompetitorFirstNameComparator
import kolskypavel.ardfmanager.backend.comparators.CompetitorLastNameComparator
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.databinding.FragmentCompetitorsBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kolskypavel.ardfmanager.ui.event.EventCreateDialogFragment
import kotlinx.coroutines.launch


class CompetitorFragment : Fragment() {

    private var _binding: FragmentCompetitorsBinding? = null

    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()
    private lateinit var competitorToolbar: Toolbar
    private lateinit var competitorTableView: SortableTableView<CompetitorCategory>
    private lateinit var competitorAddFab: FloatingActionButton
    private var mLastClickTime: Long = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCompetitorsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        competitorToolbar = view.findViewById(R.id.competitor_toolbar)
        competitorAddFab = view.findViewById(R.id.competitor_btn_add)
        competitorTableView = view.findViewById(R.id.competitor_table_view)

        competitorToolbar.inflateMenu(R.menu.fragment_menu_competitor)
        competitorToolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener setFragmentMenuActions(it)
        }

        selectedEventViewModel.event.observe(viewLifecycleOwner) { event ->
            competitorToolbar.title = event.name
            competitorToolbar.subtitle = dataProcessor.eventTypeToString(event.eventType)
        }

        competitorAddFab.setOnClickListener {
            //Prevent accidental double click
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                findNavController().navigate(
                    CompetitorFragmentDirections.modifyCompetitor(
                        true,
                        null, -1
                    )
                )
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }
        setTableHeaders()
        setRecyclerAdapter()
        setBackButton()
        setResultListener()
    }

    private fun setFragmentMenuActions(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.competitor_menu_import_file -> {
                return true
            }

            R.id.competitor_menu_edit_event -> {
                findNavController().navigate(
                    BottomNavDirections.modifyEventProperties(
                        false,
                        0,
                        selectedEventViewModel.event.value
                    )
                )
                return true
            }

            R.id.competitor_menu_global_settings -> {
                findNavController().navigate(BottomNavDirections.openSettingsFromEvent())
                return true
            }

            R.id.competitor_menu_about_app -> {
                return true
            }
        }
        return false
    }

    private fun setTableHeaders() {
        val headers =
            intArrayOf(
                R.string.competitor_first_name,
                R.string.last_name,
                R.string.club,
                R.string.category
            )

        val adapter = SimpleTableHeaderAdapter(
            requireContext(),
            *headers
        )
        adapter.setGravity(Gravity.CENTER)
        adapter.setTextSize(14)

        competitorTableView.headerAdapter = adapter
        //Set comparators
        competitorTableView.setColumnComparator(0, CompetitorFirstNameComparator())
        competitorTableView.setColumnComparator(1, CompetitorLastNameComparator())
        competitorTableView.setColumnComparator(2, CompetitorClubComparator())
        competitorTableView.setColumnComparator(3, CompetitorCategoryComparator())


        val colorEvenRows =
            requireContext().resources.getColor(R.color.white, null)
        val colorOddRows =
            requireContext().resources.getColor(R.color.light_grey, null)

        competitorTableView.setDataRowBackgroundProvider(
            TableDataRowBackgroundProviders.alternatingRowColors(
                colorEvenRows,
                colorOddRows
            )
        )
    }

    private fun setRecyclerAdapter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedEventViewModel.competitorsCategories.collect { competitorCategories ->
                    competitorTableView.dataAdapter =
                        CompetitorTableViewAdapter(
                            competitorCategories,
                            requireContext()
                        ) { action, position, competitor ->
                            tableViewContextMenuActions(
                                action,
                                position,
                                competitor
                            )
                        }
                }
            }
        }
    }

    private fun tableViewContextMenuActions(
        action: Int,
        position: Int,
        competitorCategory: CompetitorCategory
    ) {
        when (action) {
            0 -> findNavController().navigate(
                CompetitorFragmentDirections.modifyCompetitor(
                    false,
                    competitorCategory.competitor,
                    position
                )
            )

            1 -> {}
            2 -> confirmCompetitorDeletion(competitorCategory.competitor)
        }
    }

    private fun confirmCompetitorDeletion(competitor: Competitor) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.competitor_delete))
        val message =
            "${getString(R.string.competitor_delete_confirmation)} ${competitor.firstName}  ${competitor.lastName}"
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedEventViewModel.deleteCompetitor(competitor.id)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun setBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.event_end))
            val message = getString(R.string.event_end_confirmation)
            builder.setMessage(message)

            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                dataProcessor.removeReaderEvent()
                findNavController().navigate(CompetitorFragmentDirections.closeEvent())
            }

            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
    }

    private fun setResultListener() {
        setFragmentResultListener(CompetitorCreateDialogFragment.REQUEST_COMPETITOR_MODIFICATION) { _, bundle ->
            val create = bundle.getBoolean(CompetitorCreateDialogFragment.BUNDLE_KEY_CREATE)

            if (!create) {
                competitorTableView.dataAdapter.notifyDataSetChanged()
            }
        }

        //Enable event modification from menu
        setFragmentResultListener(EventCreateDialogFragment.REQUEST_EVENT_MODIFICATION) { _, bundle ->
            val event: Event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    EventCreateDialogFragment.BUNDLE_KEY_EVENT,
                    Event::class.java
                )!!
            } else {
                bundle.getSerializable(EventCreateDialogFragment.BUNDLE_KEY_EVENT) as Event
            }
            selectedEventViewModel.updateEvent(event)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}