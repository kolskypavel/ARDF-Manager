package kolskypavel.ardfmanager.ui.readouts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.wrappers.ResultDataWrapper
import kolskypavel.ardfmanager.databinding.FragmentReadoutsBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kotlinx.coroutines.launch

class ReadoutFragment : Fragment() {

    private var _binding: FragmentReadoutsBinding? = null
    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()

    private lateinit var readoutToolbar: Toolbar
    private lateinit var readoutRecyclerView: RecyclerView

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

        readoutToolbar.inflateMenu(R.menu.fragment_menu_readouts)

        selectedEventViewModel.event.observe(viewLifecycleOwner) { event ->
            readoutToolbar.title = event.name
            readoutToolbar.subtitle = dataProcessor.eventTypeToString(event.eventType)
        }
        setRecyclerAdapter()
        setBackButton()
    }

    private fun recyclerViewContextMenuActions(
        action: Int,
        position: Int,
        readoutData: ResultDataWrapper
    ) {
        when (action) {
            0 -> {}

            1 -> {
                confirmReadoutDeletion(readoutData)
            }
        }
    }

    private fun confirmReadoutDeletion(readoutData: ResultDataWrapper) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.readout_delete_readout))
        val message =
            getString(R.string.readout_delete_readout_confirmation, readoutData.result!!.siNumber)
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            selectedEventViewModel.deleteReadout(readoutData.result!!.id)
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

    private fun openReadoutDetail(readoutData: ResultDataWrapper) {
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