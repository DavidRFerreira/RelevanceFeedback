# Relevance Feedback and Pseudo Relevance Feedback

Kotlin console application implementation of Relevance Feedback and Pseudo Relevance Feedback that together with Rocchio Algorithm can improve results in the context of Information Retrieval. 

This also presents a proposal to improve the original Rocchio Algorithm by incorporating an Immediate Neighborhood Frequency. 

This project was originaly built for a search system of football plays for all Premier League's matches in the year 2020/2021 integrated with [Apache Solr](https://solr.apache.org). But besides the Model classes and the Networking Layer, all the logic for the algorithms can be applied or interpolated for any search system. 

This repository does not include any data or any scripts to run a Solr instance. Again, its only purpose is to present the implementation of Relevance Feedback, Pseudo Relevance Feedback and Rocchio Algorithm in the Kotlin language. 

## Table of Contents.

1. [Overview of the Algorithm](#overview)
2. [Concepts Introduction](#Concepts)
    1. [Relevance Feedback](#Relevance)
    2. [Pseudo Relevance Feedback](#Pseudo)
    3. [Rocchio Algorithm](#Rocchio)
    4. [Step-by-Step Demonstration](#Demonstration)
5. [Proposal to Improve Rocchio Algorithm with Immediate Neighborhood Factor.](#proposal)
6. [Usage Examples](#usage)
    1. [Example 1: Relevance Feedback](#example1)
    2. [Example 2: Pseudo Relevance Feedback](#example2)
    3. [Example 3: Relevance Feedback with the Immediate Neighborhood Factor](#example3)
7. [Further Reading](#furhter)


<a name="overview"/>

## Overview of the Algorithm. 

1. User performs a search with his original query. 
2. For each iteration:
    1. Use Apache Solr to return top 10 results (or any other software).
    2. Classify each result as relevant or non-relevant. 
        1. If Relevance Feedback, display results and let the user classify each of them as relevant or non-relevant
        1. If Pseudo Relevance Feedback, assume top k results as relevant and the others as non-relevant.
    2. Build the Inverted Matrix. 
    3. Use Rocchio Algorithm to create the Query Vector.
    4. Expand the query with the n heighest weighted terms.

<a name="Concepts"/>

## Concepts Introduction 

<a name="Relevance"/>

### Relevance Feedback. 
 
Involves the user in the process to improve the result set by considering its feedback about the initial set of results.

Explores the idea that it may be difficult to formulate a query when you don't know the collection, but it is easy to judge documents
according to their relevance for the user's own information need. 

The basic procedure is to let the user classify each result retrieved after its original query as relevant or non-relevant. The system takes this feedback (results marked as relevant or as non-relevant) and computes a better representation of the information need. 

<a name="Pseudo"/>

### Pseudo Relevance Feedback.

It is based in the same idea of Relevance Feedback, but instead of expecting the user to classify the results it provides a method for automatic local analysis. 

Basically, it assumes the top k ranked results as relevant and the others as non-relevant. 

<a name="Rocchio"/>

### Rocchio Algorithm

It is a classic algorithm for implementing Relevance Feedback (and Pseudo too) by incorporating the feedback information into the vector space model.

It tries to reach an optimal query vector, which maximizes the difference between the average vector representing the relavant documents and the average vector representing the non-relevant documents. 

After the Rocchio Algorithm, each term in the inverted matrix has an associated weight according to its ocurrence in relevant or non-relevant documents. The greater the weight, the more relevant that term is for the query (in an ideal situation, not always true).

The Rocchio Algorithm is defined by a formula that can be seen in this [wikipedia page](https://en.wikipedia.org/wiki/Rocchio_algorithm). 

<a name="Demonstration"/>

### Step-by-Step Demonstration

Now, a brief demonstration of the Relevance Feedback algorithm. 

The user wants to read all plays where a goal as disallowed by var (**information need**).

So, the **user searches** for 
```
[goal disallowed by var] (**original query**).
```

The system **retrieves the top 10** results for this query:
- Result 1.
- Result 2.
- ...
- Result 3. 

Since this is the **Relevance Feedback**, each result is displayed to the user and the user classifies each of them as relevant or non-relevant.
- Result 1: Relevant.
- Result 2: Relevant.
- Result 3: Non-Relevant.
- Result 4: Relevant.
- ...
- Result 10: Non-Relevant. 

Then the system builds the **inverted matrix**. 

The inverted matrix contains a dictionary with all terms from the results. Each dictionary entry (term) contains an associated posting with the documentID and the position in which that term occurs in that document.

```
- [term] -> [documentID: positions of occurence] 
```

In this example, 

```
[tries] -> [16613: 4]
[caught] -> [1386: 12]
...
[goal] -> [9796: 21] -> [15237: 13] -> [1386: 20, 28] -> ...
```

We pass the inverted matrix and the list of results classified as relevant/non-relevant to the **Rocchio Algorithm**

It now computes the Query Vector. In the end, each term is associated with a weight. 

```
[dean] -> [0.75]
[strong] -> [0.75]
...
[assistant] -> [0.46]
...
[referee] -> [0.419]
...
```

The system selects the two heighest weighted terms to append to the original query. 
```
[goal disallowed by var dean strong] (modified query)
```

Multiple iterations can be done. 

<a name="proposal"/>

## Proposal to Improve Rocchio Algorithm with Immediate Neighborhood Factor. 

When building this system, there were some "problems" found with the Rocchio Algorithm for many different queries. 

Due to some characteristics of the documents on this collection, in the end of the Rocchio Algorithm there were many terms with the same weight. Sometimes, there were more than 20 terms with the same heighest weight. 

For example, on the example presented earlier, we got:
```
[dean] -> [0.75]
[makes] -> [0.75]
[advice] -> [0.75]
[strong] -> [0.75]
[video] -> [0.75]
[assistant] -> [0.75]
...
[challenge] -> [0.75]
```

How can we choose the most relevant terms if there are many of them with the heighest score?

Of course that exploring and understanding more clearly the documents in the collection and tweaking, for example the stopwords list, can minimize this problem. But for this system, it was not enough. 

So I came up with an idea (based in some already existing notions) on how we can further discriminate each term according to its relevance to the query. 

The idea is to reward terms that occur in the immediate neighborhood of terms that belong to a query. 

For example, let assume the next query and result:
- Query: goal disallowed by var.
- Result: "The goal by Manchester Utd is disallowed on the **advice** of the video assistant referee due to an offside." 

According to this idea, the term "advice" is going to be rewarded because it is in the immediate proximity of the term "disallowed" which is a query's term (we ignore stopwords).

So, the Rocchio Algorithm formula is extended to: 

```
Ql = Sum( Qm[i] + (d * V[i]) )
```

where
- Ql is the optimal query vector modified.
- Qm[i] is the weight of term i in the query vector obtained with the Rocchio Algorithm. 
- d is the attenuation weight for the Immediate Neighborhood Factor.
- V[i] is the Immediate Neighborhood Factor for term i (number of times it occurs in the immediate proximity to query's terms). 

With our earlier example, this changed the Query Vector to:
- [advice] -> [1.15]
- [referee] -> [0.81]
- [dean] -> [0.75]
- ...

This version was able to improve the Mean Average Precision (MAP) for all queries evaluated both on Relevance Feedback and Pseudo Relevance Feedback. 

The base version of Pseudo Relevance Feedback had a worse MAP than the base system (without any feedback). But with the Immediate Neighborhood Factor achieved an higher MAP than the base system. 

On this system, the highest MAP scoring was achieved with Relevance Feedback with the Immediate Neighborhood Factor. 

<a name="usage"/>

## Usage Examples

<a name="example1"/>

### Example 1: Relevance Feedback.

```kotlin
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
```

<a name="example2"/>

### Example 2: Pseudo Relevance Feedback.

```kotlin
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
```

<a name="example3"/>

### Example 3: Relevance Feedback with the Immediate Neighborhood Factor.

```kotlin
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
```

<a name="furhter"/>

## Further Reading 

Croft, W. B., Metzler, D., & Strohman, T. (2015). Search Engines. Information Retrieval in Practice. Pearson Education.  