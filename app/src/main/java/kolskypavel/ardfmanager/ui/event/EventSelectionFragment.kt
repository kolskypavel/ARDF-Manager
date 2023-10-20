package kolskypavel.ardfmanager.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kolskypavel.ardfmanager.R

/**
 * A fragment representing a list of Items.
 */
class EventSelectionFragment : Fragment() {

    private var columnCount = 1
    private lateinit var toolbar: Toolbar
    private lateinit var eventAddFAB: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_selection, container, false)

        // Set the adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.event_toolbar)
        eventAddFAB = view.findViewById(R.id.btn_event_add)

        toolbar.setTitle(R.string.event_toolbar_title)
        toolbar.inflateMenu(R.menu.event_menu)
        setMenuListener()
        setFABListener()
    }

    private fun setMenuListener() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_import_file -> {
                    true
                }

                R.id.menu_item_global_settings -> {
                    // Navigate to settings screen.
                    true
                }

                R.id.menu_item_about_app -> {
                    // Display about app dialog
                    true
                }

                else -> false
            }
        }
    }

    private fun setFABListener() {
        eventAddFAB.setOnClickListener {

        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            EventSelectionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}