package org.example.backend.domain.follow.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.dto.*;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberService memberService;

    // 팔로우하기
    public ApiResponse<FollowResponseDto> follow(Long followerId, Long followeeId) {

        if (followerId.equals(followeeId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_FOLLOW);
        }
        // 이미 팔로우하고 있는지 확인
        if (followRepository.existsByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        // Member 엔티티 조회 (존재 여부 확인과 함께)
        Member follower = memberService.findByMemberId(followerId).orElseThrow(
            () -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));
        Member followee = memberService.findByMemberId(followeeId).orElseThrow(
            () -> new BusinessException(ErrorCode.FOLLOWEE_NOT_FOUND));

        Follow followEntity = Follow.builder()
            .follower(follower)
            .followee(followee)
            .build();

        Follow savedFollow = followRepository.save(followEntity);

        // 팔로우 완료 후 간단한 응답
        FollowResponseDto responseDto = FollowResponseDto.builder()
            .memberId(savedFollow.getFollowee().getMemberId())
            .nickname("") // 팔로우 완료 시에는 상세 정보 불필요
            .profileImage("")
            .build();

        return ApiResponse.ok("팔로우가 완료되었습니다.", responseDto);
    }

    public ApiResponse<Void> unfollow(Long followerId, Long followeeId) {

        if (!followRepository.existsByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId)) {
            throw new BusinessException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        followRepository.deleteByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId);
        return ApiResponse.ok("언팔로우가 완료되었습니다.");
    }


    @Transactional(readOnly = true)
    public ApiResponse<FollowListResponseDto> getFollowers(Long memberId) {
        if (memberService.notExistsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 팔로워 목록과 멤버 정보를 함께 조회 (Fetch Join)
        List<Follow> follows = followRepository.findFollowersWithMemberInfo(memberId);
        long totalCount = followRepository.countByFolloweeMemberId(memberId);

        List<FollowResponseDto> userDtos = follows.stream()
            .map(follow -> {
                Member follower = follow.getFollower();
                return FollowResponseDto.builder()
                    .memberId(follower.getMemberId())
                    .nickname(follower.getNickname())
                    .profileImage(follower.getProfileImage())
                    .build();
            })
            .collect(Collectors.toList());

        FollowListResponseDto responseDto = FollowListResponseDto.builder()
            .users(userDtos)
            .totalCount(totalCount)
            .build();

        return ApiResponse.ok("팔로워 목록을 조회했습니다.", responseDto);
    }

    // 팔로잉 목록 조회
    @Transactional(readOnly = true)
    public ApiResponse<FollowListResponseDto> getFollowings(Long memberId) {
        if (memberService.notExistsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 팔로잉 목록과 멤버 정보를 함께 조회 (Fetch Join)
        List<Follow> follows = followRepository.findFollowingsWithMemberInfo(memberId);
        long totalCount = followRepository.countByFollowerMemberId(memberId);

        List<FollowResponseDto> userDtos = follows.stream()
            .map(follow -> {
                Member followee = follow.getFollowee();
                return FollowResponseDto.builder()
                    .memberId(followee.getMemberId())
                    .nickname(followee.getNickname())
                    .profileImage(followee.getProfileImage())
                    .build();
            })
            .collect(Collectors.toList());

        FollowListResponseDto responseDto = FollowListResponseDto.builder()
            .users(userDtos)
            .totalCount(totalCount)
            .build();

        return ApiResponse.ok("팔로잉 목록을 조회했습니다.", responseDto);
    }

    // 팔로잉 여부 확인(추후에 게시글 조회 시 사용)
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return false;
        }
        if (followerId.equals(followeeId)) {
            return false; // 자기 자신은 팔로잉하지 않은 것으로 처리
        }
        return followRepository.existsByFollowerMemberIdAndFolloweeMemberId(followerId, followeeId);
    }

    public List<Long> findFolloweeIdsByFollower(Member member) {
        return followRepository.findFolloweeIdsByFollower(member);
    }

    public boolean existsByFollowerAndFollowee(Member member, Member author) {
        return  followRepository.existsByFollowerAndFollowee(member, author);
    }
}
