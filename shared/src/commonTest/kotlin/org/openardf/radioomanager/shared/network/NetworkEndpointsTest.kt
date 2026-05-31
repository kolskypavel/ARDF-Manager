package org.openardf.radioomanager.shared.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkEndpointsTest {
    @Test
    fun exposesProviderEndpoints() {
        assertTrue(NetworkEndpoints.ROBIS_RACE_API_URL.startsWith("https://rob-is.cz/api/"))
        assertTrue(NetworkEndpoints.ROBIS_PLAYGROUND_RACE_API_URL.startsWith("https://playground.rob-is.cz/api/"))
        assertEquals("https://api.oresults.eu", NetworkEndpoints.ORESULTS_RESULTS_API_URL)
        assertEquals("https://api.orienteerfeed.com/rest/v1/upload/iof", NetworkEndpoints.OFEED_RESULTS_API_URL)
    }

    @Test
    fun exposesProviderHeaders() {
        assertEquals("Race-Api-Key", NetworkHeaders.ROBIS_API_HEADER)
        assertEquals("apiKey", NetworkHeaders.ORESULTS_API_HEADER)
        assertEquals("Authorization", NetworkHeaders.OFEED_API_AUTH_HEADER)
        assertEquals("eventId", NetworkHeaders.OFEED_EVENT_ID)
    }
}
