package org.example.backend.domain.fish.service

import lombok.RequiredArgsConstructor
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.dto.FishRequestDto
import org.example.backend.domain.fish.dto.FishResponseDto
import org.example.backend.domain.fish.dto.FishUpdateResponseDto
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.repository.FishLogRepository
import org.example.backend.domain.fish.repository.FishRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class FishService(
    private val fishRepository: FishRepository,
    private val fishLogRepository: FishLogRepository,
    private val aquariumRepository: AquariumRepository
) {

    fun count(): Long {
        return fishRepository.count()
    }

    fun createFish(aquariumId: Long, fishRequestDto: FishRequestDto): FishResponseDto {
        val species = fishRequestDto.species
        val name = fishRequestDto.name

        val aquarium = aquariumRepository.findById(aquariumId)
            .orElseThrow { BusinessException(ErrorCode.AQUARIUM_NOT_FOUND) }
        val fish = Fish(aquarium, species, name)
        fishRepository.save(fish)

        return FishResponseDto(fish)
    }

    fun findAllByAquariumId(aquariumId: Long): List<FishResponseDto> =
        fishRepository.findAllByAquarium_Id(aquariumId)
            .asReversed()
            .map { FishResponseDto(it) }

    fun updateFish(aquariumId: Long, fishId: Long, fishRequestDto: FishRequestDto): FishUpdateResponseDto {
        val fish = fishRepository
            .findByAquarium_IdAndId(aquariumId, fishId)
            ?: throw BusinessException(ErrorCode.FISH_NOT_FOUND)

        val species = fishRequestDto.species
        val name = fishRequestDto.name

        fish.changeDetails(species, name)
        fishRepository.save(fish)

        return FishUpdateResponseDto(fish)
    }

    @Transactional
    fun deleteFish(aquariumId: Long, fishId: Long) {
        val fish: Fish = fishRepository
            .findByAquarium_IdAndId(aquariumId, fishId)
            ?: throw BusinessException(ErrorCode.FISH_NOT_FOUND)

        fishLogRepository.deleteAllByFish(fish)
        fishRepository.delete(fish)
    }
}
