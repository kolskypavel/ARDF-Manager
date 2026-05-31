package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.Alias
import java.io.Serializable

/** UI edit wrapper that tracks alias field validation state. */
data class AliasEditItemWrapper(
    var alias: Alias,
    var isCodeValid: Boolean,
    var isNameValid: Boolean,
) : Serializable {
    companion object {
        /** Wraps aliases with optimistic valid flags for the edit UI. */
        fun getWrappers(aliases: ArrayList<Alias>): ArrayList<AliasEditItemWrapper> {
            return ArrayList(aliases.map { aliasWrapper ->
                AliasEditItemWrapper(
                    aliasWrapper,
                    isCodeValid = true,
                    isNameValid = true,
                )
            })
        }

        /** Extracts aliases from edit wrappers for persistence. */
        fun getAliases(values: ArrayList<AliasEditItemWrapper>) = values.map { a -> a.alias }

    }
}
