package org.openardf.radioomanager.shared.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileDefinitionsTest {
    @Test
    fun resolvesDataFormatByStoredValue() {
        assertEquals(DataFormat.TXT, DataFormat.getByValue(0))
        assertEquals(DataFormat.IOF_XML, DataFormat.getByValue(4))
        assertNull(DataFormat.getByValue(-1))
    }

    @Test
    fun resolvesDataTypeByStoredValue() {
        assertEquals(DataType.CATEGORIES, DataType.getByValue(0))
        assertEquals(DataType.RESULTS_FINAL, DataType.getByValue(5))
        assertNull(DataType.getByValue(-1))
    }

    @Test
    fun exposesTemplateKeysUsedByTextAndHtmlExporters() {
        assertEquals("{{race_name}}", FileConstants.KEY_RACE_NAME)
        assertEquals("{{comp_split_time}}", FileConstants.KEY_COMP_SPLIT_TIME)
        assertEquals("<b>{{comp_split_code}}</b> - {{comp_split_time}} ", FileConstants.HTML_SPLITS_CODE)
    }
}
