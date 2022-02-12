package handlers

//
//  RelevanceFeedbackHandler.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import model.ResultWrapper

/**
 * Relevance Feedback involves the user in the process to improve the final result set,
 * by asking the user to classify each result as relevant or non-relevant.
 */
class RelevanceFeedbackHandler {

    companion object {

        /**
         * Handles the user interaction to classify each result as relevant or non-relevant.
         */
        fun manuallySelectRelevantResults(results: ResultWrapper)  {
            val playsResults = results.response?.docs

            if (playsResults != null) {
                for (play in playsResults) {

                    println("===================")
                    println("Play ID: " + (play.play_id?.get(0) ?: 0))
                    println(("Play: " + play.play))
                    println(("NextPlay: " + play.next_play))
                    println("Is this relevant? y/n: ")

                    // Expects the user input (y - relevant / n - non-relevant).
                    val userResponse = readLine()

                    if (userResponse != null) {
                        if (userResponse.lowercase() == "y") {
                            if (!play.play_id.isNullOrEmpty()) {
                                play.isRelevant = true
                            }
                        } else if (userResponse.lowercase() != "n") {
                            println("Invalid option!")
                        }
                    }
                }
            }
        }
    }
}