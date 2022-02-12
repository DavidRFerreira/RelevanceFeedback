package model

//
//  PlayResults.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import kotlinx.serialization.*

@Serializable
data class ResultWrapper(
    var responseHeader: ResponseHeader? = null,
    var response: Response? = null
)

@Serializable
data class ResponseHeader(
    var status: Int? = null,
    var QTime: Int? = null
)

@Serializable
data class Response(
    var numFound: Int? = null,
    var start: Int? = null,
    var numFoundExact: Boolean? = null,
    var docs: ArrayList<Result>? = null
)

@Serializable
data class Result(
    var id: String? = null,
    var play_id: ArrayList<Int>? = null,
    var minute: ArrayList<String>? = null,
    var play: String? = null,
    var match_id: ArrayList<Int>? = null,
    var home_score: ArrayList<Int>? = null,
    var away_score: ArrayList<Int>? = null,
    var previous_play: String? = null,
    var next_play: String? = null,
    var isRelevant: Boolean = false,
    var tfVector: HashMap<String, Int>? = null,
    var players: ArrayList<String>? = null
)