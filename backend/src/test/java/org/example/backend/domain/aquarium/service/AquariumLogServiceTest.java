package org.example.backend.domain.aquarium.service;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.entity.AquariumLog;
import org.example.backend.domain.aquarium.repository.AquariumLogRepository;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AquariumLogServiceTest {

    @Autowired
    private AquariumLogService aquariumLogService;

    @Autowired
    private AquariumLogRepository aquariumLogRepository;

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

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember = loginUtil.createMember("aquariumLogService@test.com", "test1234", "aquariumLogService", null);

        testAquarium = new Aquarium(testMember, "테스트 어항");
        aquariumRepository.save(testAquarium);
    }

    @Test
    @DisplayName("어항 로그 생성 성공")
    void createLog_Success() {
        AquariumLogRequestDto requestDto = AquariumLogRequestDto.builder()
                .aquariumId(testAquarium.getId())
                .temperature(25.5)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();

        AquariumLogResponseDto responseDto = aquariumLogService.createLog(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getAquariumId()).isEqualTo(testAquarium.getId());
        assertThat(responseDto.getTemperature()).isEqualTo(25.5);
        assertThat(responseDto.getPh()).isEqualTo(7.0);
        assertThat(responseDto.getLogDate()).isNotNull();

        AquariumLog savedLog = aquariumLogRepository.findById(responseDto.getLogId())
                .orElseThrow();
        assertThat(savedLog.getTemperature()).isEqualTo(25.5);
        assertThat(savedLog.getPh()).isEqualTo(7.0);
    }

    @Test
    @DisplayName("어항 로그 생성 - temperature와 ph가 null일 수 있음")
    void createLog_WithNullValues_Success() {
        AquariumLogRequestDto requestDto = AquariumLogRequestDto.builder()
                .aquariumId(testAquarium.getId())
                .temperature(null)
                .ph(null)
                .logDate(LocalDateTime.now())
                .build();

        AquariumLogResponseDto responseDto = aquariumLogService.createLog(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTemperature()).isNull();
        assertThat(responseDto.getPh()).isNull();
    }

    @Test
    @DisplayName("어항 로그 생성 실패 - 존재하지 않는 어항")
    void createLog_Fail_WhenAquariumNotFound() {
        Long nonExistentAquariumId = 999L;
        AquariumLogRequestDto requestDto = AquariumLogRequestDto.builder()
                .aquariumId(nonExistentAquariumId)
                .temperature(25.5)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> aquariumLogService.createLog(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_NOT_FOUND);
    }

    @Test
    @DisplayName("어항 로그 목록 조회 성공")
    void getLogsByAquariumId_Success() {
        AquariumLog log1 = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now().minusDays(2))
                .build();
        AquariumLog log2 = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(26.0)
                .ph(7.2)
                .logDate(LocalDateTime.now().minusDays(1))
                .build();
        aquariumLogRepository.save(log1);
        aquariumLogRepository.save(log2);

        List<AquariumLogResponseDto> logs = aquariumLogService.getLogsByAquariumId(testAquarium.getId());

        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(AquariumLogResponseDto::getTemperature)
                .containsExactlyInAnyOrder(25.0, 26.0);
    }

    @Test
    @DisplayName("어항 로그 목록 조회 - 로그가 없을 때 빈 리스트 반환")
    void getLogsByAquariumId_EmptyList_WhenNoLogs() {
        List<AquariumLogResponseDto> logs = aquariumLogService.getLogsByAquariumId(testAquarium.getId());

        assertThat(logs).isEmpty();
    }

    @Test
    @DisplayName("어항 로그 수정 성공")
    void updateLog_Success() {
        AquariumLog existingLog = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now().minusDays(1))
                .build();
        aquariumLogRepository.save(existingLog);

        LocalDateTime newLogDate = LocalDateTime.now();
        AquariumLogRequestDto updateDto = AquariumLogRequestDto.builder()
                .aquariumId(testAquarium.getId())
                .temperature(27.0)
                .ph(7.5)
                .logDate(newLogDate)
                .build();

        AquariumLogResponseDto responseDto = aquariumLogService.updateLog(existingLog.getLogId(), updateDto);

        assertThat(responseDto.getTemperature()).isEqualTo(27.0);
        assertThat(responseDto.getPh()).isEqualTo(7.5);
        assertThat(responseDto.getLogDate()).isEqualTo(newLogDate);
        assertThat(responseDto.getLogId()).isEqualTo(existingLog.getLogId());

        AquariumLog updatedLog = aquariumLogRepository.findById(existingLog.getLogId())
                .orElseThrow();
        assertThat(updatedLog.getTemperature()).isEqualTo(27.0);
        assertThat(updatedLog.getPh()).isEqualTo(7.5);
    }

    @Test
    @DisplayName("어항 로그 수정 - temperature와 ph를 null로 변경 가능")
    void updateLog_WithNullValues_Success() {
        AquariumLog existingLog = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();
        aquariumLogRepository.save(existingLog);

        AquariumLogRequestDto updateDto = AquariumLogRequestDto.builder()
                .aquariumId(testAquarium.getId())
                .temperature(null)
                .ph(null)
                .logDate(LocalDateTime.now())
                .build();

        AquariumLogResponseDto responseDto = aquariumLogService.updateLog(existingLog.getLogId(), updateDto);

        assertThat(responseDto.getTemperature()).isNull();
        assertThat(responseDto.getPh()).isNull();
    }

    @Test
    @DisplayName("어항 로그 수정 실패 - 존재하지 않는 로그")
    void updateLog_Fail_WhenLogNotFound() {
        Long nonExistentLogId = 999L;
        AquariumLogRequestDto updateDto = AquariumLogRequestDto.builder()
                .aquariumId(testAquarium.getId())
                .temperature(27.0)
                .ph(7.5)
                .logDate(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> aquariumLogService.updateLog(nonExistentLogId, updateDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_LOG_NOT_FOUND);
    }

    @Test
    @DisplayName("어항 로그 수정 실패 - 존재하지 않는 어항")
    void updateLog_Fail_WhenAquariumNotFound() {
        AquariumLog existingLog = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();
        aquariumLogRepository.save(existingLog);

        Long nonExistentAquariumId = 999L;
        AquariumLogRequestDto updateDto = AquariumLogRequestDto.builder()
                .aquariumId(nonExistentAquariumId)
                .temperature(27.0)
                .ph(7.5)
                .logDate(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> aquariumLogService.updateLog(existingLog.getLogId(), updateDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_NOT_FOUND);
    }

    @Test
    @DisplayName("어항 로그 삭제 성공")
    void deleteLog_Success() {
        AquariumLog log = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();
        aquariumLogRepository.save(log);
        Long logId = log.getLogId();

        aquariumLogService.deleteLog(logId);

        assertThat(aquariumLogRepository.findById(logId)).isEmpty();
    }

    @Test
    @DisplayName("어항 로그 삭제 실패 - 존재하지 않는 로그")
    void deleteLog_Fail_WhenLogNotFound() {
        Long nonExistentLogId = 999L;

        assertThatThrownBy(() -> aquariumLogService.deleteLog(nonExistentLogId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_LOG_NOT_FOUND);
    }
}


