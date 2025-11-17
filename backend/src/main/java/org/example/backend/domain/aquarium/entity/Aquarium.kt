package org.example.backend.domain.aquarium.entity

import jakarta.persistence.*
import jakarta.validation.constraints.PositiveOrZero
import lombok.AccessLevel
import lombok.Getter
import lombok.NoArgsConstructor
import org.example.backend.domain.member.entity.Member
import org.example.backend.global.jpa.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Aquarium : BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private var member: Member?

    @Column(length = 50)
    private var name: String?

    // 기본값 = false
    private var ownedAquarium = false

    @Column(columnDefinition = "int default 0") // 기본값 = 0
    // 0 이상의 숫자만 가능
    private var cycleDate: @PositiveOrZero Int = 0

    private var lastDate: LocalDateTime? = null

    private var nextDate: LocalDateTime? = null

    constructor(member: Member?, name: String?) {
        this.member = member
        this.name = name
    }

    constructor(member: Member?, name: String?, ownedAquarium: Boolean) {
        this.member = member
        this.name = name
        this.ownedAquarium = ownedAquarium
    }

    fun changeSchedule(cycleDate: Int, lastDate: LocalDateTime?, nextDate: LocalDateTime?) {
        this.cycleDate = cycleDate
        this.lastDate = lastDate
        this.nextDate = nextDate
    }

    fun changeName(name: String?) {
        this.name = name
    }
}
