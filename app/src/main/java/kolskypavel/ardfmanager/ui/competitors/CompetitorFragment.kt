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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import de.codecrafters.tableview.SortableTableView
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders
import kolskypavel.ardfmanager.BottomNavDirections
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.CompetitorTableDisplayType
import kolskypavel.ardfmanager.databinding.FragmentCompetitorsBinding
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kolskypavel.ardfmanager.ui.races.RaceCreateDialogFragment
import kotlinx.coroutines.launch


class CompetitorFragment : Fragment() {

    private var _binding: FragmentCompetitorsBinding? = null

    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()
    private lateinit var competitorToolbar: Toolbar
    private lateinit var competitorTableView: SortableTableView<CompetitorData>
    private lateinit var competitorDisplayTypePicker: MaterialAutoCompleteTextView
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

        competitorToolbar = view.findViewById(R.id.competitor_fragment_toolbar)
        competitorAddFab = view.findViewById(R.id.competitor_btn_add)
        competitorTableView = view.findViewById(R.id.competitor_fragment_table_view)
        competitorDisplayTypePicker = view.findViewById(R.id.competitor_fragment_display_type)

        competitorToolbar.inflateMenu(R.menu.fragment_menu_competitor)
        competitorToolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener setFragmentMenuActions(it)
        }

        selectedRaceViewModel.race.observe(viewLifecycleOwner) { race ->
            competitorToolbar.title = race.name
            competitorToolbar.subtitle = dataProcessor.raceTypeToString(race.raceType)
        }

        competitorDisplayTypePicker.setOnItemClickListener { _, _, _, pos ->
            toggleCompetitorDisplay(CompetitorTableDisplayType.getByValue(pos.toInt())!!)
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
        competitorDisplayTypePicker.setText(getText(R.string.competitor_display_overview), false)
        toggleCompetitorDisplay(CompetitorTableDisplayType.OVERVIEW)
        setBackButton()
        setResultListener()
    }

    private fun setFragmentMenuActions(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.competitor_menu_import_file -> {
                findNavController().navigate(
                    CompetitorFragmentDirections.importExportDataCompetitors()
                )
                return true
            }

            R.id.competitor_menu_add_categories_automatically -> {
                confirmAutomaticCategories()
                return true
            }

            R.id.competitor_menu_delete_all_competitors -> {
                confirmAllCompetitorsDeletion()
                return true
            }

            R.id.competitor_menu_edit_race -> {
                findNavController().navigate(
                    BottomNavDirections.modifyRaceProperties(
                        false,
                        0,
                        selectedRaceViewModel.race.value
                    )
                )
                return true
            }

            R.id.competitor_menu_global_settings -> {
                findNavController().navigate(BottomNavDirections.openSettingsFromRace())
                return true
            }
        }
        return false
    }

    private fun setTableHeaders(displayType: CompetitorTableDisplayType) {

        var headers = IntArray(5)
        when (displayType) {
            CompetitorTableDisplayType.OVERVIEW -> {

                headers =
                    intArrayOf(
                        R.string.competitor_start_number_header,
                        R.string.general_name,
                        R.string.club,
                        R.string.category,
                        R.string.si_number
                    )

                //Set comparators
                competitorTableView.setColumnComparator(0, CompetitorStartNumComparator())
                competitorTableView.setColumnComparator(1, CompetitorNameComparator())
                competitorTableView.setColumnComparator(2, CompetitorClubComparator())
                competitorTableView.setColumnComparator(3, CompetitorCategoryComparator())
                competitorTableView.setColumnComparator(4, CompetitorSINumberComparator())

            }

            CompetitorTableDisplayType.START_LIST -> {
                headers =
                    intArrayOf(
                        R.string.competitor_start_number_header,
                        R.string.general_start_time,
                        R.string.general_name,
                        R.string.category,
                        R.string.si_number
                    )
                competitorTableView.setColumnComparator(0, CompetitorStartNumComparator())
                competitorTableView.setColumnComparator(1, CompetitorStartTimeComparator())
                competitorTableView.setColumnComparator(2, CompetitorNameComparator())
                competitorTableView.setColumnComparator(3, CompetitorCategoryComparator())
                competitorTableView.setColumnComparator(4, CompetitorSINumberComparator())
            }

            CompetitorTableDisplayType.FINISH_REACHED -> {
                headers =
                    intArrayOf(
                        R.string.general_name,
                        R.string.category,
                        R.string.run_time,
                        R.string.general_start_time,
                        R.string.finish_time,
                    )

                competitorTableView.setColumnComparator(0, CompetitorNameComparator())
                competitorTableView.setColumnComparator(1, CompetitorCategoryComparator())
                competitorTableView.setColumnComparator(2, CompetitorStartTimeComparator())
                competitorTableView.setColumnComparator(3, CompetitorFinishTimeComparator())
                competitorTableView.setColumnComparator(4, CompetitorRunTimeComparator())
            }

            CompetitorTableDisplayType.ON_THE_WAY -> {
                headers =
                    intArrayOf(
                        R.string.general_name,
                        R.string.category,
                        R.string.general_start_time,
                        R.string.run_time,
                        R.string.competitor_to_limit,
                    )
                for (i in 0..4) {
                    competitorTableView.setColumnComparator(i, null)
                }
            }
        }

        val adapter = SimpleTableHeaderAdapter(
            requireContext(),
            *headers
        )
        adapter.setGravity(Gravity.CENTER)
        adapter.setTextSize(14)

        competitorTableView.headerAdapter = adapter

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

    /**
     * Filter the data based on required display type
     */
    private fun filterCompetitorData(
        data: List<CompetitorData>,
        displayType: CompetitorTableDisplayType
    ): List<CompetitorData> {
        return when (displayType) {
            CompetitorTableDisplayType.OVERVIEW,
            CompetitorTableDisplayType.START_LIST -> data

            CompetitorTableDisplayType.FINISH_REACHED -> {
                data.filter { cd ->
                    cd.resultData != null
                }
            }

            CompetitorTableDisplayType.ON_THE_WAY -> {
                data.filter { cd ->
                    cd.resultData == null
                }
                data.sortedWith(CompetitorStartTimeComparator())
            }
        }
    }

    private fun toggleCompetitorDisplay(displayType: CompetitorTableDisplayType) {
        setTableHeaders(displayType)
        setRecyclerAdapter(displayType)
    }

    private fun setRecyclerAdapter(displayType: CompetitorTableDisplayType) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedRaceViewModel.competitorData.collect { competitorData ->
                    competitorTableView.dataAdapter =
                        CompetitorTableViewAdapter(
                            filterCompetitorData(competitorData, displayType),
                            displayType,
                            requireContext(), selectedRaceViewModel
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
        competitorData: CompetitorData
    ) {
        when (action) {
            0 -> findNavController().navigate(
                CompetitorFragmentDirections.modifyCompetitor(
                    false,
                    competitorData.competitorCategory.competitor,
                    position
                )
            )

            1 -> confirmCompetitorDeletion(competitorData.competitorCategory.competitor)
        }
    }

    private fun confirmCompetitorDeletion(competitor: Competitor) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.competitor_delete))
        val message =
            "${getString(R.string.competitor_delete_confirmation)} ${competitor.firstName} ${competitor.lastName}"
        builder.setMessage(message)

        //TODO: Fix the readout removal
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedRaceViewModel.deleteCompetitor(competitor.id, false)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun confirmAutomaticCategories() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.competitor_add_categories_automatically))
        builder.setMessage(R.string.competitor_add_categories_automatically_confirmation)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedRaceViewModel.addCategoriesAutomatically()
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun confirmAllCompetitorsDeletion() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.competitor_delete_all))
        builder.setMessage(R.string.competitor_delete_all_confirmation)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedRaceViewModel.deleteAllCompetitorsByRace()
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
            builder.setTitle(getString(R.string.race_end))
            val message = getString(R.string.race_end_confirmation)
            builder.setMessage(message)

            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                dataProcessor.removeReaderRace()
                findNavController().navigate(CompetitorFragmentDirections.closeRace())
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

        //Enable race modification from menu
        setFragmentResultListener(RaceCreateDialogFragment.REQUEST_RACE_MODIFICATION) { _, bundle ->
            val race: Race = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    RaceCreateDialogFragment.BUNDLE_KEY_RACE,
                    Race::class.java
                )!!
            } else {
                bundle.getSerializable(RaceCreateDialogFragment.BUNDLE_KEY_RACE) as Race
            }
            selectedRaceViewModel.updateRace(race)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}