package kolskypavel.ardfmanager.ui.competitors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kolskypavel.ardfmanager.databinding.FragmentCompetitorsBinding

class CompetitorsFragment : Fragment() {

    private var _binding: FragmentCompetitorsBinding? = null

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

        val textView: TextView = binding.textHome

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}