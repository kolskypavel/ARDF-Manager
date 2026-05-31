package kolskypavel.ardfmanager.backend.network

import okhttp3.MediaType.Companion.toMediaType
import org.openardf.radioomanager.shared.network.NetworkEndpoints
import org.openardf.radioomanager.shared.network.NetworkHeaders

object NetworkConstants {

    // API URLS
    const val ROBIS_RACE_API_URL = NetworkEndpoints.ROBIS_RACE_API_URL
    const val ROBIS_PLAYGROUND_RACE_API_URL = NetworkEndpoints.ROBIS_PLAYGROUND_RACE_API_URL
    const val ROBIS_RESULTS_API_URL = NetworkEndpoints.ROBIS_RESULTS_API_URL
    const val ROBIS_PLAYGROUND_RESULTS_API_URL = NetworkEndpoints.ROBIS_PLAYGROUND_RESULTS_API_URL
    const val ORESULTS_RESULTS_API_URL = NetworkEndpoints.ORESULTS_RESULTS_API_URL
    const val OFEED_RESULTS_API_URL = NetworkEndpoints.OFEED_RESULTS_API_URL

    // API HEADERS
    const val ROBIS_API_HEADER = NetworkHeaders.ROBIS_API_HEADER
    const val ORESULTS_API_HEADER = NetworkHeaders.ORESULTS_API_HEADER
    const val OFEED_API_AUTH_HEADER = NetworkHeaders.OFEED_API_AUTH_HEADER
    const val OFEED_EVENT_ID = NetworkHeaders.OFEED_EVENT_ID

    val CONTENT_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    val CONTENT_TYPE_XML = "application/xml; charset=utf-8".toMediaType()
    val CONTENT_TYPE_GZIP = "application/gzip; charset=utf-8".toMediaType()

}
