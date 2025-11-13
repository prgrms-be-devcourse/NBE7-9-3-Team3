package org.example.backend.domain.aquarium.repository;

import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.entity.AquariumLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AquariumLogRepository extends JpaRepository<AquariumLog, Long> {

    // aquariumId로 로그 조회
    List<AquariumLog> findByAquariumId(Long aquariumId);
    
    // aquarium 엔티티로 로그 조회
    List<AquariumLog> findByAquarium(Aquarium aquarium);

  void deleteAllByAquarium(Aquarium aquarium);
}
