package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.Alias
import java.io.Serializable

data class AliasEditItemWrapper(
    var alias: Alias,
    var isCodeValid: Boolean,
    var isNameValid: Boolean,
) : Serializable {
    companion object {
        fun getWrappers(aliases: ArrayList<Alias>): ArrayList<AliasEditItemWrapper> {
            return ArrayList(aliases.map { aliasWrapper ->
                AliasEditItemWrapper(
                    aliasWrapper,
                    isCodeValid = true,
                    isNameValid = true,
                )
            })
        }

        fun getAliases(values: ArrayList<AliasEditItemWrapper>) = values.map { a -> a.alias }

    }
}