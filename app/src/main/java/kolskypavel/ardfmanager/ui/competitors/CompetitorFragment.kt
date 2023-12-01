package kolskypavel.ardfmanager.ui.competitors

import android.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.databinding.FragmentCompetitorsBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kotlinx.coroutines.launch

class CompetitorFragment : Fragment() {

    private var _binding: FragmentCompetitorsBinding? = null

    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()
    private lateinit var competitorToolbar: Toolbar
    private lateinit var competitorRecyclerView: RecyclerView
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
        competitorRecyclerView = view.findViewById(R.id.competitor_recycler_view)

        competitorToolbar.inflateMenu(R.menu.competitor_nav_menu)

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
        setRecyclerAdapter()
        setBackButton()
    }

    private fun setRecyclerAdapter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedEventViewModel.competitors.collect { competitors ->
                    competitorRecyclerView.adapter =
                        CompetitorRecyclerViewAdapter(competitors, requireContext())
                }
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}