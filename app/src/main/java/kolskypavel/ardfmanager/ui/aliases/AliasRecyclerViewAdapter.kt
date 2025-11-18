package kolskypavel.ardfmanager.ui.aliases

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.sportident.SIConstants.isSICodeValid
import kolskypavel.ardfmanager.backend.wrappers.AliasEditItemWrapper
import java.util.UUID

class AliasRecyclerViewAdapter(
    var values: ArrayList<AliasEditItemWrapper>,
    val raceId: UUID
) :
    RecyclerView.Adapter<AliasRecyclerViewAdapter.AliasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_alias, parent, false)

        return AliasViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: AliasViewHolder, position: Int) {
        val item = values[position]
        holder.siCode.setText(item.alias.siCode.toString())
        holder.name.setText(item.alias.name)

        // Add a warning to newly created wrapper via + button
        if (!item.isNameValid) {
            holder.name.error = holder.itemView.context.getString(R.string.general_required)
        }

        if (!item.isCodeValid) {
            holder.siCode.error = holder.itemView.context.getString(R.string.general_required)
        }

        holder.name.doOnTextChanged { cs: CharSequence?, _, _, _ ->
            try {
                nameWatcher(holder.adapterPosition, cs.toString(), holder.name.context)
            } catch (e: IllegalArgumentException) {
                holder.name.error = e.message
            }
        }

        holder.siCode.doOnTextChanged { cs: CharSequence?, _, _, _ ->
            try {
                codeWatcher(holder.adapterPosition, cs.toString(), holder.name.context)
            } catch (e: IllegalArgumentException) {
                holder.siCode.error = e.message
            }
        }

        holder.addBtn.setOnClickListener {
            addAlias(holder.adapterPosition)
        }

        holder.deleteBtn.setOnClickListener {
            //Remove focus to prevent crash
            holder.name.clearFocus()
            holder.siCode.clearFocus()
            deleteAlias(holder.adapterPosition)
        }
    }

    private fun codeWatcher(position: Int, code: String, context: Context) {
        if (code.isEmpty()) {
            values[position].isCodeValid = false
            throw IllegalArgumentException(context.getString(R.string.general_required))
        }

        val codeValue = code.toInt()

        if (!isSICodeValid(codeValue)) {
            values[position].isCodeValid = false
            throw IllegalArgumentException(context.getString(R.string.general_invalid))
        }

        // Use position-aware availability so the item itself isn't treated as duplicate
        if (!isCodeAvailable(codeValue, position)) {
            values[position].isCodeValid = false
            throw IllegalArgumentException(context.getString(R.string.general_duplicate))
        }

        values[position].isCodeValid = true
        values[position].alias.siCode = if (code.isEmpty()) 0 else code.toInt()
    }

    private fun nameWatcher(position: Int, name: String, context: Context) {
        if (name.isEmpty()) {
            values[position].isNameValid = false
            throw IllegalArgumentException(context.getString(R.string.general_required))
        }

        // Use position-aware availability so the item itself isn't treated as duplicate
        if (!isNameAvailable(name, position)) {
            values[position].isNameValid = false
            throw IllegalArgumentException(context.getString(R.string.general_duplicate))
        }

        values[position].isNameValid = true
        values[position].alias.name = name
    }

    private fun isCodeAvailable(code: Int, position: Int): Boolean =
        values.withIndex().all { (i, a) -> if (i == position) true else code != a.alias.siCode }

    private fun isNameAvailable(name: String, position: Int): Boolean =
        values.withIndex().all { (i, a) -> if (i == position) true else name != a.alias.name }

    fun checkFields(): Boolean = values.all { a -> a.isNameValid && a.isCodeValid }

    fun addAlias(position: Int) {
        val aliasWrapper = AliasEditItemWrapper(
            Alias(
                UUID.randomUUID(),
                raceId,
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
        if (position in 0 until values.size) {
            values.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun addStandardAliases(international: Boolean) {
        val standard = ArrayList<AliasEditItemWrapper>()

        standard.add(AliasEditItemWrapper(Alias(UUID.randomUUID(), raceId, 31, "1"), true, true))
        standard.add(AliasEditItemWrapper(Alias(UUID.randomUUID(), raceId, 32, "2"), true, true))
        standard.add(AliasEditItemWrapper(Alias(UUID.randomUUID(), raceId, 33, "3"), true, true))
        standard.add(AliasEditItemWrapper(Alias(UUID.randomUUID(), raceId, 34, "4"), true, true))
        standard.add(AliasEditItemWrapper(Alias(UUID.randomUUID(), raceId, 35, "5"), true, true))
        standard.add(AliasEditItemWrapper(Alias(UUID.randomUUID(), raceId, 36, "S"), true, true))
        standard.add(
            AliasEditItemWrapper(
                Alias(
                    UUID.randomUUID(),
                    raceId,
                    41,
                    if (international) "F1" else "R1"
                ), true, true
            )
        )
        standard.add(
            AliasEditItemWrapper(
                Alias(
                    UUID.randomUUID(),
                    raceId,
                    42,
                    if (international) "F2" else "R2"
                ), true, true
            )
        )
        standard.add(
            AliasEditItemWrapper(
                Alias(
                    UUID.randomUUID(),
                    raceId,
                    43,
                    if (international) "F3" else "R3"
                ), true, true
            )
        )
        standard.add(
            AliasEditItemWrapper(
                Alias(
                    UUID.randomUUID(),
                    raceId,
                    44,
                    if (international) "F4" else "R4"
                ), true, true
            )
        )
        standard.add(
            AliasEditItemWrapper(
                Alias(
                    UUID.randomUUID(),
                    raceId,
                    45,
                    if (international) "F5" else "R5"
                ), true, true
            )
        )

        values = standard
        notifyDataSetChanged()
    }

    class AliasViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var siCode: EditText = view.findViewById(R.id.alias_item_code)
        var name: EditText = view.findViewById(R.id.alias_item_name)
        var addBtn: ImageButton = view.findViewById(R.id.alias_item_add_btn)
        var deleteBtn: ImageButton =
            view.findViewById(R.id.alias_item_delete_btn)
    }
}