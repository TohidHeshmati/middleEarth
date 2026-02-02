package com.tohid.client

import com.tohid.Dashboard
import com.tohid.THE_ONE_API_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

data class QueryParameters(
    val limit: Int? = null,
    val page: Int? = null,
    val sort: String? = null,
    val filters: Map<String, String> = emptyMap()
)

class TheOneApiClient(private val token: String) : AutoCloseable {

    private val client = HttpClient(CIO) {
        defaultRequest {
            url(THE_ONE_API_BASE_URL)
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Logging) {
            level = LogLevel.INFO
        }
    }

    private fun HttpRequestBuilder.applyQueryParams(params: QueryParameters) {
        params.limit?.let { parameter("limit", it) }
        params.page?.let { parameter("page", it) }
        params.sort?.let { parameter("sort", it) }
        params.filters.forEach { (k, v) -> parameter(k, v) }
    }

    // --- Books ---
    suspend fun getBooks(params: QueryParameters = QueryParameters()) =
        client.get("book") { applyQueryParams(params) }.body<ApiResponse<Book>>()

    suspend fun getBook(id: String) =
        client.get("book/$id").body<ApiResponse<Book>>()

    suspend fun getBookChapters(bookId: String, params: QueryParameters = QueryParameters()) =
        client.get("book/$bookId/chapter") { applyQueryParams(params) }.body<ApiResponse<Chapter>>()

    // --- Movies ---
    suspend fun getMovies(params: QueryParameters = QueryParameters()) =
        client.get("movie") { applyQueryParams(params) }.body<ApiResponse<Movie>>()

    suspend fun getMovie(id: String) =
        client.get("movie/$id").body<ApiResponse<Movie>>()

    suspend fun getMovieQuotes(movieId: String, params: QueryParameters = QueryParameters()) =
        client.get("movie/$movieId/quote") { applyQueryParams(params) }.body<ApiResponse<Quote>>()

    // --- Characters ---
    suspend fun getCharacters(params: QueryParameters = QueryParameters()) =
        client.get("character") { applyQueryParams(params) }.body<ApiResponse<Character>>()

    suspend fun getCharacter(id: String) =
        client.get("character/$id").body<ApiResponse<Character>>()

    suspend fun getCharacterQuotes(characterId: String, params: QueryParameters = QueryParameters()) =
        client.get("character/$characterId/quote") { applyQueryParams(params) }.body<ApiResponse<Quote>>()

    // --- Quotes ---
    suspend fun getQuotes(params: QueryParameters = QueryParameters()) =
        client.get("quote") { applyQueryParams(params) }.body<ApiResponse<Quote>>()

    suspend fun getRandomQuote(params: QueryParameters = QueryParameters()) =
        client.get("quotes/random") { applyQueryParams(params) }.body<Quote>()

    suspend fun getQuote(id: String) =
        client.get("quote/$id").body<ApiResponse<Quote>>()

    // --- Chapters ---
    suspend fun getChapters(params: QueryParameters = QueryParameters()) =
        client.get("chapter") { applyQueryParams(params) }.body<ApiResponse<Chapter>>()

    suspend fun getChapter(id: String) =
        client.get("chapter/$id").body<ApiResponse<Chapter>>()




    suspend fun getMovieDataStrict(movieId: String) = coroutineScope {
        val movie = async {
            println("Fetching Movie...")
            getMovie(movieId)
        }
        val quotes = async {
            println("Fetching Quotes...")
            getMovieQuotes(movieId)
        }

        val dashboard = Dashboard("MIDDLE-EARTH MOVIE INSIGHTS")
        dashboard.addComponent("Movie", listOf(movie.await(), quotes.await()))
        dashboard.render()
    }

    suspend fun prepareLazyQuote() = coroutineScope {
        val lazyQuote = async(start = CoroutineStart.LAZY) {
            println("Requesting from API now...")
            getRandomQuote()
        }
        println("User clicked button!")
        val result = lazyQuote.await()
    }

    override fun close() = client.close()
}