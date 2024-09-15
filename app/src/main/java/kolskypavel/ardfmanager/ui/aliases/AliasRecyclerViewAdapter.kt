package kolskypavel.ardfmanager.ui.aliases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import java.util.UUID

class AliasRecyclerViewAdapter(
    var values: ArrayList<Alias>,
    var selectedRaceViewModel: SelectedRaceViewModel
) :
    RecyclerView.Adapter<AliasRecyclerViewAdapter.AliasViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_alias, parent, false)

        return AliasViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: AliasViewHolder, position: Int) {
        holder.addBtn.setOnClickListener {
            addAlias(holder.adapterPosition)
        }

        holder.deleteBtn.setOnClickListener {
            deleteAlias(holder.adapterPosition)
        }

        val item = values[position]
        holder.siCode.setText(item.siCode.toString())
        holder.name.setText(item.name)

    }

    fun addAlias(position: Int) {
        values.add(
            position, Alias(
                UUID.randomUUID(),
                selectedRaceViewModel.getCurrentRace().id,
                0,
                ""
            )
        )
        notifyItemInserted(position)
    }

    private fun deleteAlias(position: Int) {
        if (position in 0..<values.size) {
            values.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class AliasViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var siCode: EditText = view.findViewById(R.id.alias_item_code)
        var name: EditText = view.findViewById(R.id.alias_item_name)
        var addBtn: ImageButton = view.findViewById(R.id.alias_item_add_btn)
        var deleteBtn: ImageButton =
            view.findViewById(R.id.alias_item_delete_btn)
    }
}