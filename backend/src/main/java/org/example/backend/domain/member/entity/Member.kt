package org.example.backend.domain.member.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
class Member(
    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(unique = true, length = 50)
    var nickname: String,

    @Column(length = 500)
    var profileImage: String? = null

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var memberId: Long? = null
        protected set

    var tradeScore: Int = 50
        private set

    @CreatedDate
    var createDate: LocalDateTime? = null
        protected set

    @Column(nullable = false)
    var points: Long = 0L

    fun updatePoints(newPoint: Long) {
        this.points = newPoint
    }

    fun updateMemberInfo(
        email: String,
        password: String,
        nickname: String,
        profileImage: String?
    ) {
        this.email = email
        this.password = password
        this.nickname = nickname
        this.profileImage = profileImage
    }

}
