package networking

//
//  SolrClient.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import model.ResultWrapper

fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

/**
 * Handles Networking related configurations and methods
 * to request Apache Solr the results for each query.
 */
class SolrClient {

    companion object {
        private const val baseURL = "http://localhost:8983/solr/football/select?"

        /**
         * Requests results from Apache Solr for a given query.
         */
        fun getResults(query: String): ResultWrapper? {

            // Params to send to Apache Solr.
            // We request 10 results, give weights to each field (play & next_play),
            // and use edismax for retrieval.
            val params = mapOf(
                "q" to query,
                "defType" to "edismax",
                "qf" to "play^1.5 next_play^2.5",
                "rows" to "10"
            )

            val urlParams = params.map { (k, v) -> "${(k.utf8())}=${v.utf8()}" }
                .joinToString("&")

            val url = baseURL + urlParams

            return try {
                val client = HttpClient.newBuilder().build()

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                Json{ignoreUnknownKeys = true}.decodeFromString<ResultWrapper>(response.body().toString())
            } catch (e: Exception) {
                println("Error during requesting results")
                null
            }
        }
    }
}