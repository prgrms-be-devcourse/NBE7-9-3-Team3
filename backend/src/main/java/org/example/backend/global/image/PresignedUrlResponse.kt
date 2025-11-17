package org.example.backend.global.image

data class PresignedUrlResponse(
    @JvmField val presignedUrl: String,
    @JvmField val fileUrl: String
)
