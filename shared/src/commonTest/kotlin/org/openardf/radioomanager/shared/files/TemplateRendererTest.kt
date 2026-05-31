package org.openardf.radioomanager.shared.files

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateRendererTest {
    @Test
    fun rendersNamedParameters() {
        val template = "{{title_results}}${FileConstants.KEY_TAB}{{race_name}}"
        val params = mapOf(
            FileConstants.KEY_TITLE_RESULTS to "Results",
            FileConstants.KEY_RACE_NAME to "Sprint"
        )

        assertEquals("Results\tSprint", TemplateRenderer.render(template, params))
    }

    @Test
    fun leavesUnknownParametersInPlace() {
        assertEquals("{{missing}}", TemplateRenderer.render("{{missing}}", emptyMap()))
    }
}
