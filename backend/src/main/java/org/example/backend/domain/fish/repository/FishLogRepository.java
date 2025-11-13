package org.example.backend.domain.fish.repository;

import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.entity.FishLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FishLogRepository extends JpaRepository<FishLog, Long> {
    
    // fishId로 로그 조회
    List<FishLog> findByFishId(Long fishId);
    
    // Fish 엔티티로 로그 조회
    List<FishLog> findByFish(Fish fish);

    void deleteAllByFish(Fish fish);
}
