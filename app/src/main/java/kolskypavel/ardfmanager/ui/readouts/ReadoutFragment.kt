package kolskypavel.ardfmanager.ui.readouts

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kolskypavel.ardfmanager.BottomNavDirections
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.databinding.FragmentReadoutsBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kolskypavel.ardfmanager.ui.event.EventCreateDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReadoutFragment : Fragment() {

    private var _binding: FragmentReadoutsBinding? = null
    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()

    private lateinit var readoutToolbar: Toolbar
    private lateinit var startedTextView: TextView
    private lateinit var limitTextView: TextView
    private lateinit var finishedTextView: TextView
    private lateinit var startedProgressBar: ProgressBar
    private lateinit var limitProgressBar: ProgressBar
    private lateinit var finishedProgressBar: ProgressBar
    private lateinit var readoutRecyclerView: RecyclerView
    private lateinit var readoutAddFab: FloatingActionButton


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReadoutsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        readoutToolbar = view.findViewById(R.id.readouts_toolbar)
        readoutRecyclerView = view.findViewById(R.id.readout_recycler_view)

        startedTextView = view.findViewById(R.id.readouts_started_text)
        finishedTextView = view.findViewById(R.id.readouts_finished_text)
        limitTextView = view.findViewById(R.id.readouts_limit_text)
        startedProgressBar = view.findViewById(R.id.readouts_started_progress_bar)
        finishedProgressBar = view.findViewById(R.id.readouts_finished_progress_bar)
        limitProgressBar = view.findViewById(R.id.readouts_limit_progress_bar)
        readoutAddFab = view.findViewById(R.id.readout_btn_add)

        readoutToolbar.inflateMenu(R.menu.fragment_menu_readout)
        readoutToolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener setFragmentMenuActions(it)
        }

        selectedEventViewModel.event.observe(viewLifecycleOwner) { event ->
            readoutToolbar.title = event.name
            readoutToolbar.subtitle = dataProcessor.eventTypeToString(event.eventType)
        }

        readoutAddFab.setOnClickListener {
            findNavController().navigate(
                ReadoutFragmentDirections.editOrCreateReadout(
                    true,
                    null,
                    -1
                )
            )
        }

        setResultListener()
        setRecyclerAdapter()
        setBackButton()
        setStatusLayout()
    }

    private fun setFragmentMenuActions(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {

            R.id.readout_menu_edit_event -> {
                findNavController().navigate(
                    BottomNavDirections.modifyEventProperties(
                        false,
                        0,
                        selectedEventViewModel.event.value
                    )
                )
                return true
            }

            R.id.readout_menu_global_settings -> {
                findNavController().navigate(BottomNavDirections.openSettingsFromEvent())
                return true
            }

        }
        return false
    }

    private fun setResultListener() {
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

    private fun recyclerViewContextMenuActions(
        action: Int,
        position: Int,
        readoutData: ReadoutData
    ) {
        when (action) {
            0 -> {
                if (readoutData.competitorCategory != null) {
                    findNavController().navigate(
                        ReadoutFragmentDirections.editOrCreateReadout(
                            false, readoutData, position
                        )
                    )
                }
            }

            1 -> {
                confirmReadoutDeletion(readoutData)
            }
        }
    }

    private fun setStatusLayout() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val statistics =
                    selectedEventViewModel.getStatistics(selectedEventViewModel.getCurrentEvent().id)

                startedTextView.text = "${statistics.startedCompetitors}/${statistics.competitors}"
                startedProgressBar.progress = if (statistics.startedCompetitors != 0) {
                    ((statistics.startedCompetitors / statistics.competitors.toDouble()) * 100).toInt()
                } else {
                    0
                }

                finishedTextView.text =
                    "${statistics.finishedCompetitors}/${statistics.competitors}"
                finishedProgressBar.progress = if (statistics.finishedCompetitors != 0) {
                    ((statistics.finishedCompetitors / statistics.competitors.toDouble()) * 100).toInt()
                } else {
                    0
                }

                limitTextView.text =
                    "${statistics.inLimitCompetitors}/${statistics.startedCompetitors - statistics.finishedCompetitors}"
                limitProgressBar.progress =
                    if (statistics.startedCompetitors - statistics.finishedCompetitors != 0) {
                        ((statistics.inLimitCompetitors / (statistics.startedCompetitors - statistics.finishedCompetitors).toDouble()) * 100).toInt()
                    } else {
                        100
                    }
                delay(3000)
            }
        }
    }

    private fun confirmReadoutDeletion(readoutData: ReadoutData) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.readout_delete_readout))
        val message =
            getString(
                R.string.readout_delete_readout_confirmation,
                readoutData.readoutResult.readout!!.siNumber
            )
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedEventViewModel.deleteReadout(readoutData.readoutResult.readout.id)
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
                selectedEventViewModel.readoutData.collect { readouts ->
                    readoutRecyclerView.adapter =
                        ReadoutDataRecyclerViewAdapter(
                            readouts,
                            requireContext(),
                            { readoutData -> openReadoutDetail(readoutData) },
                            { action, position, readoutData ->
                                recyclerViewContextMenuActions(
                                    action,
                                    position,
                                    readoutData
                                )
                            })
                }
            }
        }
    }

    private fun openReadoutDetail(readoutData: ReadoutData) {
        findNavController().navigate(ReadoutFragmentDirections.openReadoutDetail(readoutData))
    }

    private fun setBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.event_end))
            val message = getString(R.string.event_end_confirmation)
            builder.setMessage(message)

            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                dataProcessor.removeReaderEvent()
                findNavController().navigate(ReadoutFragmentDirections.closeEvent())
            }

            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}