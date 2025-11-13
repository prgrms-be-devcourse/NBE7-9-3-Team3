package org.example.backend.domain.follow.repository;

import java.util.List;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 사용자가 다른 사용자를 팔로우하고 있는지 확인
    boolean existsByFollowerMemberIdAndFolloweeMemberId(Long followerId, Long followeeId);
    
    // 팔로우 관계 삭제
    void deleteByFollowerMemberIdAndFolloweeMemberId(Long followerId, Long followeeId);

    // 팔로워 수 조회
    long countByFolloweeMemberId(Long followeeId);

    // 팔로잉 수 조회
    long countByFollowerMemberId(Long followerId);

    // 팔로워 목록과 멤버 정보를 함께 조회 (Fetch Join)
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.followee.memberId = :followeeId")
    List<Follow> findFollowersWithMemberInfo(@Param("followeeId") Long followeeId);

    // 팔로잉 목록과 멤버 정보를 함께 조회 (Fetch Join)
    @Query("SELECT f FROM Follow f JOIN FETCH f.followee WHERE f.follower.memberId = :followerId")
    List<Follow> findFollowingsWithMemberInfo(@Param("followerId") Long followerId);

    boolean existsByFollowerAndFollowee(Member member, Member member2);

    @Query("SELECT f.followee.memberId FROM Follow f WHERE f.follower = :member")
    List<Long> findFolloweeIdsByFollower(@Param("member") Member member);}