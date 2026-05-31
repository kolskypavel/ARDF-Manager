package org.openardf.radioomanager.shared.event

data class StandardCategoryDefinition(
    val name: String,
    val isMan: Boolean,
    val maxAge: Int
)

object StandardCategoryRules {
    fun parseDefinition(row: String): StandardCategoryDefinition? {
        val fields = row.split(";")
        if (fields.size != 3) {
            return null
        }

        val name = fields[0].trim()
        val isManToken = fields[1].trim()
        val maxAge = fields[2].trim().toIntOrNull() ?: return null

        if (name.isEmpty() || isManToken !in setOf("0", "1") || maxAge <= 0) {
            return null
        }

        return StandardCategoryDefinition(
            name = name,
            isMan = isManToken == "1",
            maxAge = maxAge
        )
    }
}
