package kolskypavel.ardfmanager.ui.competitors

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import de.codecrafters.tableview.TableDataAdapter
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.CompetitorTableDisplayType
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime


class CompetitorTableViewAdapter(
    private var values: List<CompetitorData>,
    private var display: CompetitorTableDisplayType,
    private val context: Context,
    private val selectedRaceViewModel: SelectedRaceViewModel,
    private val onMoreClicked: (action: Int, position: Int, competitor: CompetitorData) -> Unit,
) : TableDataAdapter<CompetitorData>(context, values) {

    override fun getCellView(rowIndex: Int, columnIndex: Int, parentView: ViewGroup?): View {
        val item = values[rowIndex]
        val view = layoutInflater.inflate(R.layout.competitor_table_cell, parentView, false)
        val cell: TextView = view.findViewById(R.id.competitor_table_cell_text)

        when (display) {

            CompetitorTableDisplayType.OVERVIEW -> {
                when (columnIndex) {
                    0 -> cell.text =
                        item.competitorCategory.competitor.startNumber.toString()

                    1 -> {
                        cell.text =
                            item.competitorCategory.competitor.lastName.uppercase() + " " + item.competitorCategory.competitor.firstName
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
                                TimeProcessor.durationToMinuteString(item.competitorCategory.competitor.drawnRelativeStartTime!!)
                        } else {
                            cell.text = "-"
                        }
                    }

                    2 -> cell.text =
                        item.competitorCategory.competitor.lastName.uppercase() + " " + item.competitorCategory.competitor.firstName

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
                            item.competitorCategory.competitor.lastName.uppercase() + " " + item.competitorCategory.competitor.firstName
                    }

                    1 -> {
                        cell.text = item.competitorCategory.category?.name
                            ?: context.getString(R.string.no_category)
                    }

                    2 -> {
                        cell.text =
                            TimeProcessor.durationToMinuteString(item.resultData!!.result.runTime)
                    }

                    3 -> {
                        cell.text = item.resultData!!.result.startTime?.localTimeFormatter() ?: ""
                    }

                    4 -> {
                        cell.text = item.resultData!!.result.finishTime?.localTimeFormatter() ?: ""
                    }
                }
            }

            CompetitorTableDisplayType.ON_THE_WAY -> {
                when (columnIndex) {
                    0 -> {
                        cell.text =
                            item.competitorCategory.competitor.lastName.uppercase() + " " + item.competitorCategory.competitor.firstName
                    }

                    1 -> cell.text = item.competitorCategory.category?.name
                        ?: context.getString(R.string.no_category)

                    2 -> {

                        if (item.competitorCategory.competitor.drawnRelativeStartTime != null) {
                            cell.text =
                                TimeProcessor.durationToMinuteString(item.competitorCategory.competitor.drawnRelativeStartTime!!)
                        } else {
                            cell.text = "-"
                        }

                    }

                    3 -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            while (true) {
                                if (item.competitorCategory.competitor.drawnRelativeStartTime != null) {

                                    val runDuration = TimeProcessor
                                        .runDurationFromStart(
                                            selectedRaceViewModel.getCurrentRace().startDateTime,
                                            item.competitorCategory.competitor.drawnRelativeStartTime!!
                                        )
                                    if (runDuration != null) {
                                        cell.text =
                                            TimeProcessor.durationToMinuteString(runDuration)
                                    } else {
                                        cell.text = "-"
                                    }

                                } else {
                                    cell.text = "-"
                                }
                                delay(1000)
                            }
                        }
                    }

                    4 -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            while (true) {
                                if (item.competitorCategory.competitor.drawnRelativeStartTime != null) {
                                    if (item.resultData == null) {

                                        val limit: Duration =
                                            if (item.competitorCategory.category?.timeLimit != null) {
                                                item.competitorCategory.category!!.timeLimit!!
                                            } else {
                                                selectedRaceViewModel.getCurrentRace().timeLimit
                                            }
                                        val toLimit =
                                            TimeProcessor.durationToLimit(
                                                selectedRaceViewModel.getCurrentRace().startDateTime,
                                                item.competitorCategory.competitor.drawnRelativeStartTime!!,
                                                limit, LocalDateTime.now()
                                            )

                                        if (toLimit != null) {
                                            cell.text =
                                                TimeProcessor.durationToMinuteString(toLimit)
                                        } else {
                                            cell.text = "-"
                                        }
                                    }
                                }
                                delay(1000)
                            }
                        }
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