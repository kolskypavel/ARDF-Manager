package kolskypavel.ardfmanager.backend.prints

import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable

class PrintProcessor() {
    var printerReady = false

    fun print() {
        if (printerReady) {
            var printables = ArrayList<Printable>()
            var printable = TextPrintable.Builder()
                .setText("Hello World")
                .build()
            printables.add(printable)
            Printooth.printer().print(printables)
        }
    }
}