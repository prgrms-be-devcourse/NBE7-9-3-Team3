package org.example.backend.domain.aquarium.controller;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.entity.AquariumLog;
import org.example.backend.domain.aquarium.repository.AquariumLogRepository;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.LoginUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AquariumLogControllerTest {

    @Autowired
    private MockMvc mvc;

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
    private String jwtToken;

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        jwtToken = loginUtil.createMemberAndGetToken("aquariumLog@test.com", "test1234", "aquariumLog", null);
        testMember = loginUtil.getMemberByEmail("aquariumLog@test.com");

        testAquarium = new Aquarium(testMember, "테스트 어항");
        aquariumRepository.save(testAquarium);
    }

    @Test
    @DisplayName("어항 로그 생성 API 테스트")
    void createLog_Success() throws Exception {

        String requestBody = """
                {
                  "temperature": 25.5,
                  "ph": 7.0,
                  "logDate": "2024-01-01T10:00:00"
                }
                """;


        mvc.perform(post("/api/aquarium/{aquariumId}/aquariumLog", testAquarium.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록이 생성되었습니다."))
                .andExpect(jsonPath("$.data.aquariumId").value(testAquarium.getId()))
                .andExpect(jsonPath("$.data.temperature").value(25.5))
                .andExpect(jsonPath("$.data.ph").value(7.0));
    }

    @Test
    @DisplayName("어항 로그 생성 API - temperature와 ph 없이 생성")
    void createLog_WithoutTemperatureAndPh_Success() throws Exception {

        String requestBody = """
                {
                  "logDate": "2024-01-01T10:00:00"
                }
                """;


        mvc.perform(post("/api/aquarium/{aquariumId}/aquariumLog", testAquarium.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록이 생성되었습니다."))
                .andExpect(jsonPath("$.data.aquariumId").value(testAquarium.getId()));
    }

    @Test
    @DisplayName("어항 로그 목록 조회 API 테스트")
    void getLogsByAquariumId_Success() throws Exception {

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


        mvc.perform(get("/api/aquarium/{aquariumId}/aquariumLog", testAquarium.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록 목록이 조회되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].temperature").exists())
                .andExpect(jsonPath("$.data[1].temperature").exists());
    }

    @Test
    @DisplayName("어항 로그 목록 조회 API - 로그가 없을 때")
    void getLogsByAquariumId_EmptyList() throws Exception {

        mvc.perform(get("/api/aquarium/{aquariumId}/aquariumLog", testAquarium.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록 목록이 조회되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("어항 로그 수정 API 테스트")
    void updateLog_Success() throws Exception {

        AquariumLog existingLog = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now().minusDays(1))
                .build();
        aquariumLogRepository.save(existingLog);

        String requestBody = """
                {
                  "temperature": 27.0,
                  "ph": 7.5,
                  "logDate": "2024-01-02T10:00:00"
                }
                """;


        mvc.perform(put("/api/aquarium/{aquariumId}/aquariumLog/{logId}", testAquarium.getId(), existingLog.getLogId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록이 수정되었습니다."))
                .andExpect(jsonPath("$.data.temperature").value(27.0))
                .andExpect(jsonPath("$.data.ph").value(7.5))
                .andExpect(jsonPath("$.data.logId").value(existingLog.getLogId()));
    }

    @Test
    @DisplayName("어항 로그 수정 API - temperature와 ph를 null로 변경")
    void updateLog_WithNullValues_Success() throws Exception {

        AquariumLog existingLog = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();
        aquariumLogRepository.save(existingLog);

        String requestBody = """
                {
                  "temperature": null,
                  "ph": null,
                  "logDate": "2024-01-02T10:00:00"
                }
                """;


        mvc.perform(put("/api/aquarium/{aquariumId}/aquariumLog/{logId}", testAquarium.getId(), existingLog.getLogId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록이 수정되었습니다."))
                .andExpect(jsonPath("$.data.temperature").isEmpty())
                .andExpect(jsonPath("$.data.ph").isEmpty());
    }

    @Test
    @DisplayName("어항 로그 삭제 API 테스트")
    void deleteLog_Success() throws Exception {

        AquariumLog log = AquariumLog.builder()
                .aquarium(testAquarium)
                .temperature(25.0)
                .ph(7.0)
                .logDate(LocalDateTime.now())
                .build();
        aquariumLogRepository.save(log);


        mvc.perform(delete("/api/aquarium/{aquariumId}/aquariumLog/{logId}", testAquarium.getId(), log.getLogId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("어항 기록이 삭제되었습니다."));

        assertThat(aquariumLogRepository.findById(log.getLogId())).isEmpty();
    }

    @Test
    @DisplayName("어항 로그 생성 실패 - 존재하지 않는 어항")
    void createLog_Fail_WhenAquariumNotFound() throws Exception {

        Long nonExistentAquariumId = 999L;
        String requestBody = """
                {
                  "temperature": 25.5,
                  "ph": 7.0,
                  "logDate": "2024-01-01T10:00:00"
                }
                """;


        mvc.perform(post("/api/aquarium/{aquariumId}/aquariumLog", nonExistentAquariumId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("어항 로그 수정 실패 - 존재하지 않는 로그")
    void updateLog_Fail_WhenLogNotFound() throws Exception {

        Long nonExistentLogId = 999L;
        String requestBody = """
                {
                  "temperature": 27.0,
                  "ph": 7.5,
                  "logDate": "2024-01-02T10:00:00"
                }
                """;


        mvc.perform(put("/api/aquarium/{aquariumId}/aquariumLog/{logId}", testAquarium.getId(), nonExistentLogId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("어항 로그 삭제 실패 - 존재하지 않는 로그")
    void deleteLog_Fail_WhenLogNotFound() throws Exception {

        Long nonExistentLogId = 999L;


        mvc.perform(delete("/api/aquarium/{aquariumId}/aquariumLog/{logId}", testAquarium.getId(), nonExistentLogId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
}


