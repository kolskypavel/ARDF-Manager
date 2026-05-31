package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.time.Duration
import java.time.LocalTime
import java.util.UUID

/** UI edit wrapper that tracks punch field validation state. */
data class PunchEditItemWrapper(
    var punch: Punch,
    var isCodeValid: Boolean,
    var isTimeValid: Boolean,
    var isDayValid: Boolean,
    var isWeekValid: Boolean,
) {
    companion object {
        /** Wraps alias-punch aggregates with optimistic valid flags for the edit UI. */
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

        /** Extracts punch entities from edit wrappers for persistence. */
        fun getPunches(punchEditItemWrappers: ArrayList<PunchEditItemWrapper>): ArrayList<Punch> {
            val punches = ArrayList<Punch>()
            punchEditItemWrappers.forEach { wrapper ->
                punches.add(wrapper.punch)
            }
            return punches
        }

        /** Builds an editable start or finish punch from a result, or an empty placeholder if missing. */
        fun getStartOrFinishWrapper(
            start: Boolean,
            result: Result?,
            raceId: UUID
        ): PunchEditItemWrapper {

            if (start) {
                return if (result?.startTime != null) {
                    PunchEditItemWrapper(
                        Punch(
                            UUID.randomUUID(),
                            raceId,
                            null,
                            null,
                            0,
                            result.startTime!!,
                            result.startTime!!,
                            SIRecordType.START,
                            0,
                            PunchStatus.VALID,
                            Duration.ZERO
                        ), true, true, true, true
                    )
                } else {
                    PunchEditItemWrapper(
                        Punch(
                            UUID.randomUUID(),
                            raceId,
                            null,
                            null,
                            0,
                            SITime(LocalTime.MIN),
                            SITime(LocalTime.MIN),
                            SIRecordType.START,
                            0,
                            PunchStatus.VALID,
                            Duration.ZERO
                        ), true, true, true, true
                    )

                }
            }

            else {
                return if (result?.finishTime != null) {
                    PunchEditItemWrapper(
                        Punch(
                            UUID.randomUUID(),
                            raceId,
                            null,
                            null,
                            0,
                            result.finishTime!!,
                            result.finishTime!!,
                            SIRecordType.FINISH,
                            0,
                            PunchStatus.VALID,
                            Duration.ZERO
                        ), isCodeValid = true, isTimeValid = true, isDayValid = true, isWeekValid = true
                    )
                } else {
                    PunchEditItemWrapper(
                        Punch(
                            UUID.randomUUID(),
                            raceId,
                            null,
                            null,
                            0,
                            SITime(LocalTime.MIN),
                            SITime(LocalTime.MIN),
                            SIRecordType.FINISH,
                            0,
                            PunchStatus.VALID,
                            Duration.ZERO
                        ), isCodeValid = true, isTimeValid = true, isDayValid = true, isWeekValid = true
                    )

                }
            }
        }
    }
}
