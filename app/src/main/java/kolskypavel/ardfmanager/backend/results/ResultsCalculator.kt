package kolskypavel.ardfmanager.backend.results

import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Readout

class ResultsCalculator {

    val dataProcessor = DataProcessor.get()

    /**
     * Processes the given readout - calculate the score and times
     */
    fun processReadout(readout: Readout) {
        val competitor = dataProcessor.getCompetitorBySINumber(readout.siNumber, readout.eventId)
        if (competitor != null) {
            if (competitor.categoryId != null) {

            }
        }
    }

    /**
     * Process the classics race
     */
    fun processClassics(readout: Readout, controlPoint: List<ControlPoint>) {

    }

    /**
     * Process the sprint race
     */
    fun processSprint(readout: Readout, controlPoint: List<ControlPoint>) {

    }

    /**
     * Process the orienteering race
     */
    fun processOrienteering(readout: Readout, controlPoint: List<ControlPoint>) {}

    fun processCustom() {
        //TODO: Not yet implemented
    }
}