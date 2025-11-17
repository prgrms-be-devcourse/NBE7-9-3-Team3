package org.example.backend.global.image

data class PresignedUrlRequest(
    val fileName: String,
    val directory: String?
)
