package org.example.backend.domain.fish.repository;

import java.util.List;
import java.util.Optional;
import org.example.backend.domain.fish.entity.Fish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FishRepository extends JpaRepository<Fish, Long> {

  long countByAquarium_Id(Long aquariumId);

  List<Fish> findAllByAquarium_Id(Long aquariumId);

  Optional<Fish> findByAquarium_IdAndId(Long aquariumId, Long fishId);
}
