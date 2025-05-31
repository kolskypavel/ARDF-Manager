package kolskypavel.ardfmanager.ui.results

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ResultData
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.wrappers.ResultWrapper
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResultsFragmentRecyclerViewAdapter(
    var values: ArrayList<ResultWrapper>,
    var context: Context,
    var selectedRaceViewModel: SelectedRaceViewModel,
    private val openDetail: (resultData: ResultData) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(
    ) {
    val dataProcessor = DataProcessor.get()

    override fun onCreateViewHolder(parent: ViewGroup, child: Int): RecyclerView.ViewHolder {

        return if (child == 0) {
            val rowView: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item_result_category, parent, false)
            CategoryViewHolder(rowView)
        } else {
            val rowView: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item_result_competitor, parent, false)
            CompetitorViewHolder(rowView)
        }
    }

    private fun toggleArrow(expandButton: ImageButton, isExpanded: Boolean) {
        if (isExpanded) {
            expandButton.setImageResource(R.drawable.ic_collapse)
        } else {
            expandButton.setImageResource(R.drawable.ic_expand)
        }
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataList = values[position]
        if (dataList.isChild == 0) {
            holder as CategoryViewHolder
            holder.apply {
                if (dataList.category != null) {
                    categoryName.text =
                        "${dataList.category.name} (${
                            dataList.subList.size
                        })"
                } else {
                    categoryName.text =
                        "${context.getText(R.string.no_category)} (${dataList.subList.size})"
                }
                if (dataList.subList.isNotEmpty()) {
                    expandButton.visibility = View.VISIBLE

                    //Set on click expansion + icon
                    holder.itemView.setOnClickListener {
                        expandOrCollapseParentItem(dataList, position)
                        toggleArrow(holder.expandButton, dataList.isExpanded)
                    }

                    holder.expandButton.setOnClickListener {
                        expandOrCollapseParentItem(dataList, position)
                        toggleArrow(holder.expandButton, dataList.isExpanded)
                    }

                } else {
                    expandButton.visibility = View.GONE
                }
                toggleArrow(holder.expandButton, dataList.isExpanded)
            }

        } else {
            holder as CompetitorViewHolder

            holder.apply {
                val singleResult = dataList.subList.first()

                //Set the competitor place
                if (singleResult.resultData != null) {
                    val res = singleResult.resultData!!.result
                    competitorPlace.text =
                        if (res.raceStatus == RaceStatus.VALID && res.place != null) {
                            res.place.toString()
                        } else {
                            dataProcessor.raceStatusToShortString(res.raceStatus)
                        }
                } else {
                    competitorPlace.text = "-"
                }

                competitorName.text =
                    " ${singleResult.competitorCategory.competitor.lastName.uppercase()} ${singleResult.competitorCategory.competitor.firstName}"
                competitorClub.text =
                    singleResult.competitorCategory.competitor.club.ifEmpty {
                        "-"
                    }
                if (singleResult.resultData != null) {
                    competitorTime.text =
                        TimeProcessor.durationToMinuteString(singleResult.resultData!!.result.runTime)
                } else if (singleResult.competitorCategory.competitor.drawnRelativeStartTime != null) {

                    CoroutineScope(Dispatchers.Main).launch {
                        while (true) {
                            competitorTime.text = TimeProcessor.runDurationFromStartString(
                                selectedRaceViewModel.getCurrentRace().startDateTime,
                                singleResult.competitorCategory.competitor.drawnRelativeStartTime!!
                            )
                            delay(1000)
                        }
                    }
                } else {
                    competitorTime.text = "-"
                }
                competitorPoints.text = if (singleResult.resultData?.result?.points != null) {
                    singleResult.resultData?.result?.points.toString()
                } else {
                    "-"
                }
                holder.itemView.setOnClickListener {
                    if (singleResult.resultData != null) {
                        openDetail(singleResult.resultData!!)
                    }
                }

                if (dataList.childPosition % 2 == 1)
                    holder.itemView.setBackgroundResource(R.color.light_grey)
            }
        }
    }

    private fun expandOrCollapseParentItem(singleBoarding: ResultWrapper, position: Int) {
        if (singleBoarding.isExpanded) {
            collapseParentRow(position)
        } else {
            expandParentRow(position)
        }
    }

    private fun expandParentRow(position: Int) {
        val currentBoardingRow = values[position]
        val competitors = currentBoardingRow.subList
        currentBoardingRow.isExpanded = true
        var nextPosition = position
        if (currentBoardingRow.isChild == 0) {

            competitors.forEachIndexed { index, service ->
                val parentModel = ResultWrapper(null, 1, ArrayList(), false, index)
                parentModel.subList.add(service)
                values.add(++nextPosition, parentModel)
            }
            notifyDataSetChanged()
        }
    }

    private fun collapseParentRow(position: Int) {
        val currentBoardingRow = values[position]
        val services = currentBoardingRow.subList
        values[position].isExpanded = false
        if (values[position].isChild == 0) {
            services.forEach { _ ->
                values.removeAt(position + 1)
            }
            notifyDataSetChanged()
        }
    }

    fun expandAllItems() {
        var index = 0
        while (index < values.size) {
            if (values[index].isExpanded) {
                expandParentRow(index)
            }
            index++
        }
    }

    override fun getItemViewType(position: Int): Int = values[position].isChild

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class CategoryViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val categoryName: TextView = row.findViewById(R.id.result_category_name)
        val expandButton: ImageButton = row.findViewById(R.id.down_iv)
    }

    class CompetitorViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val competitorPlace: TextView = row.findViewById(R.id.result_competitor_place)
        val competitorName: TextView = row.findViewById(R.id.result_competitor_name)
        val competitorClub: TextView = row.findViewById(R.id.result_competitor_club)
        val competitorTime: TextView = row.findViewById(R.id.result_competitor_time)
        val competitorPoints: TextView = row.findViewById(R.id.result_competitor_points)
    }
}
