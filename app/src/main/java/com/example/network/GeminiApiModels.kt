package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<ContentItem>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: ContentItem? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "parts") val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    @Json(name = "thinkingLevel") val thinkingLevel: String? = "medium"
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json",
    @Json(name = "thinkingConfig") val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<CandidateItem>? = null,
    @Json(name = "error") val error: GeminiErrorResponse? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @Json(name = "content") val content: ContentItem? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)
