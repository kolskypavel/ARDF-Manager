package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.ControlPoint

data class ControlPointItemWrapper(
    var controlPoint: ControlPoint,
    var isCodeValid: Boolean,
) {

    companion object {
        fun getWrappers(controlPoints: ArrayList<ControlPoint>): ArrayList<ControlPointItemWrapper> {
            return ArrayList(controlPoints.map { controlPoint ->
                ControlPointItemWrapper(
                    controlPoint,
                    true
                )
            })
        }

        fun getControlPoints(controlPoints: ArrayList<ControlPointItemWrapper>): ArrayList<ControlPoint> {
            return ArrayList(controlPoints.map { it.controlPoint })
        }
    }
}