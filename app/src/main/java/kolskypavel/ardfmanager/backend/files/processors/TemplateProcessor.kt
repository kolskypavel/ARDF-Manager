package kolskypavel.ardfmanager.backend.files.processors;

import android.content.Context
import org.openardf.radioomanager.shared.files.TemplateRenderer
import java.io.IOException

/** Android asset loader wrapper around the shared template renderer. */
object TemplateProcessor {

    /** Reads a template file from the app's bundled assets. */
    @Throws(IOException::class)
    fun loadTemplate(templateName: String, context: Context): String {
        val inputStream = context.assets.open(templateName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    /** Replaces template placeholders with their resolved parameter values. */
    fun processTemplate(template: String, params: Map<String, String>): String {
        return TemplateRenderer.render(template, params)
    }
}
