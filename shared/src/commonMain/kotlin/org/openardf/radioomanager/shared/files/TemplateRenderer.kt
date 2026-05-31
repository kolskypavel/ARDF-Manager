package org.openardf.radioomanager.shared.files

/** Minimal token-replacement renderer for bundled text and HTML result templates. */
object TemplateRenderer {
    /** Replaces every provided token and expands the shared tab token as a final pass. */
    fun render(template: String, params: Map<String, String>): String {
        var output = template

        for (param in params) {
            output = output.replace(param.key, param.value)
        }

        return output.replace(FileConstants.KEY_TAB, "\t")
    }
}
