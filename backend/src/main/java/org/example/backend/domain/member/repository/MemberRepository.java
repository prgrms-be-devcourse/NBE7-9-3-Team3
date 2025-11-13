package org.example.backend.domain.member.repository;

import org.example.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findByMemberId(Long memberId);

    // 닉네임으로 회원 검색 + 팔로우 관계 정보를 한 번에 조회 (N+1 문제 해결)
    @Query("SELECT m, CASE WHEN f.id IS NOT NULL THEN true ELSE false END " +
           "FROM Member m " +
           "LEFT JOIN Follow f ON f.follower.memberId = :currentMemberId AND f.followee.memberId = m.memberId " +
           "WHERE m.nickname LIKE %:nickname% AND m.memberId != :currentMemberId")
    List<Object[]> findByNicknameContainingWithFollowStatus(@Param("nickname") String nickname,
                                                           @Param("currentMemberId") Long currentMemberId);
}
