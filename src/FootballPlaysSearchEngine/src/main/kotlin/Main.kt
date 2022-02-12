import handlers.*
import networking.SolrClient
import java.io.File

//
//  Main.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//


fun main(args: Array<String>) {
    exampleRelevanceFeedback()
}



fun exampleRelevanceFeedback() {
    val limitIterations = 3

    var query = "goal disallowed by var"

    var i = 0
    while (i != limitIterations) {
        println("****************************************************************")
        println("****************************************************************")

        // Request results to Apache Solr.
        val results = SolrClient.getResults(query) ?: break

        // Relevance Feedback: Display each result on the console so that the user can classify them as relevant / non-relevant.
        RelevanceFeedbackHandler.manuallySelectRelevantResults(results)

        // Computes and displays Precision value.
        println("PRECISION: " + MetricsEvaluation.computePrecision(results))

        // Build inverted index.
        val invertedFile = InvertedFileHandler.createInvertedFile(results)

        invertedFile.let {

            // Computes Rocchio Algorithm weight for each term.
            val rocchioResults = QueryExpanderHandler.rocchioAlgorithm(it, results)

            // Selects top n terms with the greatest weight.
            val newTerms = QueryExpanderHandler.getTopTerms(query, rocchioResults)

            // Appends those terms to the original query.
            query = query + " " + newTerms[0] + " " + newTerms[1]

            i++
        }
    }
}


fun examplePseudoRelevanceFeedback() {
    val limitIterations = 3

    var query = "goal disallowed by var"

    var i = 0
    while (i != limitIterations) {
        println("****************************************************************")
        println("****************************************************************")

        // Request results to Apache Solr.
        val results = SolrClient.getResults(query) ?: break

        // Pseudo Relevance Feedback: Marks top n results as relevant and the others as non-relevant.
        PseudoRelevanceFeedbackHandler.markTopResultsAsRelevant(results)

        // Build inverted index.
        val invertedFile = InvertedFileHandler.createInvertedFile(results)

        invertedFile.let {

            // Computes Rocchio Algorithm weight for each term.
            val rocchioResults = QueryExpanderHandler.rocchioAlgorithm(it, results)

            // Selects top n terms with the greatest weight.
            val newTerms = QueryExpanderHandler.getTopTerms(query, rocchioResults)

            // Appends those terms to the original query.
            query = query + " " + newTerms[0] + " " + newTerms[1]

            i++
        }
    }
}

fun exampleRelevanceFeedbackWithImmediateNeighborhoodFactor() {
    val limitIterations = 3

    var query = "goal disallowed by var"

    var i = 0
    while (i != limitIterations) {
        println("****************************************************************")
        println("****************************************************************")

        // Request results to Apache Solr.
        val results = SolrClient.getResults(query) ?: break

        // Relevance Feedback: Display each result on the console so that the user can classify them as relevant / non-relevant.
        RelevanceFeedbackHandler.manuallySelectRelevantResults(results)

        // Computes and displays Precision value.
        println("PRECISION: " + MetricsEvaluation.computePrecision(results))

        // Build inverted index.
        val invertedFile = InvertedFileHandler.createInvertedFile(results)

        invertedFile.let {

            // Computes Immediate Neighborhood Frequency for each term.
            val immediateNeighborhoodFrequency = QueryExpanderHandler.computeImmediateNeighborhoodFrequency(results, invertedFile, query)

            // Computes Rocchio Algorithm weight for each term and with the addition of the Immediate Neighborhood Frequency.
            val rocchioResults = QueryExpanderHandler.rocchioAlgorithmExtended(it, results, immediateNeighborhoodFrequency)

            // Selects top n terms with the greatest weight.
            val newTerms = QueryExpanderHandler.getTopTerms(query, rocchioResults)

            // Appends those terms to the original query.
            query = query + " " + newTerms[0] + " " + newTerms[1]

            i++
        }
    }
}








