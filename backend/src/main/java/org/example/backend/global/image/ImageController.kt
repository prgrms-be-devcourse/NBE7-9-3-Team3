package org.example.backend.global.image

import io.swagger.v3.oas.annotations.Operation
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService
) {
    @Operation(summary = "Presigned URL 발급", description = "S3 직접 업로드를 위한 Presigned URL을 생성합니다.")
    @PostMapping("/upload")
    fun generatePresignedUrl(
        @RequestBody request: PresignedUrlRequest
    ): ApiResponse<PresignedUrlResponse> {
        val response = imageService.createPresignedUrl(
            request.fileName,
            request.directory
        )

        return ApiResponse.ok("Presigned URL 생성 성공", response)
    }
}
