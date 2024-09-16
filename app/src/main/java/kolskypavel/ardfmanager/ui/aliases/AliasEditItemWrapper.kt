package kolskypavel.ardfmanager.ui.aliases

import kolskypavel.ardfmanager.backend.room.entitity.Alias

data class AliasEditItemWrapper(
    var alias: Alias,
    var isCodeValid: Boolean,
    var isNameValid: Boolean,
)