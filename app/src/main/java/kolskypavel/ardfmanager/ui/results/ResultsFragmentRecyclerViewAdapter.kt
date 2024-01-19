package kolskypavel.ardfmanager.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.wrappers.ResultDataWrapper

class ResultsFragmentRecyclerViewAdapter(var values: ArrayList<ResultDataWrapper>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(
    ) {

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
                    categoryName.text = dataList.category!!.name
                } else {
                    //TODO: no category
                }
                downIV.setOnClickListener {
                    expandOrCollapseParentItem(dataList, position)
                }
            }
        } else {
            holder as ChildViewHolder

            holder.apply {
                val singleCompetitor = dataList.subList.first()
                if (singleCompetitor.competitor != null)
                    competitorName.text =
                        "${singleCompetitor.competitor!!.firstName} ${singleCompetitor.competitor!!.lastName!!}"
            }
        }
    }

    private fun expandOrCollapseParentItem(singleBoarding: ResultDataWrapper, position: Int) {

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
                val parentModel = ResultDataWrapper()
                parentModel.isChild = 0
                val subList: ArrayList<ResultDataWrapper> = ArrayList()
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
        val downIV: ImageButton = row.findViewById(R.id.down_iv)
    }

    class ChildViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val competitorName: TextView = row.findViewById(R.id.result_competitor_name)

    }
}
