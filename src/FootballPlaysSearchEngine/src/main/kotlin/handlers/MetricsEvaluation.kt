package handlers

//
//  MetricsEvaluation.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import model.ResultWrapper

/**
 * Computation of Information Retrieval Evaluation metrics.
 */
class MetricsEvaluation {
    companion object {

        /**
         * Computes and returns the Precision metric.
         */
        fun computePrecision(results: ResultWrapper): Double {
            results.response?.docs?.let {
                return (it.count { t -> t.isRelevant }.toDouble() / it.count().toDouble())
            }
            return -1.0
        }
    }
}