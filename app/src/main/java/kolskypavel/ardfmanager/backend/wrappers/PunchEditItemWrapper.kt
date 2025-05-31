package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch

data class PunchEditItemWrapper(
    var punch: Punch,
    var isCodeValid: Boolean,
    var isTimeValid: Boolean,
    var isDayValid: Boolean,
    var isWeekValid: Boolean,
) {
    companion object {
        fun getWrappers(punches: ArrayList<AliasPunch>): ArrayList<PunchEditItemWrapper> {
            return ArrayList(punches.map { ap ->
                PunchEditItemWrapper(
                    ap.punch,
                    isCodeValid = true,
                    isTimeValid = true,
                    isDayValid = true,
                    isWeekValid = true
                )
            })
        }

        fun getPunches(punchEditItemWrappers: ArrayList<PunchEditItemWrapper>): ArrayList<Punch> {
            val punches = ArrayList<Punch>()
            punchEditItemWrappers.forEach { wrapper ->
                punches.add(wrapper.punch)
            }
            return punches
        }
    }
}