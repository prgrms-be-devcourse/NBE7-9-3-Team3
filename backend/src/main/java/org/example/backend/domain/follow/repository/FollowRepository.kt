package org.example.backend.domain.follow.repository

import org.example.backend.domain.follow.entity.Follow
import org.example.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FollowRepository : JpaRepository<Follow, Long> {
    // 사용자가 다른 사용자를 팔로우하고 있는지 확인
    fun existsByFollowerMemberIdAndFolloweeMemberId(followerId: Long, followeeId: Long): Boolean

    // 팔로우 관계 삭제
    fun deleteByFollowerMemberIdAndFolloweeMemberId(followerId: Long, followeeId: Long)

    // 팔로워 수 조회
    fun countByFolloweeMemberId(followeeId: Long): Long

    // 팔로잉 수 조회
    fun countByFollowerMemberId(followerId: Long): Long

    // 팔로워 목록과 멤버 정보를 함께 조회 (Fetch Join)
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.followee.memberId = :followeeId")
    fun findFollowersWithMemberInfo(@Param("followeeId") followeeId: Long): List<Follow>

    // 팔로잉 목록과 멤버 정보를 함께 조회 (Fetch Join)
    @Query("SELECT f FROM Follow f JOIN FETCH f.followee WHERE f.follower.memberId = :followerId")
    fun findFollowingsWithMemberInfo(@Param("followerId") followerId: Long): List<Follow>

    fun existsByFollowerAndFollowee(member: Member, author: Member): Boolean

    @Query("SELECT f.followee.memberId FROM Follow f WHERE f.follower = :member")
    fun findFolloweeIdsByFollower(@Param("member") member: Member): List<Long>
}