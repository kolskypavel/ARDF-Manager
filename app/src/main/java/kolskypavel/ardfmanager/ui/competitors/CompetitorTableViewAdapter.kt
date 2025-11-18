package kolskypavel.ardfmanager.ui.competitors

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import de.codecrafters.tableview.TableDataAdapter
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class CompetitorTableViewAdapter(
    private var values: List<CompetitorData>,
    private var display: CompetitorTableDisplayType,
    private val context: Context,
    private val race: Race,
    private val onMoreClicked: (action: Int, position: Int, competitor: CompetitorData) -> Unit,
) : TableDataAdapter<CompetitorData>(context, values) {
    private val dataProcessor = DataProcessor.get()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var updatingJob: kotlinx.coroutines.Job? = null

    init {
        if (display == CompetitorTableDisplayType.ON_THE_WAY) {
            startUpdating()
        }
    }

    private fun startUpdating() {
        updatingJob?.cancel()
        updatingJob = scope.launch {
            while (true) {
                notifyDataSetChanged() // rebind all cells
                delay(1000)
            }
        }
    }

    override fun getCellView(rowIndex: Int, columnIndex: Int, parentView: ViewGroup?): View {
        val item = values[rowIndex]
        val view = layoutInflater.inflate(R.layout.competitor_table_cell, parentView, false)
        val cell: TextView = view.findViewById(R.id.competitor_table_cell_text)

        Log.d("Adapter", "redraw row=$rowIndex col=$columnIndex")

        when (display) {

            CompetitorTableDisplayType.OVERVIEW -> {
                when (columnIndex) {
                    0 -> cell.text =
                        item.competitorCategory.competitor.startNumber.toString()

                    1 -> {
                        cell.text =
                            item.competitorCategory.competitor.getFullName()
                    }

                    2 -> cell.text = item.competitorCategory.competitor.club
                    3 -> cell.text = item.competitorCategory.category?.name
                        ?: context.getString(R.string.no_category)

                    4 -> cell.text =
                        item.competitorCategory.competitor.siNumber?.toString()
                            ?: "-"
                }
            }

            CompetitorTableDisplayType.START_LIST -> {
                when (columnIndex) {
                    0 -> cell.text =
                        item.competitorCategory.competitor.startNumber.toString()

                    1 -> {
                        if (item.competitorCategory.competitor.drawnRelativeStartTime != null) {
                            cell.text =
                                TimeProcessor.durationToFormattedString(
                                    item.competitorCategory.competitor.drawnRelativeStartTime!!,
                                    true
                                )
                        } else {
                            cell.text = "-"
                        }
                    }

                    2 -> cell.text =
                        item.competitorCategory.competitor.getFullName()

                    3 -> cell.text = item.competitorCategory.category?.name
                        ?: context.getString(R.string.no_category)

                    4 -> cell.text =
                        item.competitorCategory.competitor.siNumber?.toString()
                            ?: "-"
                }
            }

            CompetitorTableDisplayType.FINISH_REACHED -> {
                when (columnIndex) {
                    0 -> {
                        cell.text =
                            item.competitorCategory.competitor.getFullName()
                    }

                    1 -> {
                        cell.text = item.competitorCategory.category?.name
                            ?: context.getString(R.string.no_category)
                    }

                    2 -> {
                        cell.text =
                            TimeProcessor.durationToFormattedString(
                                item.readoutData!!.result.runTime,
                                dataProcessor.useMinuteTimeFormat()
                            )
                    }

                    3 -> {
                        cell.text = item.readoutData!!.result.startTime?.localTimeFormatter() ?: ""
                    }

                    4 -> {
                        cell.text = item.readoutData!!.result.finishTime?.localTimeFormatter() ?: ""
                    }
                }
            }

            CompetitorTableDisplayType.ON_THE_WAY -> {
                when (columnIndex) {
                    0 -> cell.text = item.competitorCategory.competitor.getFullName()
                    1 -> cell.text = item.competitorCategory.category?.name
                        ?: context.getString(R.string.no_category)

                    2 -> {
                        val start = item.competitorCategory.competitor.drawnRelativeStartTime
                        cell.text = if (start != null) {
                            TimeProcessor.durationToFormattedString(start, true)
                        } else "-"
                    }

                    3 -> {
                        val start = item.competitorCategory.competitor.drawnRelativeStartTime
                        val runDuration = if (start != null) {
                            TimeProcessor.runDurationFromStart(
                                race.startDateTime, start,
                                LocalDateTime.now()
                            )
                        } else null
                        cell.text = runDuration?.let {
                            TimeProcessor.durationToFormattedString(
                                it,
                                dataProcessor.useMinuteTimeFormat()
                            )
                        } ?: "-"
                    }

                    4 -> {
                        val start = item.competitorCategory.competitor.drawnRelativeStartTime
                        if (start != null && item.readoutData == null) {
                            val limit =
                                item.competitorCategory.category?.timeLimit ?: race.timeLimit
                            val toLimit = TimeProcessor.durationToLimit(
                                race.startDateTime,
                                start,
                                limit,
                                LocalDateTime.now()
                            )
                            cell.text = toLimit?.let {
                                TimeProcessor.durationToFormattedString(it, true)
                            } ?: "-"
                        } else {
                            cell.text = "-"
                        }
                    }
                }
            }

            CompetitorTableDisplayType.SI_RENT -> {
                when (columnIndex) {
                    0 -> cell.text =
                        item.competitorCategory.competitor.siNumber?.toString()
                            ?: "-"

                    1 -> cell.text = item.competitorCategory.competitor.getFullName()
                    2 -> cell.text = item.competitorCategory.category?.name
                        ?: context.getString(R.string.no_category)

                    3 -> {
                        cell.text = item.readoutData?.result?.startTime?.localTimeFormatter() ?: ""
                    }

                    4 -> {
                        cell.text = item.readoutData?.result?.finishTime?.localTimeFormatter() ?: ""
                    }
                }
            }
        }

        //Set context menu
        view.setOnLongClickListener { w ->
            val popupMenu = PopupMenu(context, w)
            popupMenu.inflate(R.menu.context_menu_competitor)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_edit_competitor -> {
                        onMoreClicked(0, rowIndex, item)
                        true
                    }

                    R.id.menu_item_delete_competitor -> {
                        onMoreClicked(1, rowIndex, item)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
            true
        }
        return view
    }
}