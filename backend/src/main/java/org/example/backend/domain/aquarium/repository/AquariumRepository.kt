package org.example.backend.domain.aquarium.repository

import org.example.backend.domain.aquarium.entity.Aquarium
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface AquariumRepository : JpaRepository<Aquarium, Long> {
    fun findAllByMember_MemberId(memberId: Long): List<Aquarium>

    fun existsByMember_MemberIdAndOwnedAquariumTrue(memberId: Long): Boolean

    fun findByMember_MemberIdAndOwnedAquariumTrue(memberId: Long): Aquarium?

    /**
     * 이메일 알림을 보낼 어항들을 조회
     * - cycleDate가 0보다 큰 어항 (알림 활성화)
     * - nextDate가 현재 시간보다 이전이거나 같은 어항 (알림 시간 도래)
     * - 회원의 이메일이 존재하는 어항
     */
    @Query(
        ("SELECT a FROM Aquarium a WHERE a.cycleDate > 0 " +
                "AND a.nextDate <= :now " +
                "AND a.member.email IS NOT NULL")
    )
    fun findAquariumsForNotification(@Param("now") now: LocalDateTime?): List<Aquarium>
}
