package org.example.backend.global.exception

import org.example.backend.global.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    // 비즈니스 로직 예외 처리 (BusinessException)
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        val errorCode = ex.errorCode

        // HTTP 상태 코드에 따라 로깅 레벨 구분
        if (errorCode.status.is5xxServerError) {
            // 5xx: 서버 오류 - error 레벨 (스택 트레이스 포함)
            log.error(
                "Business exception occurred: code={}, message={}",
                errorCode.code,
                errorCode.message,
                ex
            )
        } else {
            // 4xx: 클라이언트 오류 - warn 레벨 (스택 트레이스 제외)
            log.warn(
                "Business exception occurred: code={}, message={}",
                errorCode.code,
                errorCode.message
            )
        }

        val response = ApiResponse.error(errorCode)

        return ResponseEntity(response, errorCode.status)
    }

    // 기존 ServiceException 호환성 유지
    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(e: ServiceException): ResponseEntity<ApiResponse<Unit>> {
        log.warn(
            "ServiceException occurred: code={}, message={}",
            e.resultCode,
            e.msg
        )
        val response = e.getApiResponse()
        return ResponseEntity(response, e.httpStatus)
    }

    // 요청 유효성 검사 실패 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<MutableMap<String, String>?>> {
        // 필드별로 에러 메시지를 Map으로 수집
        val errors: MutableMap<String, String> = HashMap()

        ex.bindingResult.fieldErrors.forEach { error: FieldError ->
            val fieldName = error.field
            val errorMessage = error.defaultMessage ?: "검증에 실패했습니다"

            // 동일한 필드에 여러 에러가 있을 경우 ", "로 연결
            errors.merge(
                fieldName,
                errorMessage
            ) { existing: String, newMsg: String -> "$existing, $newMsg" }
        }

        log.warn("Validation failed: {}", errors)

        val response = ApiResponse.error(
            ErrorCode.VALIDATION_FAILED, errors
        )

        return ResponseEntity(
            response,
            ErrorCode.VALIDATION_FAILED.status
        )
    }

    // 잘못된 요청 형식
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Invalid request format: {}", ex.message)

        val response = ApiResponse.error(ErrorCode.BAD_REQUEST_FORMAT)
        return ResponseEntity(
            response,
            ErrorCode.BAD_REQUEST_FORMAT.status
        )
    }

    // NoSuchElementException 처리
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("NoSuchElementException: {}", ex.message)

        val response = ApiResponse.error(ErrorCode.NOT_FOUND_DATA)
        return ResponseEntity(response, ErrorCode.NOT_FOUND_DATA.status)
    }

    // SecurityException 처리
    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("SecurityException: {}", ex.message)

        val response = ApiResponse.error(ErrorCode.FORBIDDEN_ACCESS)
        return ResponseEntity(response, ErrorCode.FORBIDDEN_ACCESS.status)
    }

    // 정적 리소스 없음 처리 (Swagger UI 관련)
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ApiResponse<Unit>> {
        val resourcePath = ex.resourcePath

        // Swagger UI 관련 리소스는 조용히 처리 (로그 레벨 낮춤)
        if (resourcePath.contains("swagger-ui") ||
            resourcePath.contains("webjars") ||
            resourcePath.contains("api-docs")
        ) {
            log.debug(
                "Static resource not found (Swagger UI): {}",
                resourcePath
            )
        } else {
            log.warn("Static resource not found: {}", resourcePath)
        }

        val response = ApiResponse.error(ErrorCode.NOT_FOUND_DATA)
        return ResponseEntity(response, ErrorCode.NOT_FOUND_DATA.status)
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("Unexpected exception occurred", ex)

        val response = ApiResponse.error(ErrorCode.INTERNAL_ERROR)

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}