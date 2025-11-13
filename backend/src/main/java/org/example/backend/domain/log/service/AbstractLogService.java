package org.example.backend.domain.log.service;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

// 로그 서비스 공통 추상 클래스 aquariumlog, fishlog 중복코드 제거
public abstract class AbstractLogService<Entity, RequestDto, ResponseDto, ParentEntity> {

    protected abstract JpaRepository<Entity, Long> getLogRepository();
    protected abstract JpaRepository<ParentEntity, Long> getParentRepository();
    protected abstract ErrorCode getLogNotFoundErrorCode();
    protected abstract ErrorCode getParentNotFoundErrorCode();
    protected abstract Entity createEntity(RequestDto requestDto, ParentEntity parent);
    protected abstract ResponseDto convertToResponseDto(Entity entity);
    protected abstract void updateEntity(Entity entity, RequestDto requestDto, ParentEntity parent);
    protected abstract List<Entity> findByParentId(Long parentId);

    // 로그생성
    public ResponseDto createLog(RequestDto requestDto, Long parentId) {
        ParentEntity parent = getParentRepository().findById(parentId)
                .orElseThrow(() -> new BusinessException(getParentNotFoundErrorCode()));
        
        Entity entity = createEntity(requestDto, parent);
        Entity savedEntity = getLogRepository().save(entity);
        return convertToResponseDto(savedEntity);
    }

    // 로그목록 조회
    public List<ResponseDto> getLogsByParentId(Long parentId) {
        return findByParentId(parentId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    //로그 수정
    public ResponseDto updateLog(Long logId, RequestDto requestDto, Long parentId) {
        Entity entity = getLogRepository().findById(logId)
                .orElseThrow(() -> new BusinessException(getLogNotFoundErrorCode()));

        ParentEntity parent = getParentRepository().findById(parentId)
                .orElseThrow(() -> new BusinessException(getParentNotFoundErrorCode()));

        updateEntity(entity, requestDto, parent);
        return convertToResponseDto(entity);
    }

    //로그 삭제 
    public void deleteLog(Long logId) {
        Entity entity = getLogRepository().findById(logId)
                .orElseThrow(() -> new BusinessException(getLogNotFoundErrorCode()));
        getLogRepository().delete(entity);
    }
}
