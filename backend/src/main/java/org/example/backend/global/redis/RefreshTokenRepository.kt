package org.example.backend.global.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RefreshTokenRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refresh:token:"
        private const val MEMBER_TOKEN_PREFIX = "member:refresh:"
    }

    /**
     * 리프레시 토큰을 Redis에 저장
     * @param refreshToken 리프레시 토큰 (UUID)
     * @param memberId 회원 ID
     * @param expireSeconds 만료 시간 (초)
     */
    fun save(refreshToken: String, memberId: Long, expireSeconds: Long) {
        val tokenKey = "$REFRESH_TOKEN_PREFIX$refreshToken"
        val memberKey = "$MEMBER_TOKEN_PREFIX$memberId"

        // 리프레시 토큰 -> memberId 매핑 저장
        redisTemplate.opsForValue()
            .set(tokenKey, memberId.toString(), expireSeconds, TimeUnit.SECONDS)

        // memberId -> 리프레시 토큰 목록에 추가 (한 회원이 여러 기기에서 로그인 가능)
        redisTemplate.opsForSet().add(memberKey, refreshToken)
        redisTemplate.expire(memberKey, expireSeconds, TimeUnit.SECONDS)
    }

    /**
     * 리프레시 토큰으로 memberId 조회
     * @param refreshToken 리프레시 토큰
     * @return memberId (존재하는 경우), null (존재하지 않는 경우)
     */
    fun findMemberIdByToken(refreshToken: String): Long? {
        val tokenKey = "$REFRESH_TOKEN_PREFIX$refreshToken"
        val memberIdStr = redisTemplate.opsForValue().get(tokenKey) ?: return null
        return memberIdStr.toLongOrNull()
    }

    /**
     * 리프레시 토큰 삭제
     * @param refreshToken 리프레시 토큰
     */
    fun delete(refreshToken: String) {
        val tokenKey = "$REFRESH_TOKEN_PREFIX$refreshToken"
        val memberId = findMemberIdByToken(refreshToken)

        // 토큰 삭제
        redisTemplate.delete(tokenKey)

        // 회원의 토큰 목록에서도 제거
        memberId?.let {
            val memberKey = "$MEMBER_TOKEN_PREFIX$it"
            redisTemplate.opsForSet().remove(memberKey, refreshToken)
        }
    }

    /**
     * 특정 회원의 모든 리프레시 토큰 삭제
     * @param memberId 회원 ID
     */
    fun deleteAllByMemberId(memberId: Long) {
        val memberKey = "$MEMBER_TOKEN_PREFIX$memberId"
        val tokens = redisTemplate.opsForSet().members(memberKey) ?: return

        // 모든 리프레시 토큰 삭제
        tokens.forEach { token ->
            val tokenKey = "$REFRESH_TOKEN_PREFIX$token"
            redisTemplate.delete(tokenKey)
        }

        // 회원의 토큰 목록도 삭제
        redisTemplate.delete(memberKey)
    }

}

