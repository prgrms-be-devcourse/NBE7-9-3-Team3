package org.example.backend.domain.aquarium.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AquariumRepository extends JpaRepository<Aquarium, Long> {

  List<Aquarium> findAllByMember_MemberId(Long memberId);

  boolean existsByMember_MemberIdAndOwnedAquariumTrue(Long memberId);

  Optional<Aquarium> findByMember_MemberIdAndOwnedAquariumTrue(Long memberId);

  /**
   * 이메일 알림을 보낼 어항들을 조회
   * - cycleDate가 0보다 큰 어항 (알림 활성화)
   * - nextDate가 현재 시간보다 이전이거나 같은 어항 (알림 시간 도래)
   * - 회원의 이메일이 존재하는 어항
   */
  @Query("SELECT a FROM Aquarium a WHERE a.cycleDate > 0 " +
         "AND a.nextDate <= :now " +
         "AND a.member.email IS NOT NULL")
  List<Aquarium> findAquariumsForNotification(@Param("now") LocalDateTime now);
}
