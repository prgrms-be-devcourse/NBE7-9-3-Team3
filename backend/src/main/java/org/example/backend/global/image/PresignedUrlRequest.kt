package org.example.backend.global.image

data class PresignedUrlRequest(
    @JvmField val fileName: String,
    @JvmField val directory: String?
)
