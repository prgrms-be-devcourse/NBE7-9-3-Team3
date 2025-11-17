package org.example.backend.global.response

import org.example.backend.global.exception.ErrorCode

class ApiResponse<T>(
    val resultCode: String,
    val msg: String?,
    val data: T? = null
) {
    constructor(resultCode: String, msg: String?) : this(resultCode, msg, null)

    companion object {
        // 성공 응답 생성
        @JvmStatic
        fun <T> ok(data: T): ApiResponse<T> {
            return ApiResponse("200", "성공", data)
        }

        @JvmStatic
        fun ok(message: String): ApiResponse<Void> {
            return ApiResponse("200", message, null)
        }

        @JvmStatic
        fun <T> ok(message: String, data: T): ApiResponse<T> {
            return ApiResponse("200", message, data)
        }

        // 에러 응답 생성
        @JvmStatic
        fun error(errorCode: ErrorCode): ApiResponse<Void> {
            return ApiResponse(errorCode.code, errorCode.message, null)
        }

        @JvmStatic
        fun <T> error(errorCode: ErrorCode, data: T?): ApiResponse<T?> {
            return ApiResponse(errorCode.code, errorCode.message, data)
        }
    }
}