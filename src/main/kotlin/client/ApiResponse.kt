package com.tohid.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val docs: List<T>,
    val total: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val page: Int? = null,
    val pages: Int? = null
    )

@Serializable
data class Book(
    @SerialName("_id") val id: String,
    val name: String
)

@Serializable
data class Character(
    @SerialName("_id") val id: String,
    val height: String? = null,
    val race: String? = null,
    val gender: String? = null,
    val birth: String? = null,
    val spouse: String? = null,
    val death: String? = null,
    val realm: String? = null,
    val hair: String? = null,
    val name: String,
    val wikiUrl: String? = null
)

@Serializable
data class Quote(
    @SerialName("_id") val id: String,
    val dialog: String,
    val movie: String, // Movie ID
    val character: String, // Character ID
)

@Serializable
data class Chapter(
    @SerialName("_id") val id: String,
    val chapterName: String,
    val book: String? = null, // Book ID
)

@Serializable
data class Movie(
    @SerialName("_id") val id: String,
    val name: String,
    val runtimeInMinutes: Int? = null,
    val budgetInMillions: Int? = null,
    val boxOfficeRevenueInMillions: Double? = null,
    val academyAwardNominations: Int? = null,
    val academyAwardWins: Int? = null,
    val rottenTomatoesScore: Double? = null
)
