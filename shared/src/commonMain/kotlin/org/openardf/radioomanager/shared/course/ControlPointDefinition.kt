package org.openardf.radioomanager.shared.course

import org.openardf.radioomanager.shared.domain.ControlPointType

data class ControlPointDefinition(
    val siCode: Int,
    val type: ControlPointType,
    val order: Int
)
