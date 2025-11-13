package org.example.backend.domain.fish.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.entity.FishLog;
import org.example.backend.domain.fish.repository.FishLogRepository;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.log.service.AbstractLogService;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FishLogService extends AbstractLogService<FishLog, FishLogRequestDto, FishLogResponseDto, Fish> {
    
    private final FishLogRepository fishLogRepository;
    private final FishRepository fishRepository;

    @Override
    protected JpaRepository<FishLog, Long> getLogRepository() {
        return fishLogRepository;
    }

    @Override
    protected JpaRepository<Fish, Long> getParentRepository() {
        return fishRepository;
    }

    @Override
    protected ErrorCode getLogNotFoundErrorCode() {
        return ErrorCode.FISH_LOG_NOT_FOUND;
    }

    @Override
    protected ErrorCode getParentNotFoundErrorCode() {
        return ErrorCode.FISH_NOT_FOUND;
    }

    @Override
    protected FishLog createEntity(FishLogRequestDto requestDto, Fish fish) {
        return FishLog.builder()
                .fish(fish)
                .status(requestDto.getStatus())
                .logDate(requestDto.getLogDate() != null ? requestDto.getLogDate() : LocalDateTime.now())
                .build();
    }

    @Override
    protected FishLogResponseDto convertToResponseDto(FishLog entity) {
        return FishLogResponseDto.from(entity);
    }

    @Override
    protected void updateEntity(FishLog entity, FishLogRequestDto requestDto, Fish fish) {
        entity.setFish(fish);
        entity.setStatus(requestDto.getStatus());
        entity.setLogDate(requestDto.getLogDate());
    }

    @Override
    protected List<FishLog> findByParentId(Long parentId) {
        return fishLogRepository.findByFishId(parentId);
    }

    // 기존 메서드들을 추상 클래스의 메서드로 위임
    @Transactional
    public FishLogResponseDto createLog(FishLogRequestDto requestDto) {
        return createLog(requestDto, requestDto.getFishId());
    }

    public List<FishLogResponseDto> getLogsByFishId(Long fishId) {
        return getLogsByParentId(fishId);
    }

    @Transactional
    public FishLogResponseDto updateLog(Long logId, FishLogRequestDto requestDto) {
        return updateLog(logId, requestDto, requestDto.getFishId());
    }

    @Transactional
    public void deleteLog(Long logId) {
        super.deleteLog(logId);
    }
}
