package org.example.backend.global.exception

import org.example.backend.global.response.ApiResponse
import org.springframework.http.HttpStatus

class ServiceException(
    val resultCode: String,
    val msg: String,
    val httpStatus: HttpStatus
) : RuntimeException(
    "$resultCode : $msg"
) {
    fun getApiResponse(): ApiResponse<Void?> {
        return ApiResponse(resultCode, msg)
    }
}
