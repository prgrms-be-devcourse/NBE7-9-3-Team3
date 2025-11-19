package org.example.backend.domain.member.service

import org.example.backend.domain.member.entity.Member
import org.example.backend.global.redis.RefreshTokenRepository
import org.example.backend.global.security.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    @Value("\${custom.jwt.secretPattern}")
    lateinit var secretPattern: String

    @Value("\${custom.jwt.expireSeconds}")
    var expireSeconds: Long = 0

    @Value("\${custom.jwt.shortExpireSeconds:600}") // 기본값 10분
    var shortExpireSeconds: Long = 0

    @Value("\${custom.jwt.refreshExpireSeconds:604800}") // 기본값 7일 (초 단위)
    var refreshExpireSeconds: Long = 604800

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

    //리프레시 토큰 생성 및 Redis에 저장
    fun genRefreshToken(member: Member): String {
        val memberId = member.memberId ?: throw IllegalStateException("Member ID is null")
        val refreshToken = UUID.randomUUID().toString()

        // Redis에 리프레시 토큰 저장 (memberId를 값으로 저장)
        refreshTokenRepository.save(refreshToken, memberId, refreshExpireSeconds)

        return refreshToken
    }

    //리프레시 토큰 검증
    fun validateRefreshToken(refreshToken: String): Long? {
        return refreshTokenRepository.findMemberIdByToken(refreshToken)
    }

    //리프레시 토큰 삭제 (로그아웃 시 사용)
    fun deleteRefreshToken(refreshToken: String) {
        refreshTokenRepository.delete(refreshToken)
    }

    //특정 회원의 모든 리프레시 토큰 삭제 (보안 강화용)
    fun deleteAllRefreshTokensByMemberId(memberId: Long) {
        refreshTokenRepository.deleteAllByMemberId(memberId)
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