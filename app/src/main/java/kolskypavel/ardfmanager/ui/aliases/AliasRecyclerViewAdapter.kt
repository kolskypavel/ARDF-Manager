package kolskypavel.ardfmanager.ui.aliases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.backend.wrappers.AliasEditItemWrapper
import kolskypavel.ardfmanager.ui.SelectedRaceViewModel
import java.util.UUID

class AliasRecyclerViewAdapter(
    var values: ArrayList<AliasEditItemWrapper>,
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
        holder.name.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            if (!nameWatcher(position, cs.toString()))
                holder.name.error = holder.name.context.getString(R.string.invalid)
        }

        holder.siCode.doOnTextChanged { cs: CharSequence?, i: Int, i1: Int, i2: Int ->
            if (!codeWatcher(position, cs.toString()))
                holder.siCode.error = holder.siCode.context.getString(R.string.invalid)
        }

        holder.addBtn.setOnClickListener {
            addAlias(holder.adapterPosition)
        }

        holder.deleteBtn.setOnClickListener {
            deleteAlias(holder.adapterPosition)
        }

        val item = values[position]
        holder.siCode.setText(item.alias.siCode.toString())
        holder.name.setText(item.alias.name)

    }

    private fun codeWatcher(position: Int, code: String): Boolean {
        values[position].isCodeValid = isCodeValid(code)
        values[position].alias.siCode = if (code.isEmpty()) 0 else code.toInt()
        return values[position].isCodeValid
    }

    private fun nameWatcher(position: Int, name: String): Boolean {
        values[position].isNameValid = isNameValid(name)
        values[position].alias.name = name
        return values[position].isNameValid
    }

    private fun isCodeValid(code: String): Boolean {
        if (code.isEmpty()) {
            return false
        }

        return isCodeAvailable(code.toInt())
    }

    private fun isNameValid(name: String): Boolean {
        if (name.isEmpty()) {
            return false
        }

        return isNameAvailable(name)
    }

    private fun isCodeAvailable(code: Int): Boolean = values.all { a -> code != a.alias.siCode }

    private fun isNameAvailable(name: String): Boolean = values.all { a -> name != a.alias.name }

    private fun checkCodes(): Boolean = values.all { a -> a.isCodeValid }

    private fun checkNames(): Boolean = values.all { a -> a.isNameValid }

    fun checkFields(): Boolean = values.all { a -> a.isNameValid && a.isCodeValid }


    fun addAlias(position: Int) {
        val aliasWrapper = AliasEditItemWrapper(
            Alias(
                UUID.randomUUID(),
                selectedRaceViewModel.getCurrentRace().id,
                0,
                ""
            ),
            isCodeValid = false, isNameValid = false
        )

        if (position == values.size - 1) {
            values.add(aliasWrapper)
        } else {
            values.add(position + 1, aliasWrapper)
        }
        notifyItemInserted(position + 1)
    }

    private fun deleteAlias(position: Int) {
        if (position in 0..<values.size) {
            //TODO: Remove focus to prevent crashes
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