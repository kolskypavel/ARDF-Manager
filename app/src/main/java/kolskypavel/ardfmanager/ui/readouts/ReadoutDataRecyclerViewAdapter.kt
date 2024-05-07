package kolskypavel.ardfmanager.ui.readouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData

class ReadoutDataRecyclerViewAdapter(
    private var values: List<ReadoutData>,
    private val context: Context,
    private val onReadoutClicked: (readoutData: ReadoutData) -> Unit,
    private val onMoreClicked: (action: Int, position: Int, readoutData: ReadoutData) -> Unit
) : RecyclerView.Adapter<ReadoutDataRecyclerViewAdapter.ReadoutViewHolder>() {
    val dataProcessor = DataProcessor.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadoutViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_readout, parent, false)

        return ReadoutViewHolder(adapterLayout)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: ReadoutViewHolder, position: Int) {
        val item = values[position]

        if (item.competitorCategory?.competitor != null) {
            holder.competitorView.text =
                "${item.competitorCategory!!.competitor!!.firstName} ${item.competitorCategory!!.competitor.lastName}"
        } else {
            holder.competitorView.setText(R.string.unknown_competitor)
        }

        if (item.competitorCategory?.category != null) {
            holder.categoryView.text = item.competitorCategory!!.category!!.name
        } else {
            holder.categoryView.text = context.getString(R.string.no_category)
        }

        holder.siNumberView.text = if (item.readoutResult.readout.siNumber != null) {
            item.readoutResult.readout.siNumber.toString()
        } else {
            context.getString(R.string.unknown)
        }
//        holder.runTimeView.text =
//            item.readout!!.runTime?.let { dataProcessor.durationToString(it) }.orEmpty()
        holder.placementView.text = ""

        //Set the start + finish + readout time
        holder.startTimeView.text = if (item.readoutResult.readout.startTime != null) {
            item.readoutResult.readout.startTime.toString()
        } else {
            context.getString(R.string.unknown)
        }

        holder.finishTimeView.text = if (item.readoutResult.readout.finishTime != null) {
            item.readoutResult.readout.finishTime.toString()
        } else {
            context.getString(R.string.unknown)
        }

        holder.readoutTimeView.text = item.readoutResult.readout.readoutTime.toString()

        //Set readout detail navigation
        holder.itemView.setOnClickListener {
            onReadoutClicked(item)
        }


        //Set context menu
        holder.moreBtn.setOnClickListener {

            val popupMenu = PopupMenu(context, holder.moreBtn)
            popupMenu.inflate(R.menu.context_menu_readout)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_edit_readout -> {
                        onMoreClicked(0, position, item)
                        true
                    }

                    R.id.menu_item_delete_readout -> {
                        onMoreClicked(1, position, item)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }
    }

    inner class ReadoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var competitorView: TextView = view.findViewById(R.id.readout_item_competitor)
        var siNumberView: TextView = view.findViewById(R.id.readout_item_si_number)
        var runTimeView: TextView = view.findViewById(R.id.readout_item_run_time)
        var startTimeView: TextView = view.findViewById(R.id.readout_item_start_time)
        var finishTimeView: TextView = view.findViewById(R.id.readout_item_finish_time)
        var readoutTimeView: TextView = view.findViewById(R.id.readout_item_readout_time)
        var categoryView: TextView = view.findViewById(R.id.readout_item_category)
        var placementView: TextView = view.findViewById(R.id.readout_item_placement)
        var moreBtn: ImageButton = view.findViewById(R.id.readout_item_more_btn)
    }
}