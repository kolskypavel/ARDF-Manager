package kolskypavel.ardfmanager.backend.results

import android.content.Context
import android.widget.Toast
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.UUID


class ResultsProcessor {
    val dataProcessor = DataProcessor.get()

    /**
     * Processes the given readout - calculate the score and times
     */
    fun processCardData(cardData: CardData, event: Event, context: Context): Boolean {

        //Check if readout already exists
        if (!dataProcessor.checkIfReadoutExistsBySI(cardData.siNumber, event.id)) {
            val competitor = dataProcessor.getCompetitorBySINumber(cardData.siNumber, event.id)
            val readout =
                Readout(
                    UUID.randomUUID(),
                    cardData.siNumber,
                    cardData.cardType,
                    event.id,
                    competitor?.id,
                    cardData.checkTime, cardData.startTime, cardData.finishTime,
                    Duration.ZERO
                )
            processPunches()
            dataProcessor.createReadout(readout)

            return true
        } else {

            //Run on the main UI thread
            CoroutineScope(Dispatchers.Main).launch {
                //TODO: Notify with snackMessage about existing readout
                Toast.makeText(context, context.getText(R.string.readout_exists), Toast.LENGTH_LONG)
                    .show()

            }
            return false
        }
    }

    private fun processPunches() {

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