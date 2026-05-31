package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.ControlPoint

/** UI edit wrapper that tracks control-point SI-code validation state. */
data class ControlPointItemWrapper(
    var controlPoint: ControlPoint,
    var isCodeValid: Boolean,
) {

    companion object {
        /** Wraps control points with optimistic valid flags for the edit UI. */
        fun getWrappers(controlPoints: ArrayList<ControlPoint>): ArrayList<ControlPointItemWrapper> {
            return ArrayList(controlPoints.map { controlPoint ->
                ControlPointItemWrapper(
                    controlPoint,
                    true
                )
            })
        }

        /** Extracts control points from edit wrappers for persistence. */
        fun getControlPoints(controlPoints: ArrayList<ControlPointItemWrapper>): ArrayList<ControlPoint> {
            return ArrayList(controlPoints.map { it.controlPoint })
        }
    }
}
