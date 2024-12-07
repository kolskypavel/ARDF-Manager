package kolskypavel.ardfmanager.ui.readouts

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.BottomNavDirections
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ResultData
import kolskypavel.ardfmanager.databinding.FragmentResultsBinding
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kolskypavel.ardfmanager.ui.races.RaceCreateDialogFragment
import kolskypavel.ardfmanager.ui.results.ResultsFragmentRecyclerViewAdapter
import kotlinx.coroutines.launch

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()
    private lateinit var resultsToolbar: Toolbar
    private lateinit var resultsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        resultsRecyclerView.adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultsToolbar = view.findViewById(R.id.results_toolbar)
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view)
        resultsToolbar.inflateMenu(R.menu.fragment_menu_result)
        resultsToolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener setFragmentMenuActions(it)
        }

        selectedRaceViewModel.race.observe(viewLifecycleOwner) { race ->
            resultsToolbar.title = race.name
            resultsToolbar.subtitle = dataProcessor.raceTypeToString(race.raceType)
        }

        setResultListener()
        setBackButton()
        setRecyclerViewAdapter()
    }

    private fun setFragmentMenuActions(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.result_menu_share_results -> {
                findNavController().navigate(ResultsFragmentDirections.exportResults())
            }

            R.id.result_menu_edit_race -> {
                findNavController().navigate(
                    BottomNavDirections.modifyRaceProperties(
                        false,
                        0,
                        selectedRaceViewModel.race.value
                    )
                )
                return true
            }

            R.id.result_menu_global_settings -> {
                findNavController().navigate(BottomNavDirections.openSettingsFromRace())
                return true
            }

        }
        return false
    }

    private fun setResultListener() {
        //Enable event modification from menu
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

    private fun setBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.race_end))
            val message = getString(R.string.race_end_confirmation)
            builder.setMessage(message)

            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                dataProcessor.removeReaderRace()
                findNavController().navigate(ResultsFragmentDirections.closeRace())
            }

            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

    }

    private fun openReadoutDetail(resultData: ResultData) {
        findNavController().navigate(ResultsFragmentDirections.openReadoutDetail(resultData))
    }

    private fun setRecyclerViewAdapter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedRaceViewModel.resultData.collect { results ->
                    resultsRecyclerView.adapter =
                        ResultsFragmentRecyclerViewAdapter(
                            ArrayList(results),
                            requireContext(),
                            selectedRaceViewModel
                        ) { cd -> openReadoutDetail(cd) }

                    (resultsRecyclerView.adapter as ResultsFragmentRecyclerViewAdapter).expandAllItems()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}