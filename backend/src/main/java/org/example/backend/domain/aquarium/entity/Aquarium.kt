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
class Aquarium(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Column(length = 50)
    var name: String,

    var ownedAquarium: Boolean,  // 기본값 = false

    @Column(columnDefinition = "int default 0")  // 기본값 = 0
    @field:PositiveOrZero  // 0 이상의 숫자만 가능
    var cycleDate: Int,

    var lastDate: LocalDateTime? = null,
    var nextDate: LocalDateTime? = null,

    ) : BaseEntity() {

    constructor(member: Member, name: String) : this(
        member,
        name,
        false,
        0,
        null,
        null
    )

    constructor(member: Member, name: String, ownedAquarium: Boolean) : this(
        member,
        name,
        ownedAquarium,
        0,
        null,
        null
    )

    fun changeSchedule(cycleDate: Int, lastDate: LocalDateTime?, nextDate: LocalDateTime?) {
        this.cycleDate = cycleDate
        this.lastDate = lastDate
        this.nextDate = nextDate
    }

    fun changeName(name: String) {
        this.name = name
    }
}