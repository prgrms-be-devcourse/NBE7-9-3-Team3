package org.example.backend.domain.aquarium.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 어항 로그 엔티티
 * 어항의 환경 데이터(온도, pH 등)를 저장하는 엔티티
 * 
 * @property logId 로그 ID (프라이머리 키, JPA가 자동 생성 - 저장 전 0, 저장 후 항상 값이 있음)
 * @property aquarium 어항 엔티티 (다대일 관계, 항상 값이 있음)
 * @property temperature 온도 (선택적 필드, nullable)
 * @property ph pH 값 (선택적 필드, nullable)
 * @property logDate 기록 일시 (항상 값이 있음)
 */
@Entity
@Table(name = "aquarium_log")
class AquariumLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    var logId: Long = 0L
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aquarium_id", nullable = false)
    lateinit var aquarium: Aquarium

    @Column(name = "temperature")
    var temperature: Double? = null

    @Column(name = "ph")
    var ph: Double? = null

    @Column(name = "log_date", nullable = false)
    var logDate: LocalDateTime = LocalDateTime.now()

    /**
     * 일반 사용을 위한 생성자
     * @param aquarium 어항 엔티티
     * @param temperature 온도 (선택적)
     * @param ph pH 값 (선택적)
     * @param logDate 기록 일시 (기본값: 현재 시간)
     */
    constructor(
        aquarium: Aquarium,
        temperature: Double? = null,
        ph: Double? = null,
        logDate: LocalDateTime = LocalDateTime.now()
    ) {
        this.aquarium = aquarium
        this.temperature = temperature
        this.ph = ph
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
