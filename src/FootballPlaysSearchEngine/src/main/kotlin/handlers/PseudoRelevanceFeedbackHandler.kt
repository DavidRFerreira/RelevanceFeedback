package handlers

//
//  PseudoRelevanceFeedbackHandler.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import model.ResultWrapper

/**
 * Pseudo Relevance Feedback assumes the n first results as Relevant
 * and the other ones as Non-Relevant.
 */
class PseudoRelevanceFeedbackHandler {
    companion object {

        // Number of results to consider relevant from the top.
        private const val numberTopResults: Int = 2

        /**
         * Loops through a given list of results and marks the first
         * n ones as Relevant.
         */
        fun markTopResultsAsRelevant(results: ResultWrapper)  {
            val playsResults = results.response?.docs

            playsResults?.let {
                for (play in it.take(numberTopResults)) {
                    play.isRelevant = true
                }
            }
        }
    }
}