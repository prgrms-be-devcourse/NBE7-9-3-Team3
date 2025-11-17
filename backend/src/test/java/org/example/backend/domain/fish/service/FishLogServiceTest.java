package org.example.backend.domain.fish.service;

import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.entity.FishLog;
import org.example.backend.domain.fish.repository.FishLogRepository;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.LoginUtil;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FishLogServiceTest {

    @Autowired
    private FishLogService fishLogService;

    @Autowired
    private FishLogRepository fishLogRepository;

    @Autowired
    private FishRepository fishRepository;

    @Autowired
    private AquariumRepository aquariumRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenService authTokenService;

    private Member testMember;
    private Aquarium testAquarium;
    private Fish testFish;

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember = loginUtil.createMember("fishLogService@test.com", "test1234", "fishLogService", null);

        testAquarium = new Aquarium(testMember, "테스트 어항");
        aquariumRepository.save(testAquarium);

        testFish = new Fish(testAquarium, "금붕어", "테스트 물고기");
        fishRepository.save(testFish);
    }

    @Test
    @DisplayName("물고기 로그 생성 성공")
    void createLog_Success() {
        FishLogRequestDto requestDto = FishLogRequestDto.builder()
                .fishId(testFish.getId())
                .status("건강함")
                .logDate(LocalDateTime.now())
                .build();

        FishLogResponseDto responseDto = fishLogService.createLog(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFishId()).isEqualTo(testFish.getId());
        assertThat(responseDto.getStatus()).isEqualTo("건강함");
        assertThat(responseDto.getAquariumId()).isEqualTo(testAquarium.getId());
        assertThat(responseDto.getLogDate()).isNotNull();

        FishLog savedLog = fishLogRepository.findById(responseDto.getLogId())
                .orElseThrow();
        assertThat(savedLog.getStatus()).isEqualTo("건강함");
    }

    @Test
    @DisplayName("물고기 로그 생성 - logDate가 null일 때 자동으로 현재 시간 설정")
    void createLog_WithNullLogDate_SetsCurrentTime() {
        FishLogRequestDto requestDto = FishLogRequestDto.builder()
                .fishId(testFish.getId())
                .status("건강함")
                .logDate(null)
                .build();

        FishLogResponseDto responseDto = fishLogService.createLog(requestDto);

        assertThat(responseDto.getLogDate()).isNotNull();
        assertThat(responseDto.getLogDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("물고기 로그 생성 실패 - 존재하지 않는 물고기")
    void createLog_Fail_WhenFishNotFound() {
        Long nonExistentFishId = 999L;
        FishLogRequestDto requestDto = FishLogRequestDto.builder()
                .fishId(nonExistentFishId)
                .status("건강함")
                .logDate(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> fishLogService.createLog(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_NOT_FOUND);
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 성공")
    void getLogsByFishId_Success() {
        FishLog log1 = FishLog.builder()
                .fish(testFish)
                .status("건강함")
                .logDate(LocalDateTime.now().minusDays(2))
                .build();
        FishLog log2 = FishLog.builder()
                .fish(testFish)
                .status("약간 아픔")
                .logDate(LocalDateTime.now().minusDays(1))
                .build();
        fishLogRepository.save(log1);
        fishLogRepository.save(log2);

        List<FishLogResponseDto> logs = fishLogService.getLogsByFishId(testFish.getId());

        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(FishLogResponseDto::getStatus)
                .containsExactlyInAnyOrder("건강함", "약간 아픔");
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 - 로그가 없을 때 빈 리스트 반환")
    void getLogsByFishId_EmptyList_WhenNoLogs() {
        List<FishLogResponseDto> logs = fishLogService.getLogsByFishId(testFish.getId());

        assertThat(logs).isEmpty();
    }

    @Test
    @DisplayName("물고기 로그 수정 성공")
    void updateLog_Success() {
        FishLog existingLog = FishLog.builder()
                .fish(testFish)
                .status("건강함")
                .logDate(LocalDateTime.now().minusDays(1))
                .build();
        fishLogRepository.save(existingLog);

        LocalDateTime newLogDate = LocalDateTime.now();
        FishLogRequestDto updateDto = FishLogRequestDto.builder()
                .fishId(testFish.getId())
                .status("아픔")
                .logDate(newLogDate)
                .build();

        FishLogResponseDto responseDto = fishLogService.updateLog(existingLog.getLogId(), updateDto);

        assertThat(responseDto.getStatus()).isEqualTo("아픔");
        assertThat(responseDto.getLogDate()).isEqualTo(newLogDate);
        assertThat(responseDto.getLogId()).isEqualTo(existingLog.getLogId());

        FishLog updatedLog = fishLogRepository.findById(existingLog.getLogId())
                .orElseThrow();
        assertThat(updatedLog.getStatus()).isEqualTo("아픔");
    }

    @Test
    @DisplayName("물고기 로그 수정 실패 - 존재하지 않는 로그")
    void updateLog_Fail_WhenLogNotFound() {
        Long nonExistentLogId = 999L;
        FishLogRequestDto updateDto = FishLogRequestDto.builder()
                .fishId(testFish.getId())
                .status("아픔")
                .logDate(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> fishLogService.updateLog(nonExistentLogId, updateDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_LOG_NOT_FOUND);
    }

    @Test
    @DisplayName("물고기 로그 수정 실패 - 존재하지 않는 물고기")
    void updateLog_Fail_WhenFishNotFound() {
        FishLog existingLog = FishLog.builder()
                .fish(testFish)
                .status("건강함")
                .logDate(LocalDateTime.now())
                .build();
        fishLogRepository.save(existingLog);

        Long nonExistentFishId = 999L;
        FishLogRequestDto updateDto = FishLogRequestDto.builder()
                .fishId(nonExistentFishId)
                .status("아픔")
                .logDate(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> fishLogService.updateLog(existingLog.getLogId(), updateDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_NOT_FOUND);
    }

    @Test
    @DisplayName("물고기 로그 삭제 성공")
    void deleteLog_Success() {
        FishLog log = FishLog.builder()
                .fish(testFish)
                .status("건강함")
                .logDate(LocalDateTime.now())
                .build();
        fishLogRepository.save(log);
        Long logId = log.getLogId();

        fishLogService.deleteLog(logId);

        assertThat(fishLogRepository.findById(logId)).isEmpty();
    }

    @Test
    @DisplayName("물고기 로그 삭제 실패 - 존재하지 않는 로그")
    void deleteLog_Fail_WhenLogNotFound() {
        Long nonExistentLogId = 999L;

        assertThatThrownBy(() -> fishLogService.deleteLog(nonExistentLogId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_LOG_NOT_FOUND);
    }
}


