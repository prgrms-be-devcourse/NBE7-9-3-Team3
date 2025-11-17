package org.example.backend.domain.aquarium.service

import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.entity.AquariumLog
import org.example.backend.domain.aquarium.repository.AquariumLogRepository
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.log.service.AbstractLogService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 어항 로그 서비스
 * 어항의 환경 데이터(온도, pH 등)를 관리하는 서비스
 */
@Service
@Transactional(readOnly = true)
class AquariumLogService(
    private val aquariumLogRepository: AquariumLogRepository,
    private val aquariumRepository: AquariumRepository
) : AbstractLogService<AquariumLog, AquariumLogRequestDto, AquariumLogResponseDto, Aquarium>() {

    override fun getLogRepository(): JpaRepository<AquariumLog, Long> = aquariumLogRepository

    override fun getParentRepository(): JpaRepository<Aquarium, Long> = aquariumRepository

    override fun getLogNotFoundErrorCode(): ErrorCode = ErrorCode.AQUARIUM_LOG_NOT_FOUND

    override fun getParentNotFoundErrorCode(): ErrorCode = ErrorCode.AQUARIUM_NOT_FOUND

    override fun createEntity(requestDto: AquariumLogRequestDto, aquarium: Aquarium): AquariumLog {
        return AquariumLog(
            aquarium = aquarium,
            temperature = requestDto.temperature,
            ph = requestDto.ph,
            logDate = requestDto.logDate ?: LocalDateTime.now()
        )
    }

    override fun convertToResponseDto(entity: AquariumLog): AquariumLogResponseDto {
        return AquariumLogResponseDto.from(entity)
    }

    override fun updateEntity(entity: AquariumLog, requestDto: AquariumLogRequestDto, aquarium: Aquarium) {
        entity.aquarium = aquarium
        entity.temperature = requestDto.temperature
        entity.ph = requestDto.ph
        entity.logDate = requestDto.logDate ?: LocalDateTime.now()
    }

    override fun findByParentId(parentId: Long): List<AquariumLog> {
        return aquariumLogRepository.findByAquariumId(parentId)
    }

    /**
     * 어항 로그 생성
     * @param requestDto 어항 로그 요청 DTO
     * @return 생성된 어항 로그 응답 DTO
     */
    @Transactional
    fun createLog(requestDto: AquariumLogRequestDto): AquariumLogResponseDto {
        val aquariumId = requestDto.aquariumId
            ?: throw BusinessException(ErrorCode.AQUARIUM_NOT_FOUND)
        return createLog(requestDto, aquariumId)
    }

    /**
     * 어항 ID로 로그 목록 조회
     * @param aquariumId 어항 ID
     * @return 어항 로그 응답 DTO 목록
     */
    fun getLogsByAquariumId(aquariumId: Long): List<AquariumLogResponseDto> {
        return getLogsByParentId(aquariumId)
    }

    /**
     * 어항 로그 수정
     * @param logId 로그 ID
     * @param requestDto 어항 로그 요청 DTO
     * @return 수정된 어항 로그 응답 DTO
     */
    @Transactional
    fun updateLog(logId: Long, requestDto: AquariumLogRequestDto): AquariumLogResponseDto {
        val aquariumId = requestDto.aquariumId
            ?: throw BusinessException(ErrorCode.AQUARIUM_NOT_FOUND)
        return updateLog(logId, requestDto, aquariumId)
    }

    /**
     * 어항 로그 삭제
     * @param logId 로그 ID
     */
    @Transactional
    override fun deleteLog(logId: Long) {
        val entity = aquariumLogRepository.findById(logId)
            .orElseThrow { BusinessException(ErrorCode.AQUARIUM_LOG_NOT_FOUND) }
        aquariumLogRepository.delete(entity)
    }
}
