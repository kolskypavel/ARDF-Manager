package kolskypavel.ardfmanager.ui.readouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kolskypavel.ardfmanager.databinding.FragmentReadoutsBinding

class ReadoutsFragment : Fragment() {

    private var _binding: FragmentReadoutsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReadoutsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}