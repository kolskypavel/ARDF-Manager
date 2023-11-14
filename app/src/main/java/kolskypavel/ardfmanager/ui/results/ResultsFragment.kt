package kolskypavel.ardfmanager.ui.readouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.databinding.FragmentResultsBinding
import kolskypavel.ardfmanager.ui.SelectedEventViewModel

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()
    private val dataProcessor = DataProcessor.get()
    private lateinit var resultsToolbar: Toolbar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultsToolbar = view.findViewById(R.id.results_toolbar)

        selectedEventViewModel.event.observe(viewLifecycleOwner) { event ->
            resultsToolbar.title = event.name
            resultsToolbar.subtitle = dataProcessor.eventTypeToString(event.eventType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}