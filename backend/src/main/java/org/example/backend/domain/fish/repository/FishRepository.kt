package org.example.backend.domain.fish.repository

import org.example.backend.domain.fish.entity.Fish
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FishRepository : JpaRepository<Fish, Long> {
    fun countByAquarium_Id(aquariumId: Long): Long

    fun findAllByAquarium_Id(aquariumId: Long): List<Fish>

    fun findByAquarium_IdAndId(aquariumId: Long, fishId: Long): Fish?
}