package helpers

//
//  Constants.kt
//  FootballPlaysSearchEngine
//
//  Created by David Ferreira on 12/02/2022.
//

class Constants {
    companion object {
        const val applyPorterStemmer = false
        const val applyStopwordsElimination = true

        // Rocchio Algorithm
        const val rewardForRelevantOccurrence = 0.75
        const val penalizationForNonRelevantOccurrence = -0.15

        // Rocchio Algorithm with Immediate Neighborhood Factor.
        const val weightRewardImmediateNeighborhood = 0.40

        var STOPWORDS = arrayListOf<String>(
            "about",
            "above",
            "after",
            "again",
            "against",
            "all",
            "am",
            "an",
            "and",
            "any",
            "are",
            "aren",
            "as",
            "at",
            "be",
            "because",
            "been",
            "before",
            "being",
            "below",
            "between",
            "both",
            "but",
            "by",
            "can",
            "cannot",
            "could",
            "couldn",
            "did",
            "didn",
            "do",
            "does",
            "doesn",
            "doing",
            "don",
            "down",
            "during",
            "each",
            "few",
            "for",
            "from",
            "further",
            "had",
            "hadn",
            "has",
            "hasn",
            "have",
            "haven",
            "having",
            "he",
            "her",
            "here",
            "here",
            "hers",
            "herself",
            "him",
            "himself",
            "his",
            "how",
            "how",
            "if",
            "in",
            "into",
            "is",
            "isn",
            "it",
            "its",
            "itself",
            "let",
            "me",
            "more",
            "most",
            "mustn",
            "my",
            "myself",
            "no",
            "nor",
            "not",
            "of",
            "off",
            "on",
            "once",
            "only",
            "or",
            "other",
            "ought",
            "our",
            "ours",
            "ourselves",
            "out",
            "over",
            "own",
            "same",
            "shan",
            "she",
            "should",
            "shouldn",
            "so",
            "some",
            "such",
            "than",
            "that",
            "the",
            "their",
            "theirs",
            "them",
            "themselves",
            "then",
            "there",
            "these",
            "they",
            "this",
            "those",
            "through",
            "to",
            "too",
            "under",
            "until",
            "up",
            "very",
            "was",
            "wasn",
            "we",
            "were",
            "weren",
            "what",
            "when",
            "where",
            "which",
            "while",
            "who",
            "whom",
            "why",
            "with",
            "would",
            "wouldn",
            "you",
            "your",
            "yours",
            "yourself",
            "yourselves",
            "arsenal",
            "aston",
            "villa",
            "brighton",
            "albion",
            "burnley",
            "chelsea",
            "crystal",
            "palace",
            "everton",
            "fulham",
            "leeds",
            "leicester",
            "liverpool",
            "manchester",
            "city",
            "manchester",
            "newcastle",
            "manchester",
            "sheffield",
            "southampton",
            "tottenham",
            "hotspur",
            "tottenham",
            "west",
            "bromwich",
            "albion",
            "united",
            "ham",
            "west",
            "wolverhampton",
            "wanderers",
            "brom"
        )
    }
}