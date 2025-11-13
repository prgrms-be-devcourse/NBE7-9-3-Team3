package org.example.backend.domain.fish.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.global.jpa.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fish extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aquarium_id")
  private Aquarium aquarium;

  @Column(length = 50)
  private String species;

  @Column(length = 50)
  private String name;

  public Fish(Aquarium aquarium, String species, String name) {
    this.aquarium = aquarium;
    this.species = species;
    this.name = name;
  }

  public void changeAquarium(Aquarium myOwnedAquarium) {
    this.aquarium = myOwnedAquarium;
  }

  public void changeDetails(String species, String name) {
    this.species = species;
    this.name = name;
  }
}
