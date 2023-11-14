package kolskypavel.ardfmanager.ui.competitors

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.databinding.FragmentCompetitorsBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel

class CompetitorsFragment : Fragment() {

    private var _binding: FragmentCompetitorsBinding? = null

    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()
    private lateinit var competitorToolbar: Toolbar
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
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        competitorToolbar = view.findViewById(R.id.competitor_toolbar)
        competitorAddFab = view.findViewById(R.id.competitor_btn_add)

        competitorToolbar.inflateMenu(R.menu.competitor_nav_menu)

        selectedEventViewModel.event.observe(viewLifecycleOwner) { event ->
            competitorToolbar.title = event.name
            competitorToolbar.subtitle = dataProcessor.eventTypeToString(event.eventType)
        }

        competitorAddFab.setOnClickListener {
            //Prevent accidental double click
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                findNavController().navigate(
                    CompetitorsFragmentDirections.modifyCompetitor(true, null)
                )
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}