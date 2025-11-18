package org.example.backend.domain.fish.service

import org.example.backend.domain.fish.dto.FishLogRequestDto
import org.example.backend.domain.fish.dto.FishLogResponseDto
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.entity.FishLog
import org.example.backend.domain.fish.repository.FishLogRepository
import org.example.backend.domain.fish.repository.FishRepository
import org.example.backend.domain.log.service.AbstractLogService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 물고기 로그 서비스
 * 물고기의 상태 데이터를 관리하는 서비스
 */
@Service
@Transactional(readOnly = true)
class FishLogService(
    private val fishLogRepository: FishLogRepository,
    private val fishRepository: FishRepository
) : AbstractLogService<FishLog, FishLogRequestDto, FishLogResponseDto, Fish>() {

    override fun getLogRepository(): JpaRepository<FishLog, Long> = fishLogRepository

    override fun getParentRepository(): JpaRepository<Fish, Long> = fishRepository

    override fun getLogNotFoundErrorCode(): ErrorCode = ErrorCode.FISH_LOG_NOT_FOUND

    override fun getParentNotFoundErrorCode(): ErrorCode = ErrorCode.FISH_NOT_FOUND

    override fun createEntity(requestDto: FishLogRequestDto, fish: Fish): FishLog {
        val status = requestDto.status 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        val logDate = requestDto.logDate ?: LocalDateTime.now()
        
        return FishLog(
            fish = fish,
            status = status,
            logDate = logDate
        )
    }

    override fun convertToResponseDto(entity: FishLog): FishLogResponseDto {
        return FishLogResponseDto.from(entity)
    }

    override fun updateEntity(entity: FishLog, requestDto: FishLogRequestDto, fish: Fish) {
        val status = requestDto.status 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        val logDate = requestDto.logDate ?: LocalDateTime.now()
        
        entity.fish = fish
        entity.status = status
        entity.logDate = logDate
    }

    override fun findByParentId(parentId: Long): List<FishLog> {
        return fishLogRepository.findByFishId(parentId)
    }

    /**
     * 물고기 로그 생성
     * @param requestDto 물고기 로그 요청 DTO
     * @return 생성된 물고기 로그 응답 DTO
     */
    @Transactional
    fun createLog(requestDto: FishLogRequestDto): FishLogResponseDto {
        val fishId = requestDto.fishId 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        return createLog(requestDto, fishId)
    }

    /**
     * 물고기 ID로 로그 목록 조회
     * @param fishId 물고기 ID
     * @return 물고기 로그 응답 DTO 목록
     */
    fun getLogsByFishId(fishId: Long): List<FishLogResponseDto> {
        return getLogsByParentId(fishId)
    }

    /**
     * 물고기 로그 수정
     * @param logId 로그 ID
     * @param requestDto 물고기 로그 요청 DTO
     * @return 수정된 물고기 로그 응답 DTO
     */
    @Transactional
    fun updateLog(logId: Long, requestDto: FishLogRequestDto): FishLogResponseDto {
        val fishId = requestDto.fishId 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        return updateLog(logId, requestDto, fishId)
    }

    /**
     * 물고기 로그 삭제
     * @param logId 로그 ID
     */
    @Transactional
    override fun deleteLog(logId: Long) {
        val entity = fishLogRepository.findById(logId)
            .orElseThrow { BusinessException(ErrorCode.FISH_LOG_NOT_FOUND) }
        fishLogRepository.delete(entity)
    }
}
