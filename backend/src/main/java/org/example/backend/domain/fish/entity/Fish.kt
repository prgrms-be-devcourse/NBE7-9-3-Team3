package org.example.backend.domain.fish.entity

import jakarta.persistence.*
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.global.jpa.entity.BaseEntity

@Entity
class Fish(
    @field:JoinColumn(name = "aquarium_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    var aquarium: Aquarium,

    @field:Column(length = 50)
    var species: String,

    @field:Column(length = 50)
    var name: String

) : BaseEntity() {

    fun changeAquarium(myOwnedAquarium: Aquarium) {
        this.aquarium = myOwnedAquarium
    }

    fun changeDetails(species: String, name: String) {
        this.species = species
        this.name = name
    }
}
