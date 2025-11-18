package org.example.backend.domain.fish.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 물고기 로그 엔티티
 * 물고기의 상태 데이터를 저장하는 엔티티
 * 
 * @property logId 로그 ID (프라이머리 키, JPA가 자동 생성 - 저장 전 0, 저장 후 항상 값이 있음)
 * @property fish 물고기 엔티티 (다대일 관계, 항상 값이 있음)
 * @property status 상태 (항상 값이 있음)
 * @property logDate 기록 일시 (항상 값이 있음)
 */
@Entity
@Table(name = "fish_log")
class FishLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    var logId: Long = 0L
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fish_id", nullable = false)
    lateinit var fish: Fish

    @Column(name = "status", nullable = false, length = 100)
    lateinit var status: String

    @Column(name = "log_date", nullable = false)
    var logDate: LocalDateTime = LocalDateTime.now()

    /**
     * 일반 사용을 위한 생성자
     * @param fish 물고기 엔티티
     * @param status 상태
     * @param logDate 기록 일시 (기본값: 현재 시간)
     */
    constructor(
        fish: Fish,
        status: String,
        logDate: LocalDateTime = LocalDateTime.now()
    ) {
        this.fish = fish
        this.status = status
        this.logDate = logDate
    }

    /**
     * JPA를 위한 protected 기본 생성자
     * JPA가 리플렉션을 통해 필드를 직접 설정하므로 빈 생성자만 필요
     */
    @Suppress("UNUSED")
    protected constructor()

    @PrePersist
    protected fun onCreate() {
        // JPA가 필드를 직접 설정하므로 추가 처리 불필요
    }
}
