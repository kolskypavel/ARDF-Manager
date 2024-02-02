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
import kolskypavel.ardfmanager.backend.wrappers.ReadoutDataWrapper
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper

class ResultsFragmentRecyclerViewAdapter(
    var values: ArrayList<ResultDisplayWrapper>,
    var context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(
    ) {
    val dataProcessor = DataProcessor.get()

    override fun onCreateViewHolder(parent: ViewGroup, child: Int): RecyclerView.ViewHolder {

        return if (child == 0) {
            val rowView: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item_result_category, parent, false)
            GroupViewHolder(rowView)
        } else {
            val rowView: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item_result_competitor, parent, false)
            ChildViewHolder(rowView)
        }
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val dataList = values[position]
        if (dataList.isChild == 0) {
            holder as GroupViewHolder
            holder.apply {
                if (dataList.category != null) {
                    categoryName.text = dataList.category.name + " (" + dataList.category.controlPointsNames +")"
                } else {
                    categoryName.text = context.getText(R.string.no_category)
                }
                if (dataList.subList.isNotEmpty()) {
                    expandButton.visibility = View.VISIBLE

                    //Set on click expansion + icon
                    expandButton.setOnClickListener {
                        if (dataList.isExpanded) {
                            expandButton.setImageResource(R.drawable.ic_expand)
                        } else {
                            expandButton.setImageResource(R.drawable.ic_collapse)
                        }
                        expandOrCollapseParentItem(dataList, position)
                    }
                } else {
                    expandButton.visibility = View.GONE
                }
            }
        } else {
            holder as ChildViewHolder

            holder.apply {
                val singleResult = dataList.subList.first()
                if (singleResult.competitor != null) {
                    competitorName.text =
                        "${singleResult.competitor!!.firstName} ${singleResult.competitor!!.lastName!!}"
                    competitorClub.text = singleResult.competitor!!.club
                    competitorTime.text =
                        dataProcessor.durationToString(singleResult.result?.runTime!!)
                    competitorPoints.text = singleResult.result?.points.toString()
                }
            }
        }
    }

    private fun expandOrCollapseParentItem(singleBoarding: ResultDisplayWrapper, position: Int) {

        if (singleBoarding.isExpanded) {
            collapseParentRow(position)
        } else {
            expandParentRow(position)
        }
    }

    private fun expandParentRow(position: Int) {
        val currentBoardingRow = values[position]
        val services = currentBoardingRow.subList
        currentBoardingRow.isExpanded = true
        var nextPosition = position
        if (currentBoardingRow.isChild == 0) {

            services.forEach { service ->
                val parentModel = ResultDisplayWrapper()
                parentModel.isChild = 1
                val subList: ArrayList<ReadoutDataWrapper> = ArrayList()
                subList.add(service)
                parentModel.subList = subList
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

    override fun getItemViewType(position: Int): Int = values[position].isChild

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class GroupViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val categoryName: TextView = row.findViewById(R.id.result_category_name)
        val expandButton: ImageButton = row.findViewById(R.id.down_iv)
    }

    class ChildViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val competitorPlace: TextView = row.findViewById(R.id.result_competitor_place)
        val competitorName: TextView = row.findViewById(R.id.result_competitor_name)
        val competitorClub: TextView = row.findViewById(R.id.result_competitor_club)
        val competitorTime: TextView = row.findViewById(R.id.result_competitor_time)
        val competitorPoints: TextView = row.findViewById(R.id.result_competitor_points)
    }
}
