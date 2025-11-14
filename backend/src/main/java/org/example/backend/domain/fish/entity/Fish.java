package org.example.backend.domain.fish.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Getter
import lombok.NoArgsConstructor
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.global.jpa.entity.BaseEntity

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Fish(
    @field:JoinColumn(name = "aquarium_id") @field:ManyToOne(fetch = FetchType.LAZY) private var aquarium: Aquarium?,
    @field:Column(
        length = 50
    ) private var species: String?,
    @field:Column(length = 50) private var name: String?
) : BaseEntity() {
    fun changeAquarium(myOwnedAquarium: Aquarium?) {
        this.aquarium = myOwnedAquarium
    }

    fun changeDetails(species: String?, name: String?) {
        this.species = species
        this.name = name
    }
}
