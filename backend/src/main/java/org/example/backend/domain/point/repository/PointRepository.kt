package org.example.backend.domain.point.repository

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.point.entity.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PointRepository : JpaRepository<Point, Long> {
    fun findAllByMemberOrderByCreateDateDesc(member: Member): List<Point>
}