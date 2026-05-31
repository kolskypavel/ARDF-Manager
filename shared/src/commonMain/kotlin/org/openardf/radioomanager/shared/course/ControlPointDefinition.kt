package org.openardf.radioomanager.shared.course

import org.openardf.radioomanager.shared.domain.ControlPointType

/** Parsed control-point definition with SI code, ARDF role, and sequence order. */
data class ControlPointDefinition(
    val siCode: Int,
    val type: ControlPointType,
    val order: Int
)
