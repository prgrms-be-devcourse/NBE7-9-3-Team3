package org.example.backend.domain.member.service

import org.assertj.core.api.Assertions
import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(
    properties = [
        "custom.jwt.secretPattern=testSecretKey123456789012345678901234567890",
        "custom.jwt.expireSeconds=86400",
        "custom.jwt.shortExpireSeconds=600",
        "custom.jwt.refreshExpireSeconds=604800"
    ]
)
class AuthTokenServiceTest {

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    @DisplayName("t1: 액세스 토큰 생성 성공")
    fun t1() {
        // given
        val member = Member(
            "test@test.com",
            passwordEncoder.encode("password123"),
            "testuser",
            null
        )
        val savedMember = memberRepository.save(member)

        // when
        val token = authTokenService.genAccessToken(savedMember)

        // then
        Assertions.assertThat(token).isNotNull()
        Assertions.assertThat(token).isNotEmpty()

        // 토큰에서 payload 추출하여 검증
        val payload = authTokenService.payloadOrNull(token)
        Assertions.assertThat(payload).isNotNull()
        requireNotNull(payload)
        Assertions.assertThat(payload["id"]).isEqualTo(savedMember.memberId)
        Assertions.assertThat(payload["email"]).isEqualTo(savedMember.email)
        Assertions.assertThat(payload["nickname"]).isEqualTo(savedMember.nickname)
    }

    @Test
    @DisplayName("t2: 임시 토큰 생성 성공")
    fun t2() {
        // given
        val member = Member(
            "temp@test.com",
            passwordEncoder.encode("password123"),
            "tempuser",
            null
        )
        val savedMember = memberRepository.save(member)

        // when
        val tempToken = authTokenService.genTempToken(savedMember)

        // then
        Assertions.assertThat(tempToken).isNotNull()
        Assertions.assertThat(tempToken).isNotEmpty()

        // 토큰에서 payload 추출하여 검증
        val payload = authTokenService.payloadOrNull(tempToken)
        Assertions.assertThat(payload).isNotNull()
        requireNotNull(payload)
        Assertions.assertThat(payload["id"]).isEqualTo(savedMember.memberId)
        Assertions.assertThat(payload["email"]).isEqualTo(savedMember.email)
        Assertions.assertThat(payload["nickname"]).isEqualTo(savedMember.nickname)
    }

    @Test
    @DisplayName("t3: 유효한 토큰에서 payload 추출 성공")
    fun t3() {
        // given
        val member = Member(
            "payload@test.com",
            passwordEncoder.encode("password123"),
            "payloaduser",
            null
        )
        val savedMember = memberRepository.save(member)
        val token = authTokenService.genAccessToken(savedMember)

        // when
        val payload = authTokenService.payloadOrNull(token)

        // then
        Assertions.assertThat(payload).isNotNull()
        requireNotNull(payload)
        Assertions.assertThat(payload["id"]).isEqualTo(savedMember.memberId)
        Assertions.assertThat(payload["email"]).isEqualTo(savedMember.email)
        Assertions.assertThat(payload["nickname"]).isEqualTo(savedMember.nickname)
    }

    @Test
    @DisplayName("t4: 유효하지 않은 토큰에서 payload 추출 시 null 반환")
    fun t4() {
        // given
        val invalidToken = "invalid.token.string"

        // when
        val payload = authTokenService.payloadOrNull(invalidToken)

        // then
        Assertions.assertThat(payload).isNull()
    }

    @Test
    @DisplayName("t5: 빈 문자열 토큰에서 payload 추출 시 null 반환")
    fun t5() {
        // given
        val emptyToken = ""

        // when
        val payload = authTokenService.payloadOrNull(emptyToken)

        // then
        Assertions.assertThat(payload).isNull()
    }

    @Test
    @DisplayName("t6: 다른 시크릿 키로 생성된 토큰에서 payload 추출 시 null 반환")
    fun t6() {
        // given
        // 다른 시크릿 키로 토큰을 생성하는 것은 JwtUtil을 직접 사용해야 하므로
        // 여기서는 간단히 잘못된 형식의 토큰을 테스트합니다.
        val wrongToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        // when
        val payload = authTokenService.payloadOrNull(wrongToken)

        // then
        Assertions.assertThat(payload).isNull()
    }

