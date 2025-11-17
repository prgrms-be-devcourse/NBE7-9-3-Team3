package org.example.backend.global.exception

class BusinessException(val errorCode: ErrorCode) : RuntimeException(
    errorCode.message
)
