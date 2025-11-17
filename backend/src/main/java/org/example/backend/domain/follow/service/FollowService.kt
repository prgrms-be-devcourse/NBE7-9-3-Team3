package org.example.backend.domain.follow.service

import org.example.backend.domain.follow.dto.FollowListResponseDto
import org.example.backend.domain.follow.dto.FollowResponseDto
import org.example.backend.domain.follow.entity.Follow
import org.example.backend.domain.follow.repository.FollowRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.service.MemberService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.response.ApiResponse.Companion.ok
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FollowService(
    private val followRepository: FollowRepository,
    private val memberService: MemberService
) {

    // 팔로우하기
    fun follow(followerId: Long, followeeId: Long): ApiResponse<FollowResponseDto> {
        if (followerId == followeeId) {
            throw BusinessException(ErrorCode.FOLLOW_SELF_FOLLOW)
        }

        // 이미 팔로우하고 있는지 확인
        if (followRepository.existsByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId)) {
            throw BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS)
        }

        // Member 엔티티 조회 (존재 여부 확인과 함께)
        val follower = memberService.findByMemberId(followerId)
            .orElseThrow { BusinessException(ErrorCode.FOLLOW_NOT_FOUND) }
        val followee = memberService.findByMemberId(followeeId)
            .orElseThrow { BusinessException(ErrorCode.FOLLOWEE_NOT_FOUND) }

        val followEntity = Follow(
            follower = follower,
            followee = followee
        )

        val savedFollow = followRepository.save(followEntity)

        // 팔로우 완료 후 간단한 응답
        val responseDto = FollowResponseDto(
            memberId = savedFollow.followee.memberId,
            nickname = "", // 팔로우 완료 시에는 상세 정보 불필요
            profileImage = ""
        )

        return ok("팔로우가 완료되었습니다.", responseDto)
    }

    fun unfollow(followerId: Long, followeeId: Long): ApiResponse<Void> {
        if (!followRepository.existsByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId)) {
            throw BusinessException(ErrorCode.FOLLOW_NOT_FOUND)
        }

        followRepository.deleteByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId)
        return ApiResponse.ok("언팔로우가 완료되었습니다.")
    }


    @Transactional(readOnly = true)
    fun getFollowers(memberId: Long?): ApiResponse<FollowListResponseDto> {
        if (memberService.notExistsById(
                memberId ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
            )
        ) {
            throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        }

        // 팔로워 목록과 멤버 정보를 함께 조회 (Fetch Join)
        val follows = followRepository.findFollowersWithMemberInfo(memberId)
        val totalCount = followRepository.countByFolloweeMemberId(memberId)

        val userDtos = follows.map { follow ->
            FollowResponseDto(
                memberId = follow.follower.memberId,
                nickname = follow.follower.nickname,
                profileImage = follow.follower.profileImage
            )
        }

        val responseDto = FollowListResponseDto(
            users = userDtos,
            totalCount = totalCount
        )

        return ok("팔로워 목록을 조회했습니다.", responseDto)
    }

    // 팔로잉 목록 조회
    @Transactional(readOnly = true)
    fun getFollowings(memberId: Long?): ApiResponse<FollowListResponseDto> {
        if (memberService.notExistsById(
                memberId ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
            )
        ) {
            throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        }

        // 팔로잉 목록과 멤버 정보를 함께 조회 (Fetch Join)
        val follows = followRepository.findFollowingsWithMemberInfo(memberId)
        val totalCount = followRepository.countByFollowerMemberId(memberId)

        val userDtos = follows.map { follow ->
            FollowResponseDto(
                memberId = follow.followee.memberId,
                nickname = follow.followee.nickname,
                profileImage = follow.followee.profileImage
            )
        }

        val responseDto = FollowListResponseDto(
            users = userDtos,
            totalCount = totalCount
        )

        return ok("팔로잉 목록을 조회했습니다.", responseDto)
    }

    fun findFolloweeIdsByFollower(member: Member): List<Long> {
        return followRepository.findFolloweeIdsByFollower(member)
    }

    fun existsByFollowerAndFollowee(member: Member, author: Member): Boolean {
        return followRepository.existsByFollowerAndFollowee(member, author)
    }

}
