package kolskypavel.ardfmanager.ui.event

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
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.ui.SelectedEventViewModel
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class EventSelectionFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var eventAddFAB: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private var mLastClickTime: Long = 0

    private val eventViewModel: EventViewModel by activityViewModels()
    private val selectedEventViewModel: SelectedEventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_selection, container, false)
        recyclerView = view.findViewById(R.id.event_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.event_toolbar)
        eventAddFAB = view.findViewById(R.id.event_btn_add)

        toolbar.setTitle(R.string.event_toolbar_title)
        toolbar.inflateMenu(R.menu.event_fragment_menu)

        eventAddFAB.setOnClickListener {

            //Prevent accidental double click
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                findNavController().navigate(
                    EventSelectionFragmentDirections.eventModification(true, -1, null)
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
                R.id.event_menu_import_file -> {
                    true
                }

                R.id.event_menu_categories -> {
                    // Navigate to settings screen.
                    true
                }

                R.id.event_menu_about_the_app -> {
                    // Display about app dialog
                    true
                }

                else -> false
            }
        }
    }

    private fun setFragmentListener() {
        setFragmentResultListener(EventCreateDialogFragment.REQUEST_EVENT_MODIFICATION) { _, bundle ->
            val create = bundle.getBoolean(EventCreateDialogFragment.BUNDLE_KEY_CREATE)
            val position = bundle.getInt(EventCreateDialogFragment.BUNDLE_KEY_POSITION)

            val event: Event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    EventCreateDialogFragment.BUNDLE_KEY_EVENT,
                    Event::class.java
                )!!
            } else {
                bundle.getSerializable(EventCreateDialogFragment.BUNDLE_KEY_EVENT) as Event
            }

            //create new event
            if (create) {
                eventViewModel.createEvent(event)
            }
            //Edit an existing event
            else {
                eventViewModel.modifyEvent(event)
                recyclerView.adapter?.notifyItemChanged(position)
            }
        }
    }

    private fun recyclerViewContextMenuActions(action: Int, position: Int, event: Event) {
        when (action) {
            0 -> findNavController().navigate(
                EventSelectionFragmentDirections.eventModification(
                    false, position, event
                )
            )

            1 -> confirmEventDeletion(event)
        }
    }

    /**
     * Displays alert dialog to confirm the deletion of the event
     */
    private fun confirmEventDeletion(event: Event) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.event_delete))
        val message = getString(R.string.event_delete_confirmation) + " " + event.name
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            eventViewModel.deleteEvent(event.id)
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

                eventViewModel.events.collect { events ->
                    recyclerView.adapter =
                        EventRecyclerViewAdapter(
                            events, { eventId ->

                                // Pass the event id into view Model
                                selectedEventViewModel.setEvent(eventId)

                                findNavController().navigate(
                                    EventSelectionFragmentDirections.openEvent()
                                )
                            },
                            //Context menu action setup
                            { action, position, event ->
                                recyclerViewContextMenuActions(
                                    action,
                                    position,
                                    event
                                )
                            }, requireContext()
                        )
                }
            }
        }
    }
}
