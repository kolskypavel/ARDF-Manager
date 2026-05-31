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
import kolskypavel.ardfmanager.backend.wrappers.AliasEditItemWrapper
import org.openardf.radioomanager.shared.alias.AliasRules
import org.openardf.radioomanager.shared.alias.AliasValidationResult
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
        val result = AliasRules.validateCode(
            code = code,
            existingCodes = values.map { it.alias.siCode },
            position = position
        )

        if (result == AliasValidationResult.Valid) {
            values[position].isCodeValid = true
            values[position].alias.siCode = code.toInt()
        } else {
            values[position].isCodeValid = false
            throw IllegalArgumentException(result.toMessage(context))
        }
    }

    private fun nameWatcher(position: Int, name: String, context: Context) {
        val result = AliasRules.validateName(
            name = name,
            existingNames = values.map { it.alias.name },
            position = position
        )

        if (result == AliasValidationResult.Valid) {
            values[position].isNameValid = true
            values[position].alias.name = name
        } else {
            values[position].isNameValid = false
            throw IllegalArgumentException(result.toMessage(context))
        }
    }

    fun checkFields(): Boolean = values.all { a -> a.isNameValid && a.isCodeValid }

    private fun AliasValidationResult.toMessage(context: Context): String {
        return when (this) {
            AliasValidationResult.Valid -> ""
            AliasValidationResult.Required -> context.getString(R.string.general_required)
            AliasValidationResult.Invalid -> context.getString(R.string.general_invalid)
            AliasValidationResult.Duplicate -> context.getString(R.string.general_duplicate)
        }
    }

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
