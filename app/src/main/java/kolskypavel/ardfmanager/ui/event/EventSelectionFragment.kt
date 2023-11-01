package kolskypavel.ardfmanager.ui.event

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.room.entitity.EventBand
import kolskypavel.ardfmanager.room.entitity.EventLevel
import kolskypavel.ardfmanager.room.entitity.EventType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * A fragment representing a list of Items.
 */
class EventSelectionFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var eventAddFAB: FloatingActionButton
    private lateinit var recyclerView: RecyclerView

    private val eventsViewModel: EventsViewModel by viewModels()

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
        eventAddFAB = view.findViewById(R.id.btn_event_add)

        toolbar.setTitle(R.string.event_toolbar_title)
        toolbar.inflateMenu(R.menu.event_fragment_menu)

        eventAddFAB.setOnClickListener {
            findNavController().navigate(
                EventSelectionFragmentDirections.eventModification(-1, null, null)
            )
        }

        setMenuListener()
        setRecyclerAdapter()
        setFragmentListener()
    }

    private fun setMenuListener() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_import_file -> {
                    true
                }

                R.id.navigation_categories -> {
                    // Navigate to settings screen.
                    true
                }

                R.id.navigation_competitors -> {
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
            val eventName = bundle.getString(EventCreateDialogFragment.BUNDLE_KEY_EVENT_NAME)!!
            val eventDate =
                LocalDate.parse(bundle.getString(EventCreateDialogFragment.BUNDLE_KEY_EVENT_DATE))
            val eventTime =
                LocalTime.parse(bundle.getString(EventCreateDialogFragment.BUNDLE_KEY_EVENT_START_TIME))
            val eventType: EventType
            val eventLevel: EventLevel
            val eventBand: EventBand


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                eventType =
                    bundle.getSerializable(
                        EventCreateDialogFragment.BUNDLE_KEY_EVENT_TYPE,
                        EventType::class.java
                    )!!
                eventLevel = bundle.getSerializable(
                    EventCreateDialogFragment.BUNDLE_KEY_EVENT_LEVEL,
                    EventLevel::class.java
                )!!
                eventBand = bundle.getSerializable(
                    EventCreateDialogFragment.BUNDLE_KEY_EVENT_BAND,
                    EventBand::class.java
                )!!

            } else {
                eventType =
                    bundle.getSerializable(EventCreateDialogFragment.BUNDLE_KEY_EVENT_TYPE) as EventType
                eventLevel =
                    bundle.getSerializable(EventCreateDialogFragment.BUNDLE_KEY_EVENT_LEVEL) as EventLevel
                eventBand =
                    bundle.getSerializable(EventCreateDialogFragment.BUNDLE_KEY_EVENT_BAND) as EventBand
            }

            //create new event
            if (create) {
                eventsViewModel.createEvent(
                    eventName,
                    eventDate,
                    eventTime,
                    eventType,
                    eventLevel,
                    eventBand
                )
            }
            //Edit an existing event
            else {
                eventsViewModel.modifyEvent(
                    position, eventName,
                    eventDate,
                    eventTime,
                    eventType,
                    eventLevel,
                    eventBand
                )
            }
        }
    }

    private fun setRecyclerAdapter() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                eventsViewModel.events.collect { events ->
                    recyclerView.adapter = EventRecyclerViewAdapter(events, { eventId ->
                        findNavController().navigate(
                            EventSelectionFragmentDirections.openEvent(
                                eventId
                            )
                        )
                    }) { position, name, date, time, eventType, eventLevel, eventBand ->
                        findNavController().navigate(
                            EventSelectionFragmentDirections.eventModification(
                                position,
                                date,
                                time,
                            )
                        )
                    }
                }
            }
        }
    }
}