    @Test
    @DisplayName("t7: 액세스 토큰과 임시 토큰의 payload 내용이 동일한지 확인")
    fun t7() {
        // given
        val member = Member(
            "compare@test.com",
            passwordEncoder.encode("password123"),
            "compareuser",
            null
        )
        val savedMember = memberRepository.save(member)

        // when
        val accessToken = authTokenService.genAccessToken(savedMember)
        val tempToken = authTokenService.genTempToken(savedMember)

        // then
        val accessPayload = authTokenService.payloadOrNull(accessToken)
        val tempPayload = authTokenService.payloadOrNull(tempToken)

        Assertions.assertThat(accessPayload).isNotNull()
        Assertions.assertThat(tempPayload).isNotNull()

        requireNotNull(accessPayload)
        requireNotNull(tempPayload)

        // payload 내용이 동일한지 확인
        Assertions.assertThat(accessPayload["id"]).isEqualTo(tempPayload["id"])
        Assertions.assertThat(accessPayload["email"]).isEqualTo(tempPayload["email"])
        Assertions.assertThat(accessPayload["nickname"]).isEqualTo(tempPayload["nickname"])
    }

    @Test
    @DisplayName("t8: 리프레시 토큰 생성 성공")
    fun t8() {
        // given
        val member = Member(
            "refresh@test.com",
            passwordEncoder.encode("password123"),
            "refreshuser",
            null
        )
        val savedMember = memberRepository.save(member)

        // when
        val refreshToken = authTokenService.genRefreshToken(savedMember)

        // then
        Assertions.assertThat(refreshToken).isNotNull()
        Assertions.assertThat(refreshToken).isNotEmpty()

        // Redis에 저장되었는지 확인
        val memberId = authTokenService.validateRefreshToken(refreshToken)
        Assertions.assertThat(memberId).isNotNull()
        Assertions.assertThat(memberId).isEqualTo(savedMember.memberId)
    }

    @Test
    @DisplayName("t9: 리프레시 토큰 검증 성공")
    fun t9() {
        // given
        val member = Member(
            "validate@test.com",
            passwordEncoder.encode("password123"),
            "validateuser",
            null
        )
        val savedMember = memberRepository.save(member)
        val refreshToken = authTokenService.genRefreshToken(savedMember)

        // when
        val memberId = authTokenService.validateRefreshToken(refreshToken)

        // then
        Assertions.assertThat(memberId).isNotNull()
        Assertions.assertThat(memberId).isEqualTo(savedMember.memberId)
    }

    @Test
    @DisplayName("t10: 유효하지 않은 리프레시 토큰 검증 시 null 반환")
    fun t10() {
        // given
        val invalidRefreshToken = "invalid-refresh-token"

        // when
        val memberId = authTokenService.validateRefreshToken(invalidRefreshToken)

        // then
        Assertions.assertThat(memberId).isNull()
    }

    @Test
    @DisplayName("t11: 리프레시 토큰 삭제 성공")
    fun t11() {
        // given
        val member = Member(
            "delete@test.com",
            passwordEncoder.encode("password123"),
            "deleteuser",
            null
        )
        val savedMember = memberRepository.save(member)
        val refreshToken = authTokenService.genRefreshToken(savedMember)

        // 토큰이 존재하는지 확인
        val memberIdBefore = authTokenService.validateRefreshToken(refreshToken)
        Assertions.assertThat(memberIdBefore).isNotNull()

        // when
        authTokenService.deleteRefreshToken(refreshToken)

        // then
        val memberIdAfter = authTokenService.validateRefreshToken(refreshToken)
        Assertions.assertThat(memberIdAfter).isNull()
    }

    @Test
    @DisplayName("t12: 특정 회원의 모든 리프레시 토큰 삭제 성공")
    fun t12() {
        // given
        val member = Member(
            "deleteall@test.com",
            passwordEncoder.encode("password123"),
            "deletealluser",
            null
        )
        val savedMember = memberRepository.save(member)

        // 여러 리프레시 토큰 생성
        val refreshToken1 = authTokenService.genRefreshToken(savedMember)
        val refreshToken2 = authTokenService.genRefreshToken(savedMember)
        val refreshToken3 = authTokenService.genRefreshToken(savedMember)

        // 모든 토큰이 존재하는지 확인
        Assertions.assertThat(authTokenService.validateRefreshToken(refreshToken1)).isNotNull()
        Assertions.assertThat(authTokenService.validateRefreshToken(refreshToken2)).isNotNull()
        Assertions.assertThat(authTokenService.validateRefreshToken(refreshToken3)).isNotNull()

        // when
        authTokenService.deleteAllRefreshTokensByMemberId(savedMember.memberId!!)

        // then
        Assertions.assertThat(authTokenService.validateRefreshToken(refreshToken1)).isNull()
        Assertions.assertThat(authTokenService.validateRefreshToken(refreshToken2)).isNull()
        Assertions.assertThat(authTokenService.validateRefreshToken(refreshToken3)).isNull()
    }
}

