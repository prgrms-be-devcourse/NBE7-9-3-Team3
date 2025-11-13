package org.example.backend.domain.fish.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.dto.FishRequestDto;
import org.example.backend.domain.fish.dto.FishResponseDto;
import org.example.backend.domain.fish.dto.FishUpdateResponseDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishLogRepository;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FishService {

  private final FishRepository fishRepository;
  private final FishLogRepository fishLogRepository;
  private final AquariumRepository aquariumRepository;

  public long count() {
    return fishRepository.count();
  }

  public FishResponseDto createFish(Long aquariumId, FishRequestDto fishRequestDto) {
    String species = fishRequestDto.species();
    String name = fishRequestDto.name();

    Aquarium aquarium = aquariumRepository.findById(aquariumId)
        .orElseThrow(() -> new BusinessException(ErrorCode.AQUARIUM_NOT_FOUND));
    Fish fish = new Fish(aquarium, species, name);

    fishRepository.save(fish);
    FishResponseDto responseDto = new FishResponseDto(fish);

    return responseDto;
  }

  public List<FishResponseDto> findAllByAquariumId(Long aquariumId) {
    List<FishResponseDto> responseDto = fishRepository.findAllByAquarium_Id(aquariumId)
        .reversed().stream().map(FishResponseDto::new).toList();

    return responseDto;
  }

  public FishUpdateResponseDto updateFish(Long aquariumId, Long fishId, FishRequestDto fishRequestDto) {
    Fish fish = fishRepository.findByAquarium_IdAndId(aquariumId, fishId)
        .orElseThrow(() -> new BusinessException(ErrorCode.FISH_NOT_FOUND));

    String species = fishRequestDto.species();
    String name = fishRequestDto.name();

    fish.changeDetails(species, name);
    fishRepository.save(fish);

    FishUpdateResponseDto responseDto = new FishUpdateResponseDto(fish);

    return responseDto;
  }

  @Transactional
  public void deleteFish(Long aquariumId, Long fishId) {
    Fish fish = fishRepository.findByAquarium_IdAndId(aquariumId, fishId)
        .orElseThrow(() -> new BusinessException(ErrorCode.FISH_NOT_FOUND));

    fishLogRepository.deleteAllByFish(fish);
    fishRepository.delete(fish);
  }

}
