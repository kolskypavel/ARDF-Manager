package kolskypavel.ardfmanager.ui.races

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nambimobile.widgets.efab.FabOption
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kotlinx.coroutines.launch
import java.util.UUID


/**
 * A fragment representing a list of Items.
 */
class RaceSelectionFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var raceCreateOption: FabOption
    private lateinit var robisImportOption: FabOption
    private lateinit var fileImportOption: FabOption
    private lateinit var recyclerView: RecyclerView
    private var selectedRaceId: UUID? = null
    private var exportData: Boolean = true

    private val raceViewModel: RaceViewModel by activityViewModels()
    private val selectedRaceViewModel: SelectedRaceViewModel by activityViewModels()
    private var raceData: RaceData? = null

    // Race export
    private val getResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val value = it.data
            val uri = value?.data

            if (uri != null) {
                exportImportRaceData(uri)
            }
        }
    }

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
        toolbar.setTitle(R.string.race_toolbar_title)
        toolbar.inflateMenu(R.menu.fragment_menu_race)

        //FAB options
        raceCreateOption = view.findViewById(R.id.race_fab_create)
        robisImportOption = view.findViewById(R.id.race_fab_robis)
        fileImportOption = view.findViewById(R.id.race_fab_file)

        raceCreateOption.setOnClickListener {
            findNavController().navigate(
                RaceSelectionFragmentDirections.raceCreateOfModify(
                    RaceEditDialogFragment.RaceEditActions.CREATE,
                    -1,
                    null
                )
            )
        }

        fileImportOption.setOnClickListener {
            exportData = false
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            getResult.launch(intent)
        }
        robisImportOption.setOnClickListener {
            findNavController().navigate(RaceSelectionFragmentDirections.importRobis())
        }

        setMenuListener()
        setRecyclerAdapter()
        setFragmentListener()
        setBackButton()
    }

    private fun setMenuListener() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.race_menu_global_settings -> {
                    // Navigate to settings screen.
                    findNavController().navigate(RaceSelectionFragmentDirections.openSettings())
                    true
                }

                else -> false
            }
        }
    }

    private fun setBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.general_exit_title))
            val message = getString(R.string.general_exit_confirmation)
            builder.setMessage(message)

            builder.setPositiveButton(R.string.general_ok) { _, _ ->
                requireActivity().finishAffinity();
            }

            builder.setNegativeButton(R.string.general_cancel) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
    }

    private fun setFragmentListener() {
        setFragmentResultListener(RaceEditDialogFragment.REQUEST_RACE_MODIFICATION) { _, bundle ->
            val action =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) bundle.getSerializable(
                    RaceEditDialogFragment.BUNDLE_KEY_ACTIONS,
                    RaceEditDialogFragment.RaceEditActions::class.java
                )
                else {
                    bundle.getSerializable(RaceEditDialogFragment.BUNDLE_KEY_ACTIONS) as RaceEditDialogFragment.RaceEditActions

                }

            val position = bundle.getInt(RaceEditDialogFragment.BUNDLE_KEY_POSITION)

            val race: Race = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    RaceEditDialogFragment.BUNDLE_KEY_RACE,
                    Race::class.java
                )!!
            } else {
                bundle.getSerializable(RaceEditDialogFragment.BUNDLE_KEY_RACE) as Race
            }

            //create new race
            when (action) {
                RaceEditDialogFragment.RaceEditActions.CREATE -> {
                    raceViewModel.createRace(race)
                }
                //Edit an existing race
                RaceEditDialogFragment.RaceEditActions.EDIT -> {
                    raceViewModel.updateRace(race)
                    recyclerView.adapter?.notifyItemChanged(position)
                }

                else -> {
                    raceData?.let { raceData ->
                        raceData.race = race
                        raceViewModel.saveRaceData(raceData)
                    }
                }
            }
        }
    }

    private fun recyclerViewContextMenuActions(action: Int, position: Int, race: Race) {
        when (action) {
            0 -> findNavController().navigate(
                RaceSelectionFragmentDirections.raceCreateOfModify(
                    RaceEditDialogFragment.RaceEditActions.EDIT, position, race
                )
            )

            1 -> exportRace(race.id)
            2 -> confirmRaceDeletion(race)
        }
    }

    private fun displayAlert(message: String) {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setTitle(getString(R.string.race_import_failure))
        alertDialog.setMessage(message)
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.general_ok)
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    private fun exportImportRaceData(uri: Uri) {
        if (exportData && selectedRaceId != null) {
            raceViewModel.exportRaceData(uri, selectedRaceId!!)

            // Inform user about successful export
            Toast.makeText(
                requireContext(),
                requireContext().getText(R.string.race_export_success),
                Toast.LENGTH_SHORT
            ).show()

        } else {
            try {
                raceData = raceViewModel.importRaceData(uri)
                findNavController().navigate(
                    RaceSelectionFragmentDirections.raceCreateOfModify(
                        RaceEditDialogFragment.RaceEditActions.IMPORT, -1, raceData!!.race
                    )
                )

            } catch (e: Exception) {
                displayAlert(e.message.toString())
            }
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

        builder.setPositiveButton(R.string.general_ok) { dialog, _ ->
            raceViewModel.deleteRace(race.id)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.general_cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun exportRace(raceId: UUID) {
        selectedRaceId = raceId
        exportData = true

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/json"
        intent.putExtra(Intent.EXTRA_TITLE, "race.ardfjs")
        getResult.launch(intent)
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
