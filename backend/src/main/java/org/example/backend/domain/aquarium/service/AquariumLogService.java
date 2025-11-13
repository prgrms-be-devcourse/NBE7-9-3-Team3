package org.example.backend.domain.aquarium.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.entity.AquariumLog;
import org.example.backend.domain.aquarium.repository.AquariumLogRepository;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.log.service.AbstractLogService;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AquariumLogService extends AbstractLogService<AquariumLog, AquariumLogRequestDto, AquariumLogResponseDto, Aquarium> {

    private final AquariumLogRepository aquariumLogRepository;
    private final AquariumRepository aquariumRepository;

    @Override
    protected JpaRepository<AquariumLog, Long> getLogRepository() {
        return aquariumLogRepository;
    }

    @Override
    protected JpaRepository<Aquarium, Long> getParentRepository() {
        return aquariumRepository;
    }

    @Override
    protected ErrorCode getLogNotFoundErrorCode() {
        return ErrorCode.AQUARIUM_LOG_NOT_FOUND;
    }

    @Override
    protected ErrorCode getParentNotFoundErrorCode() {
        return ErrorCode.AQUARIUM_NOT_FOUND;
    }

    @Override
    protected AquariumLog createEntity(AquariumLogRequestDto requestDto, Aquarium aquarium) {
        return AquariumLog.builder()
                .aquarium(aquarium)
                .temperature(requestDto.getTemperature())
                .ph(requestDto.getPh())
                .logDate(requestDto.getLogDate())
                .build();
    }

    @Override
    protected AquariumLogResponseDto convertToResponseDto(AquariumLog entity) {
        return AquariumLogResponseDto.from(entity);
    }

    @Override
    protected void updateEntity(AquariumLog entity, AquariumLogRequestDto requestDto, Aquarium aquarium) {
        entity.setAquarium(aquarium);
        entity.setTemperature(requestDto.getTemperature());
        entity.setPh(requestDto.getPh());
        entity.setLogDate(requestDto.getLogDate());
    }

    @Override
    protected List<AquariumLog> findByParentId(Long parentId) {
        return aquariumLogRepository.findByAquariumId(parentId);
    }

    // 기존 메서드들을 추상 클래스의 메서드로 위임
    @Transactional
    public AquariumLogResponseDto createLog(AquariumLogRequestDto requestDto) {
        return createLog(requestDto, requestDto.getAquariumId());
    }

    public List<AquariumLogResponseDto> getLogsByAquariumId(Long aquariumId) {
        return getLogsByParentId(aquariumId);
    }

    @Transactional
    public AquariumLogResponseDto updateLog(Long logId, AquariumLogRequestDto requestDto) {
        return updateLog(logId, requestDto, requestDto.getAquariumId());
    }

    @Transactional
    public void deleteLog(Long logId) {
        super.deleteLog(logId);
    }
}
