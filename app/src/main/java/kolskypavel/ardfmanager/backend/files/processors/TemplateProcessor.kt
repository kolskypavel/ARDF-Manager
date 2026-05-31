package kolskypavel.ardfmanager.backend.files.processors;

import android.content.Context
import org.openardf.radioomanager.shared.files.TemplateRenderer
import java.io.IOException

object TemplateProcessor {

    @Throws(IOException::class)
    fun loadTemplate(templateName: String, context: Context): String {
        val inputStream = context.assets.open(templateName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun processTemplate(template: String, params: Map<String, String>): String {
        return TemplateRenderer.render(template, params)
    }
}
