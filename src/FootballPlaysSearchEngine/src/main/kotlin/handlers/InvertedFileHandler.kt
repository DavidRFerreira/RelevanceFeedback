package handlers

//
//  InvertedFileHandler.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

import helpers.Constants
import model.ResultWrapper
import java.io.File

/**
 * Handles methods to build the Inverted Index (Inverted File).
 * An Inverted Index is a data structure which given a term provides access to the list
 * of documents that contain the terms.
 */
class InvertedFileHandler {

    companion object {

        /**
         * Creates the Inverted Index given a list of documents/results.
         */
        fun createInvertedFile(results: ResultWrapper): HashMap<String, HashMap<Int, ArrayList<Int>>> {

            // Key: DocumentID / Value: Frequency of the term.
            val termFrequencies: HashMap<String, Int> = HashMap()
            // Key: Term / Value: HashMap of the DocumentID and frequency of the term in that document.
            val invertedFile: HashMap<String, HashMap<Int, ArrayList<Int>>> = HashMap()
            // Results.
            val playsResults = results.response?.docs

            // Document-at-a-Time.
            if (playsResults != null) {
                for (result in playsResults) {

                    var j = 0
                    val terms = ArrayList<String>()

                    // Get result ID.
                    var playId: Int
                    result.play_id.let {
                        playId = it?.get(0)!!
                    }

                    // In this example, each result has a field for "play" and one for "next_play".
                    // We combine them together as a unique field.
                    val resultStr = result.play.toString().plus(result.next_play.toString())

                    // Tokens with each split the tokens in the text.
                    val tokens = resultStr.split(" ", "?", "!", ".", "]", "[", "}", "{", "'s", ",", "-", ")", "(", "'s", "-")

                    // Loop through all tokens present at the result.
                    for (currentToken in tokens) {

                        var token = currentToken.lowercase()

                        // If true, apply porter stemmer to chop the end of the word in order to reduce inflectional forms.
                        if (Constants.applyPorterStemmer) {
                            token = PortStemmer.stem(token)
                        }

                        // If token is empty or has less than 1 character or more than 10, we don't add it to the inverted index.
                        if (token == " " || token.length <= 1 || token.length >= 10) {
                            continue
                        }

                        terms.add(token)

                        // Increment the token frequency count.
                        termFrequencies.let {
                            it[token] = it.getOrDefault(token, 0) + 1
                        }


                        invertedFile.let {

                            // If the inverted index does not contain an entry for this token yet,
                            // we initialize an entry for it.
                            if (!it.contains(token)) {
                                it[token] = HashMap()
                            }

                            it[token]?.let { t->

                                // If there is no posting yet for this token associated to this result/document,
                                // we initialize an entry for it.
                                if (!t.contains(playId)) {
                                    t[playId] = ArrayList()
                                }

                                t[playId]?.add(j)
                            }
                        }

                        // We also build the term frequency vector.
                        // The "Result" class has a field called "tfVector" that stores
                        // the frequency for each term in that result.
                        if (result.tfVector == null) {
                            result.tfVector = HashMap()
                        }
                        result.tfVector!![token] = result.tfVector!!.getOrDefault(token, 0) + 1

                        j += 1
                    }
                }
            }

            // For logging purposes, stores each pair of term-weight in a file.
            File("./invertedIndex.txt").writeText("")
            File("./invertedIndex.txt").printWriter().use { out ->
                for (term in invertedFile) {
                    out.println("===========")
                    out.println(term)
                }
            }

            return invertedFile
        }
    }

}