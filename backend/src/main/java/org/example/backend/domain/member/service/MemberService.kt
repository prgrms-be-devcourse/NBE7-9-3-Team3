package org.example.backend.domain.member.service

import org.example.backend.domain.follow.repository.FollowRepository
import org.example.backend.domain.member.dto.*
import org.example.backend.domain.member.dto.MemberEditResponseDto.Companion.from
import org.example.backend.domain.member.dto.MemberJoinResponseDto.Companion.from
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.image.ImageService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.response.ApiResponse.Companion.ok
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenService: AuthTokenService,
    private val followRepository: FollowRepository,
    private val imageService: ImageService
) {
    fun findById(id: Long): Member? {
        return memberRepository.findById(id).orElse(null)
    }

    fun findByEmail(email: String?): Member? {
        return memberRepository.findByEmail(email).orElse(null)
    }

    fun findByMemberId(memberId: Long?): Member? {
        return memberRepository.findByMemberId(memberId).orElse(null)
    }

    // 멤버 존재하지 않음 확인
    fun notExistsById(memberId: Long): Boolean {
        return !memberRepository.existsById(memberId)
    }

    private val currentMemberId: Long
        // 현재 인증된 사용자 ID 가져오기
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
            val userDetails = authentication.principal as CustomUserDetails
            return userDetails.member.memberId
                ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        }

    @Transactional
    fun create(email: String, password: String, nickname: String, profileImage: String?): Member {
        // 이메일 중복 체크
        if (memberRepository.findByEmail(email).isPresent) {
            throw BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE)
        }

        // 닉네임 중복 체크
        if (memberRepository.findByNickname(nickname).isPresent) {
            throw BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATE)
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(password)

        // 회원 생성
        val member = Member(email, encodedPassword, nickname, profileImage)

        return memberRepository.save(member)
    }

    @Transactional
    fun join(
        request: MemberJoinRequestDto,
        profileImageUrl: String?
    ): ApiResponse<MemberJoinResponseDto> {
        val savedMember = create(
            request.email ?: throw BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE),
            request.password ?: throw BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH),
            request.nickname ?: throw BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATE),
            profileImageUrl
        )

        val response = from(savedMember)
        return ok("회원가입이 완료되었습니다.", response)
    }

    fun login(request: MemberLoginRequestDto): ApiResponse<MemberLoginResponseDto> {
        val member: Member = memberRepository.findByEmail(request.email)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH)
        }

        val response = MemberLoginResponseDto(
            member.memberId,
            member.email,
            member.nickname,
            member.profileImage
        )
        return ok("로그인에 성공했습니다.", response)
    }

    // JWT 토큰을 별도로 생성하는 메서드
    fun generateAccessToken(member: Member): String {
        return authTokenService.genAccessToken(member)
    }

    @Transactional
    fun edit(
        request: MemberEditRequestDto,
        profileImageUrl: String?
    ): ApiResponse<MemberEditResponseDto> {
        // 현재 로그인한 사용자 조회
        val member: Member = memberRepository.findByMemberId(this.currentMemberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        // 현재 비밀번호 재확인
        if (!passwordEncoder.matches(request.currentPassword, member.password)) {
            throw BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH)
        }

        // 이메일이나 닉네임이 변경되는지 확인
        val emailChanged = member.email != request.email
        val nicknameChanged = member.nickname != request.nickname

        // 이메일 중복 체크 (현재 사용자와 다른 이메일인 경우)
        if (emailChanged) {
            if (memberRepository.findByEmail(request.email).isPresent) {
                throw BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE)
            }
        }

        // 닉네임 중복 체크 (현재 사용자와 다른 닉네임인 경우)
        if (nicknameChanged) {
            if (memberRepository.findByNickname(request.nickname).isPresent) {
                throw BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATE)
            }
        }

        // 새로운 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(
            request.newPassword ?: throw BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH)
        )

        // 프로필 이미지: 새로운 URL이 제공되면 업데이트, 없으면 기존 이미지 유지
        val finalProfileImageUrl =
            profileImageUrl?.takeIf { it.isNotEmpty() } ?: member.profileImage

        // 회원 정보 업데이트
        member.updateMemberInfo(
            request.email ?: throw BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE),
            encodedPassword,
            request.nickname ?: throw BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATE),
            finalProfileImageUrl
        )

        // 데이터베이스에 저장
        val updatedMember = memberRepository.save(member)

        // 토큰 정보가 변경된 경우 새로운 토큰 발급 (컨트롤러에서 처리)
        val newAccessToken = if (emailChanged || nicknameChanged) {
            authTokenService.genAccessToken(updatedMember)
        } else {
            null
        }

        // 응답 DTO 생성
        val response = from(updatedMember, newAccessToken)
        return ok("회원정보 수정에 성공했습니다.", response)
    }

    @Transactional
    fun myPage(): ApiResponse<MemberResponseDto> {
        // 현재 로그인한 사용자 조회
        val member: Member = memberRepository.findByMemberId(this.currentMemberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }
        val memberId = member.memberId ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val followerCount = followRepository.countByFolloweeMemberId(memberId)
        val followingCount = followRepository.countByFollowerMemberId(memberId)
        val response = MemberResponseDto(member, followerCount, followingCount)
        return ok("회원 정보 조회에 성공했습니다.", response)
    }

    @Transactional
    fun updateProfileImage(profileImageUrl: String?): ApiResponse<MemberEditResponseDto> {
        // 현재 로그인한 사용자 조회
        val member: Member = memberRepository.findByMemberId(this.currentMemberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val oldImageUrl = member.profileImage

        // 기존 이미지와 새 이미지가 다를 때만 S3에서 삭제
        if (!oldImageUrl.isNullOrEmpty() && oldImageUrl != profileImageUrl) {
            imageService.deleteFile(oldImageUrl)
        }

        // 회원 정보 업데이트 (프로필 이미지만)
        member.updateMemberInfo(
            member.email,
            member.password,
            member.nickname,
            profileImageUrl
        )

        // 데이터베이스에 저장
        val updatedMember = memberRepository.save(member)

        // 응답 DTO 생성
        val response = from(updatedMember, null)
        return ok("프로필 이미지가 성공적으로 업데이트되었습니다.", response)
    }

    @Transactional(readOnly = true)
    fun searchMembers(nickname: String?): ApiResponse<MemberSearchListResponseDto> {
        val currentMemberId = this.currentMemberId

        // N+1 문제 해결: 한 번의 쿼리로 회원 정보와 팔로우 상태를 함께 조회
        val results = memberRepository.findByNicknameContainingWithFollowStatus(
            nickname, currentMemberId
        )

        val members = results.mapNotNull { result ->
            if (result.isEmpty()) return@mapNotNull null
            val member = result[0] as? Member ?: return@mapNotNull null
            val isFollowing = result.getOrNull(1) as? Boolean ?: false
            MemberSearchResponseDto(
                memberId = member.memberId,
                nickname = member.nickname,
                profileImage = member.profileImage,
                isFollowing = isFollowing
            )
        }

        val response = MemberSearchListResponseDto(members)
        return ok("회원 검색에 성공했습니다.", response)
    }
}
