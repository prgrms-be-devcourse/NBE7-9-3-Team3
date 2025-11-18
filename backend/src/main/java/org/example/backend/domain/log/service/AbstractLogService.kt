package org.example.backend.domain.log.service

import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 로그 서비스 공통 추상 클래스
 * AquariumLog, FishLog 등 다양한 로그 서비스의 공통 기능을 제공
 * 
 * @param Entity 로그 엔티티 타입
 * @param RequestDto 요청 DTO 타입
 * @param ResponseDto 응답 DTO 타입
 * @param ParentEntity 부모 엔티티 타입 (Aquarium, Fish 등)
 */
abstract class AbstractLogService<Entity, RequestDto, ResponseDto, ParentEntity> {
    
    protected abstract fun getLogRepository(): JpaRepository<Entity, Long>
    protected abstract fun getParentRepository(): JpaRepository<ParentEntity, Long>
    protected abstract fun getLogNotFoundErrorCode(): ErrorCode
    protected abstract fun getParentNotFoundErrorCode(): ErrorCode
    protected abstract fun createEntity(requestDto: RequestDto, parent: ParentEntity): Entity
    protected abstract fun convertToResponseDto(entity: Entity): ResponseDto
    protected abstract fun updateEntity(entity: Entity, requestDto: RequestDto, parent: ParentEntity)
    protected abstract fun findByParentId(parentId: Long): List<Entity>

    /**
     * 로그 생성
     * @param requestDto 요청 DTO
     * @param parentId 부모 엔티티 ID
     * @return 생성된 로그의 응답 DTO
     */
    fun createLog(requestDto: RequestDto, parentId: Long): ResponseDto {
        val parent = getParentRepository().findById(parentId)
            .orElseThrow { BusinessException(getParentNotFoundErrorCode()) }
        
        val entity = createEntity(requestDto, parent)
        // 코틀린의 제네릭 타입 추론 문제를 해결하기 위한 타입 캐스팅
        // JpaRepository.save()는 <S extends T> S save(S entity) 형태이므로 타입 캐스팅 필요
        // 임시 코드: Any로 캐스팅하여 타입 체크 우회
        @Suppress("UNCHECKED_CAST")
        val savedEntity: Entity = (getLogRepository() as JpaRepository<Any, Long>).save(entity as Any) as Entity
        return convertToResponseDto(savedEntity)
    }

    /**
     * 부모 ID로 로그 목록 조회
     * @param parentId 부모 엔티티 ID
     * @return 로그 응답 DTO 목록
     */
    fun getLogsByParentId(parentId: Long): List<ResponseDto> {
        return findByParentId(parentId).map { convertToResponseDto(it) }
    }

    /**
     * 로그 수정
     * @param logId 로그 ID
     * @param requestDto 요청 DTO
     * @param parentId 부모 엔티티 ID
     * @return 수정된 로그의 응답 DTO
     */
    fun updateLog(logId: Long, requestDto: RequestDto, parentId: Long): ResponseDto {
        val entity = getLogRepository().findById(logId)
            .orElseThrow { BusinessException(getLogNotFoundErrorCode()) }

        val parent = getParentRepository().findById(parentId)
            .orElseThrow { BusinessException(getParentNotFoundErrorCode()) }

        updateEntity(entity, requestDto, parent)
        return convertToResponseDto(entity)
    }

    /**
     * 로그 삭제
     * 하위 클래스에서 오버라이드 가능하도록 open으로 선언
     * @param logId 로그 ID
     */
    open fun deleteLog(logId: Long) {
        val entity = getLogRepository().findById(logId)
            .orElseThrow { BusinessException(getLogNotFoundErrorCode()) }
        // 코틀린의 제네릭 타입 추론 문제를 해결하기 위한 타입 캐스팅
        // 임시 코드: Any로 캐스팅하여 타입 체크 우회
        @Suppress("UNCHECKED_CAST")
        (getLogRepository() as JpaRepository<Any, Long>).delete(entity as Any)
    }
}
