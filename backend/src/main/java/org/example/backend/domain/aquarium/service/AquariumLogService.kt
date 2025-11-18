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
        // @NotNull validation이 통과했으므로 null이 아님
        val temperature = requestDto.temperature 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        val ph = requestDto.ph 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        val logDate = requestDto.logDate 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        
        return AquariumLog(
            aquarium = aquarium,
            temperature = temperature,
            ph = ph,
            logDate = logDate
        )
    }

    override fun convertToResponseDto(entity: AquariumLog): AquariumLogResponseDto {
        return AquariumLogResponseDto.from(entity)
    }

    override fun updateEntity(entity: AquariumLog, requestDto: AquariumLogRequestDto, aquarium: Aquarium) {
        // @NotNull validation이 통과했으므로 null이 아님
        val temperature = requestDto.temperature 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        val ph = requestDto.ph 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        val logDate = requestDto.logDate 
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        
        entity.aquarium = aquarium
        entity.temperature = temperature
        entity.ph = ph
        entity.logDate = logDate
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
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
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
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
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
