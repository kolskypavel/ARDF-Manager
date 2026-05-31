package org.openardf.radioomanager.shared.files

object TemplateRenderer {
    fun render(template: String, params: Map<String, String>): String {
        var output = template

        for (param in params) {
            output = output.replace(param.key, param.value)
        }

        return output.replace(FileConstants.KEY_TAB, "\t")
    }
}
