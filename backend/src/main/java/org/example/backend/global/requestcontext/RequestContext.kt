package org.example.backend.global.requestcontext

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.backend.domain.member.entity.Member
import org.example.backend.global.exception.ServiceException
import org.example.backend.global.security.CustomUserDetails
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class RequestContext {
    private val request: HttpServletRequest?
        get() = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    private val response: HttpServletResponse?
        get() = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.response

    fun setHeader(name: String?, value: String?) {
        response?.setHeader(name, value)
    }

    fun getHeader(name: String?, defaultValue: String?): String? {
        val headerValue = request?.getHeader(name)
        return if (headerValue != null && headerValue.isNotBlank()) {
            headerValue
        } else {
            defaultValue
        }
    }

    fun getCookieValue(name: String?, defaultValue: String?): String? {
        val cookies = request?.cookies ?: return defaultValue

        return cookies
            .firstOrNull { it.name == name }
            ?.takeIf { it.value.isNotBlank() }
            ?.value
            ?: defaultValue
    }

    fun setCookie(name: String?, value: String?) {
        val cookieValue = value ?: ""
        val cookie = Cookie(name, cookieValue)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.domain = "localhost"
        cookie.secure = true
        cookie.setAttribute("SameSite", "Strict")

        // 값이 없다면 해당 쿠키변수를 삭제하라는 뜻
        if (cookieValue.isBlank()) {
            cookie.maxAge = 0
        }

        response?.addCookie(cookie)
    }

    fun deleteCookie(name: String?) {
        setCookie(name, null)
    }

    val currentMember: Member
        // 현재 인증된 사용자 정보 가져오기
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null || authentication.principal !is CustomUserDetails) {
                throw ServiceException(
                    "401",
                    "인증되지 않은 사용자입니다.",
                    HttpStatus.UNAUTHORIZED
                )
            }

            val userDetails = authentication.principal as CustomUserDetails
            return userDetails.member
        }

    val currentMemberId: Long?
        // 편의 메서드들
        get() = currentMember.memberId
}
