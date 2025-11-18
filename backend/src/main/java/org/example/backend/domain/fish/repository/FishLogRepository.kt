package org.example.backend.domain.fish.repository

import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.entity.FishLog
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 물고기 로그 리포지토리
 * 물고기 로그 엔티티에 대한 데이터 접근을 제공
 */
interface FishLogRepository : JpaRepository<FishLog, Long> {
    /**
     * 물고기 ID로 로그 목록 조회
     * @param fishId 물고기 ID
     * @return 물고기 로그 목록
     */
    fun findByFishId(fishId: Long): List<FishLog>

    /**
     * Fish 엔티티로 로그 목록 조회
     * @param fish 물고기 엔티티
     * @return 물고기 로그 목록
     */
    fun findByFish(fish: Fish): List<FishLog>

    /**
     * Fish 엔티티로 모든 로그 삭제
     * @param fish 물고기 엔티티
     */
    fun deleteAllByFish(fish: Fish)
}
