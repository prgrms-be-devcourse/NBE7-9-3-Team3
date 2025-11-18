package org.example.backend.domain.aquarium.repository

import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.entity.AquariumLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 어항 로그 리포지토리
 * 어항 로그 엔티티에 대한 데이터베이스 접근을 담당
 */
@Repository
interface AquariumLogRepository : JpaRepository<AquariumLog, Long> {
    
    /**
     * 어항 ID로 로그 목록 조회
     * @param aquariumId 어항 ID
     * @return 어항 로그 목록
     */
    fun findByAquariumId(aquariumId: Long): List<AquariumLog>
    
    /**
     * 어항 엔티티로 로그 목록 조회
     * @param aquarium 어항 엔티티
     * @return 어항 로그 목록
     */
    fun findByAquarium(aquarium: Aquarium): List<AquariumLog>

    /**
     * 어항에 속한 모든 로그 삭제
     * @param aquarium 어항 엔티티
     */
    fun deleteAllByAquarium(aquarium: Aquarium)
}
