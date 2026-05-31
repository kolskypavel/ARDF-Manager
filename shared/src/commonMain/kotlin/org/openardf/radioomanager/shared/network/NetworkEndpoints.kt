package org.openardf.radioomanager.shared.network

/** Provider endpoint URLs shared by Android and future desktop result-service code. */
object NetworkEndpoints {
    const val ROBIS_RACE_API_URL = "https://rob-is.cz/api/?type=json&name=race"
    const val ROBIS_PLAYGROUND_RACE_API_URL = "https://playground.rob-is.cz/api/?type=json&name=race"
    const val ROBIS_RESULTS_API_URL = "https://rob-is.cz/api/results/?name=json"
    const val ROBIS_PLAYGROUND_RESULTS_API_URL = "https://playground.rob-is.cz/api/results/?name=json"
    const val ORESULTS_RESULTS_API_URL = "https://api.oresults.eu"
    const val OFEED_RESULTS_API_URL = "https://api.orienteerfeed.com/rest/v1/upload/iof"
}

/** Provider HTTP header names shared by platform-specific network clients. */
object NetworkHeaders {
    const val ROBIS_API_HEADER = "Race-Api-Key"
    const val ORESULTS_API_HEADER = "apiKey"
    const val OFEED_API_AUTH_HEADER = "Authorization"
    const val OFEED_EVENT_ID = "eventId"
}
