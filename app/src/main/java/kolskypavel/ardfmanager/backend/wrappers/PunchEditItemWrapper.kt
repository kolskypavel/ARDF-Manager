package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Punch

data class PunchEditItemWrapper(
    var punch: Punch,
    var isCodeValid: Boolean,
    var isTimeValid: Boolean,
    var isDayValid: Boolean,
    var isWeekValid: Boolean,
) {
    companion object {
        fun getWrappers(punches: ArrayList<Punch>): ArrayList<PunchEditItemWrapper> {
            return ArrayList(punches.map { punch ->
                PunchEditItemWrapper(
                    punch,
                    isCodeValid = true,
                    isTimeValid = true,
                    isDayValid = true,
                    isWeekValid = true
                )
            })
        }

        fun getPunches(punchEditItemWrappers: ArrayList<PunchEditItemWrapper>): ArrayList<Punch> {
            return ArrayList(punchEditItemWrappers.map { it.punch })
        }
    }
}