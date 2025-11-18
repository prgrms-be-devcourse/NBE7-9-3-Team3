package org.example.backend.domain.member.service

import org.example.backend.domain.member.entity.Member
import org.example.backend.global.security.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService {
    @Value("\${custom.jwt.secretPattern}")
    lateinit var secretPattern: String

    @Value("\${custom.jwt.expireSeconds}")
    var expireSeconds: Long = 0

    @Value("\${custom.jwt.shortExpireSeconds:600}") // 기본값 10분
    var shortExpireSeconds: Long = 0

    fun genAccessToken(member: Member): String {
        return JwtUtil.toString(
            secretPattern,
            expireSeconds,
            mutableMapOf(
                "id" to (member.memberId ?: throw IllegalStateException("Member ID is null")),
                "email" to member.email,
                "nickname" to member.nickname
            )
        )
    }

    // 웹소켓 연결용 임시 토큰 (10분)
    fun genTempToken(member: Member): String {
        return JwtUtil.toString(
            secretPattern,
            shortExpireSeconds,
            mutableMapOf(
                "id" to (member.memberId ?: throw IllegalStateException("Member ID is null")),
                "email" to member.email,
                "nickname" to member.nickname
            )
        )
    }

    fun payloadOrNull(jwt: String): Map<String, Any>? {
        val payload = JwtUtil.payloadOrNull(jwt, secretPattern) ?: return null

        val idNo = payload["id"] as? Number ?: return null
        val id = idNo.toLong()
        val email = payload["email"] as? String ?: return null
        val nickname = payload["nickname"] as? String ?: return null

        return mapOf("id" to id, "email" to email, "nickname" to nickname)
    }
}