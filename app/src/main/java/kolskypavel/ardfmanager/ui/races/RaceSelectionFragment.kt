package kolskypavel.ardfmanager.ui.races

import android.app.AlertDialog
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
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class RaceSelectionFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var raceAddFAB: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private var mLastClickTime: Long = 0

    private val raceViewModel: RaceViewModel by activityViewModels()
    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_race_selection, container, false)
        recyclerView = view.findViewById(R.id.race_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.race_toolbar)
        raceAddFAB = view.findViewById(R.id.race_btn_add)

        toolbar.setTitle(R.string.race_toolbar_title)
        toolbar.inflateMenu(R.menu.fragment_menu_race)

        raceAddFAB.setOnClickListener {

            //Prevent accidental double click
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                findNavController().navigate(
                    RaceSelectionFragmentDirections.raceCreateOfModify(true, -1, null)
                )
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        setMenuListener()
        setRecyclerAdapter()
        setFragmentListener()
    }

    private fun setMenuListener() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.race_menu_import_file -> {
                    true
                }

                R.id.race_menu_global_settings -> {
                    // Navigate to settings screen.
                    findNavController().navigate(RaceSelectionFragmentDirections.openSettings())
                    true
                }


                else -> false
            }
        }
    }

    private fun setFragmentListener() {
        setFragmentResultListener(RaceCreateDialogFragment.REQUEST_RACE_MODIFICATION) { _, bundle ->
            val create = bundle.getBoolean(RaceCreateDialogFragment.BUNDLE_KEY_CREATE)
            val position = bundle.getInt(RaceCreateDialogFragment.BUNDLE_KEY_POSITION)

            val race: Race = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    RaceCreateDialogFragment.BUNDLE_KEY_RACE,
                    Race::class.java
                )!!
            } else {
                bundle.getSerializable(RaceCreateDialogFragment.BUNDLE_KEY_RACE) as Race
            }

            //create new race
            if (create) {
                raceViewModel.createRace(race)
            }
            //Edit an existing race
            else {
                raceViewModel.updateRace(race)
                recyclerView.adapter?.notifyItemChanged(position)
            }
        }
    }

    private fun recyclerViewContextMenuActions(action: Int, position: Int, race: Race) {
        when (action) {
            0 -> findNavController().navigate(
                RaceSelectionFragmentDirections.raceCreateOfModify(
                    false, position, race
                )
            )

            1 -> confirmRaceDeletion(race)
        }
    }

    /**
     * Displays alert dialog to confirm the deletion of the race
     */
    private fun confirmRaceDeletion(race: Race) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.race_delete))
        val message = getString(R.string.race_delete_confirmation) + " " + race.name
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            raceViewModel.deleteRace(race.id)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun setRecyclerAdapter() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                raceViewModel.races.collect { races ->
                    recyclerView.adapter =
                        RaceRecyclerViewAdapter(
                            races, { raceId ->

                                // Pass the race id into view Model
                                selectedRaceViewModel.setRace(raceId)

                                findNavController().navigate(
                                    RaceSelectionFragmentDirections.openRace()
                                )
                            },
                            //Context menu action setup
                            { action, position, race ->
                                recyclerViewContextMenuActions(
                                    action,
                                    position,
                                    race
                                )
                            }, requireContext()
                        )
                }
            }
        }
    }
}
