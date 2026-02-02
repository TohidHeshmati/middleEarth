package com.tohid

import com.tohid.client.QueryParameters
import com.tohid.client.TheOneApiClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

const val THE_ONE_API_TOKEN = "aaa"
const val THE_ONE_API_BASE_URL = "https://the-one-api.dev/v2/"

suspend fun main() {
    val client = TheOneApiClient(THE_ONE_API_TOKEN)

    client.use { api ->
        // showSequential(api)
        // showConcurrent(api)
        // showContextSwitching(api)
        // showErrorHandling(api)
        //showCancellation(api)
        showFanOutFanIn(api)
    }
}

// 1. SEQUENTIAL: The "Standard" way
suspend fun showSequential(api: TheOneApiClient) {
    println("\n--- Pattern 1: Sequential ---")
    val books = api.getBooks()
    val chapters = api.getBookChapters(books.docs.first().id)
    println("Done: Dependent data fetched.")
}

// 2. CONCURRENT: Speeding things up
suspend fun showConcurrent(api: TheOneApiClient) = coroutineScope {
    println("\n--- Pattern 2: Concurrent ---")
    val movies = async { api.getMovies() }
    val chars = async { api.getCharacters() }

    println("Fetched ${movies.await().total} movies and ${chars.await().total} characters.")
}

// 3. DISPATCHERS: Thread Management
suspend fun showContextSwitching(api: TheOneApiClient) {
    println("\n--- Pattern 3: Context Switching ---")
    withContext(Dispatchers.IO) {
        println("I/O Work on: ${Thread.currentThread().name}")
        val quotes = api.getQuotes()

        withContext(Dispatchers.Default) {
            println("Heavy CPU Processing on: ${Thread.currentThread().name}")
            calculateFibonacci(12)
        }
    }
}

suspend fun showErrorHandling(api: TheOneApiClient) {
    println("\n--- Pattern 4: Resilience ---")
    supervisorScope {
        val movies = async {
            throw Exception("API Failed!")
        }
        val characters = async {
            api.getCharacters() // This will still succeed because of supervisorScope
        }

        try {
            movies.await()
        } catch (e: Exception) {
            println("Caught error: ${e.message}")
        }
        println("Character count: ${characters.await().total}")
    }
}

suspend fun showCancellation(api: TheOneApiClient) = coroutineScope {
    val job = launch(Dispatchers.Default) {
        var i = 0
        while (i < 10_000 && isActive) { // 'isActive' is the key here
            print("i: $i, fib: ${calculateFibonacci(i)},")
            i++
        }
    }
    delay(10) // Let it run briefly
    job.cancelAndJoin()
    println("Job cancelled successfully.")
}

suspend fun showBoundedConcurrency(api: TheOneApiClient) = coroutineScope {
    val semaphore = Semaphore(permits = 3)
    val results = mutableListOf<Deferred<Unit>>()

    repeat(10) { i ->
        results += async {
            semaphore.withPermit {
                println("Request $i starting...")
                api.getQuotes()
                println("Request $i finished.")
            }
        }
    }
    results.awaitAll()
}

fun calculateFibonacci(n: Int): Long {
    return if (n <= 1) n.toLong() else calculateFibonacci(n - 1) + calculateFibonacci(n - 2)
}

suspend fun showFanOutFanIn(api: TheOneApiClient) = coroutineScope {
    println("\n--- Pattern 6: Fan-out / Fan-in (Aggregating movie Quotes) ---")

    val response = api.getMovies(QueryParameters(limit = 10))
    val movies = response.docs.take(2)

    println("Fanning out to fetch quotes for: ${movies.joinToString { it.name }}")

    val deferredQuotes = movies.map { movie ->
        async {
            api.getMovieQuotes(movie.id, QueryParameters(limit = 5))
        }
    }

    val allQuotes = deferredQuotes.awaitAll().flatMap { it.docs }

    println("Fan-in complete: Flattened ${allQuotes.size} total quotes from ${movies.size} workers.")

    allQuotes.forEach { quote -> println(" > ${quote.dialog}") }
}