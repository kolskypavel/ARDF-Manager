package kolskypavel.ardfmanager.backend.network

import okhttp3.MediaType.Companion.toMediaType

object NetworkConstants {

    // API URLS
    const val ROBIS_RACE_API_URL = "https://rob-is.cz/api/?type=json&name=race"
    const val ROBIS_STARTLIST_API_URL = "https://rob-is.cz/api/startlist/?valid=True"
    const val ROBIS_LIVE_RESULTS_API_URL = "https://rob-is.cz/api/results/?name=json"
    const val ROBIS_FINAL_RESULTS_API_URL = "https://rob-is.cz/api/results/?valid=True"

    const val ROBIS_PLAYGROUND_RACE_API_URL = "https://playground.rob-is.cz/api/?type=json&name=race"
    const val ROBIS_PLAYGROUND_STARTLIST_API_URL = "https://playground.rob-is.cz/api/startlist/?valid=True"
    const val ROBIS_PLAYGROUND_LIVE_RESULTS_API_URL = "https://playground.rob-is.cz/api/results/?name=json"
    const val ROBIS_PLAYGROUND_FINAL_RESULTS_API_URL = "https://playground.rob-is.cz/api/results/?valid=True"

    const val ORESULTS_RESULTS_API_URL =  "https://api.oresults.eu"
    const val OFEED_RESULTS_API_URL =  "https://api.orienteerfeed.com/rest/v1/upload/iof"

    // API HEADERS
    const val ROBIS_API_HEADER = "Race-Api-Key"
    const val ORESULTS_API_HEADER = "apiKey"
    const val OFEED_API_AUTH_HEADER = "Authorization"
    const val OFEED_EVENT_ID = "eventId"

    val CONTENT_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    val CONTENT_TYPE_XML = "application/xml; charset=utf-8".toMediaType()
    val CONTENT_TYPE_GZIP = "application/gzip; charset=utf-8".toMediaType()

}