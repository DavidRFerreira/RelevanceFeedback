package handlers

//
//  QueryExpanderHandler.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import helpers.Constants
import model.ResultWrapper
import java.io.File
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.log10

/**
 * Handles algorithms for Query Expansion.
 */
class QueryExpanderHandler {

    companion object {

        /**
         * Selects n tokens with the greatest weight that do not belong to the query.
         */
        fun getTopTerms(currentQuery: String, weights: HashMap<String, Double>, numLimitTokens: Int = 2): List<String> {
            var i = 0
            val topTerms = ArrayList<String>()

            // Sorts the terms by their weight.
            val resultMap = weights.entries.sortedByDescending { it.value }.associate { it.toPair() }

            // For logging purposes, stores each pair of term-weight in a file.
            File("./termWeight.txt").writeText("")
            File("./termWeight.txt").printWriter().use { out ->
                for (term in resultMap) {
                    out.println("===========")
                    out.println(term.key)
                    out.println(term.value)
                }
            }

            // Loops through all terms.
            for (result in resultMap) {

                // Ignore terms that belong to the query.
                if (currentQuery.contains(result.key)) {
                    continue
                }

                // If true, ignores terms that belong to the StopWords lists.
                if (Constants.applyStopwordsElimination) {
                    if (Constants.STOPWORDS.contains(result.key)) {
                        continue
                    }
                }

                // Adds term to a new list of top terms.
                topTerms.add(result.key)

                i++
                if (i == numLimitTokens) {
                    break
                }
            }

            return topTerms
        }


        /**
         * Pure Rocchio Algorithm implementation.
         * Incorporates relevance feedback information into the vector space model.
         * Maximizes the difference between the average vector representing the relevant documents and the average
         * vector representing the non-relevant documents.
         * Assigns a weight to each term according to its relevance to the query.
         */
        fun rocchioAlgorithm(invertedFile: HashMap<String, HashMap<Int, ArrayList<Int>>>,
                             results: ResultWrapper): HashMap<String, Double> {

            val docsResults = results.response?.docs
            val query = HashMap<String, Double>()
            val weights = HashMap<String, Double>()

            // Get all documents/results marked as relevant.
            val relevantDocs = results.response?.docs?.filter { t -> t.isRelevant }
            // Get all documents/results marked as non-relevant.
            val nonRelevantDocs = results.response?.docs?.filter { t -> !t.isRelevant }

            // Initialize each term's weight.
            for (term in invertedFile) {
                weights[term.key] = 0.0
            }

            // Holds the weight for each token related its influence in relevant results.
            val relevantDocsTFWeights = HashMap<String, Double>()
            //  Holds the weight for each token related its influence in non-relevant results.
            val nonRelevantDocsTFWeights = HashMap<String, Double>()

            // Counts the frequency of each term that occurs in a relevant document.
            relevantDocs?.let {
                for (document in it) {
                    document.tfVector?.let { t ->
                        for (term in t) {
                            relevantDocsTFWeights[term.key] =  (relevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()  + (t.getOrDefault(term.key, 0.00)).toDouble()
                        }
                    }
                }
            }

            // Counts the frequency of each term that occurs in a non-relevant document.
            nonRelevantDocs?.let {
                for (document in it) {
                    document.tfVector?.let { t ->
                        for (term in t) {
                            nonRelevantDocsTFWeights[term.key] =  (nonRelevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()  + (t.getOrDefault(term.key, 0.00)).toDouble()
                        }
                    }
                }
            }

            // For each term in the Inverted Matrix.
            for (term in invertedFile) {

                docsResults?.let{
                    invertedFile[term.key]?.let { t ->

                        // Computes idf (inverted document frequency) for a term.
                        // idf = log(N / df_t) where:
                        // N - number of total documents.
                        // df_t - document frequency for term t.
                        val idf = log10(it.count().toDouble() / t.keys.count())

                        // For each entry (term) in the Inverted Matrix,
                        // Computes its weight according to the Rocchio Algorithm formula.
                        for (dictionaryKey in t) {
                            it.indexOfFirst { (it.play_id?.get(0) ?: -1) == dictionaryKey.key }.let { y ->
                                val x1 = (weights.getOrDefault(term.key, 0)).toDouble()
                                var x2 = 0.0
                                val x3 = idf
                                var x4 = 0.0
                                val x5 = relevantDocs?.count() ?: 0


                                if (docsResults[y].isRelevant) {
                                    x2 = Constants.rewardForRelevantOccurrence
                                    x4 = (relevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()
                                } else {
                                    x2 = Constants.penalizationForNonRelevantOccurrence
                                    x4 = (nonRelevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()
                                }

                                // Rocchio Algorithm formula.
                                weights[term.key] = x1 + x2 * x3 * (x4 / x5)
                            }
                        }

                        // Update the term weight in the vector model.
                        if (query.contains(term.key)) {
                            query[term.key] = 0 * (query.getOrDefault(term.key, 0)).toDouble() + (weights.getOrDefault(term.key, 0)).toDouble()
                        } else {
                            query[term.key] = (weights.getOrDefault(term.key, 0)).toDouble()
                        }
                    }
                }
            }

            // For logging purposes, stores each pair of term-weight in a file.
            File("./rocchioResults.txt").writeText("")
            File("./rocchioResults.txt").printWriter().use { out ->
                for (term in query) {
                    out.println("===========")
                    out.println(term.key)
                    out.println(term.value)
                }
            }

            return query
        }


        /**
         * Rocchio Algorithm extended.
         * This is an original proposal to optimize the Rocchio Algorithm's result. (David R Ferreira, 2022)
         * Each term's weight is additionally rewarded everytime that it occurs in a result in the immediate proximity
         * of a term that belongs to the query.
         * It is aims to further discriminate each term according to its relevance to the query.
         */
        fun rocchioAlgorithmExtended(invertedFile: HashMap<String, HashMap<Int, ArrayList<Int>>>,
                             results: ResultWrapper, immediateNeighborhoodFrequency: HashMap<String, Int>): HashMap<String, Double> {

            val docsResults = results.response?.docs
            val query = HashMap<String, Double>()
            val weights = HashMap<String, Double>()

            // Get all documents/results marked as relevant.
            val relevantDocs = results.response?.docs?.filter { t -> t.isRelevant }
            // Get all documents/results marked as non-relevant.
            val nonRelevantDocs = results.response?.docs?.filter { t -> !t.isRelevant }

            // Initialize each term's weight.
            for (term in invertedFile) {
                weights[term.key] = 0.0
            }

            // Holds the weight for each token related its influence in relevant results.
            val relevantDocsTFWeights = HashMap<String, Double>()
            //  Holds the weight for each token related its influence in non-relevant results.
            val nonRelevantDocsTFWeights = HashMap<String, Double>()

            // Counts the frequency of each term that occurs in a relevant document.
            relevantDocs?.let {
                for (document in it) {
                    document.tfVector?.let { t ->
                        for (term in t) {
                            relevantDocsTFWeights[term.key] =  (relevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()  + (t.getOrDefault(term.key, 0.00)).toDouble()
                        }
                    }
                }
            }

            // Counts the frequency of each term that occurs in a non-relevant document.
            nonRelevantDocs?.let {
                for (document in it) {
                    document.tfVector?.let { t ->
                        for (term in t) {
                            nonRelevantDocsTFWeights[term.key] =  (nonRelevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()  + (t.getOrDefault(term.key, 0.00)).toDouble()
                        }
                    }
                }
            }

            // For each term in the Inverted Matrix.
            for (term in invertedFile) {

                docsResults?.let{
                    invertedFile[term.key]?.let { t ->

                        // Computes idf (inverted document frequency) for a term.
                        // idf = log(N / df_t) where:
                        // N - number of total documents.
                        // df_t - document frequency for term t.
                        val idf = log10(it.count().toDouble() / t.keys.count())

                        // For each entry (term) in the Inverted Matrix,
                        // Computes its weight according to the Rocchio Algorithm formula.
                        for (dictionaryKey in t) {
                            it.indexOfFirst { (it.play_id?.get(0) ?: -1) == dictionaryKey.key }.let { y ->
                                val x1 = (weights.getOrDefault(term.key, 0)).toDouble()
                                var x2 = 0.0
                                val x3 = idf
                                var x4 = 0.0
                                val x5 = relevantDocs?.count() ?: 0


                                if (docsResults[y].isRelevant) {
                                    x2 = Constants.rewardForRelevantOccurrence
                                    x4 = (relevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()
                                } else {
                                    x2 = Constants.penalizationForNonRelevantOccurrence
                                    x4 = (nonRelevantDocsTFWeights.getOrDefault(term.key, 0)).toDouble()
                                }

                                // Rocchio Algorithm formula.
                                weights[term.key] = x1 + x2 * x3 * (x4 / x5)
                            }
                        }

                        // Update the term weight in the vector model.
                        if (query.contains(term.key)) {
                            query[term.key] = 0 * (query.getOrDefault(term.key, 0)).toDouble() + (weights.getOrDefault(term.key, 0)).toDouble()
                        } else {
                            query[term.key] = (weights.getOrDefault(term.key, 0)).toDouble()
                        }
                    }
                }

                // Where the difference with the Pure Rocchio Algorithm occurs.
                // The term's weight is not only the result from the Rocchio Algorithm but it is algo given an additional weight
                // if the terms occurs in a result document in the immediate neighborhood of a term that also belongs to query.
                query[term.key] = query.getOrDefault(term.key, 0).toDouble() + (Constants.weightRewardImmediateNeighborhood * immediateNeighborhoodFrequency.getOrDefault(term.key, 0).toDouble())
            }

            // For logging purposes, stores each pair of term-weight in a file.
            File("./rocchioExtendedResults.txt").writeText("")
            File("./rocchioExtendedResults.txt").printWriter().use { out ->
                for (term in query) {
                    out.println("===========")
                    out.println(term.key)
                    out.println(term.value)
                }
            }

            return query
        }

        /**
         * For each term in the Inverted Matrix, counts the number of times that term appears in the result
         * in the immediate neighborhood of a term that belongs to the query.
         */
        fun computeImmediateNeighborhoodFrequency(results: ResultWrapper, invertedFile: HashMap<String, HashMap<Int, ArrayList<Int>>>, query: String) : HashMap<String, Int> {
            /*
                Example
                Query: goal disallowed by VAR.

                Returns 10 results.

                One of those results is:
                "The goal by Manchester Utd is disallowed on the advice of the video assistant referee due to an offside".

                The term "advice" is going to be rewarded because it occurs in the immediate neighborhood of a term that belongs
                to the query, in this case the term "disallowed" (we ignore stop words).

                So, ImmediateNeighborhoodFrequency["advice"] += 1.
             */

            // Characters to split the tokens in the text results.
            val tokensArr = query.split(" ", "?", "!", ".", "]", "[", "}", "{", "'s", ",", "-", ")", "(", "'s", "-")

            val immediateNeighborhoodFrequency = HashMap<String, Int>()

            // Loop through all dictionary values (terms) in the inverted matrix.
            for (key in invertedFile.keys) {

                invertedFile[key]?.let {

                    for (playID in it.keys) {

                        val document = results.response?.docs?.first { t -> (t.play_id?.get(0) ?: -1) == playID }
                        val resultStr = document?.play.toString().plus(document?.next_play.toString())

                        val tokens = resultStr.split(" ", "?", "!", ".", "]", "[", "}", "{", "'s", ",", "-", ")", "(", "'s", "-")

                        it[playID]?.let { t ->


                            for (position in t) {

                                // If the term in the immediate next position belongs to the query,
                                // we increment the Immediate Neighborhood Frequency.
                                if (position + 1 < tokensArr.count()) {
                                    if (tokensArr.contains(tokens[position + 1])) {
                                        immediateNeighborhoodFrequency[key] = immediateNeighborhoodFrequency.getOrDefault(key, 0) + 1
                                    }
                                }

                                // If the term in the immediate previous position belongs to the query,
                                // we increment the Immediate Neighborhood Frequency.
                                if (position - 1 >= 0) {
                                    if (tokensArr.contains(tokens[position - 1])) {
                                        immediateNeighborhoodFrequency[key] = immediateNeighborhoodFrequency.getOrDefault(key, 0) + 1
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return immediateNeighborhoodFrequency
        }
    }
}