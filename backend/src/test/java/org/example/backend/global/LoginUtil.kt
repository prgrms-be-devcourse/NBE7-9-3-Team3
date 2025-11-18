package org.example.backend.global

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder

// 테스트에서 Repository를 직접 사용하여 빠르게 회원 생성 및 로그인 토큰을 발급하는 유틸리티 클래스
class LoginUtil(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenService: AuthTokenService
) {
    // 회원을 생성하고 Member 엔티티를 반환하는 메서드
    fun createMember(
        email: String,
        password: String?,
        nickname: String,
        profileImage: String?
    ): Member {
        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(password)

        // 회원 생성
        val member = Member(email, encodedPassword, nickname, profileImage)

        return memberRepository.save<Member>(member)
    }

    // 회원을 생성하고 JWT 토큰을 반환하는 메서드
    fun createMemberAndGetToken(
        email: String,
        password: String?,
        nickname: String,
        profileImage: String?
    ): String {
        val savedMember = createMember(email, password, nickname, profileImage)
        // JWT 토큰 생성
        return authTokenService.genAccessToken(savedMember)
    }

    // 이메일로 저장된 Member 엔티티를 조회하는 메서드
    fun getMemberByEmail(email: String): Member {
        val member = memberRepository.findByEmail(email)
        if (member == null) {
            throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        }
        return member
    }
}

