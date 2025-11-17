package org.example.backend.global.image

data class PresignedUrlResponse(
    val presignedUrl: String,
    val fileUrl: String
)
