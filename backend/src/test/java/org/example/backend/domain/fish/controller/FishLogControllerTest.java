package org.example.backend.domain.fish.controller;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.entity.FishLog;
import org.example.backend.domain.fish.repository.FishLogRepository;
import org.example.backend.domain.fish.repository.FishRepository;
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
class FishLogControllerTest {

    @Autowired
    private MockMvc mvc;

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
    private String jwtToken;

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        jwtToken = loginUtil.createMemberAndGetToken("fishLog@test.com", "test1234", "fishLog", null);
        testMember = loginUtil.getMemberByEmail("fishLog@test.com");

        testAquarium = new Aquarium(testMember, "테스트 어항");
        aquariumRepository.save(testAquarium);

        testFish = new Fish(testAquarium, "금붕어", "테스트 물고기");
        fishRepository.save(testFish);
    }

    @Test
    @DisplayName("물고기 로그 생성 API 테스트")
    void createLog_Success() throws Exception {
        String requestBody = """
                {
                  "status": "건강함",
                  "logDate": "2024-01-01T10:00:00"
                }
                """;

        mvc.perform(post("/api/fish/{fishId}/fishLog", testFish.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("물고기 기록이 생성되었습니다."))
                .andExpect(jsonPath("$.data.fishId").value(testFish.getId()))
                .andExpect(jsonPath("$.data.status").value("건강함"))
                .andExpect(jsonPath("$.data.aquariumId").value(testAquarium.getId()));
    }

    @Test
    @DisplayName("물고기 로그 생성 API - logDate 없이 생성")
    void createLog_WithoutLogDate_Success() throws Exception {
        String requestBody = """
                {
                  "status": "건강함"
                }
                """;

        mvc.perform(post("/api/fish/{fishId}/fishLog", testFish.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("물고기 기록이 생성되었습니다."))
                .andExpect(jsonPath("$.data.status").value("건강함"))
                .andExpect(jsonPath("$.data.logDate").exists());
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 API 테스트")
    void getLogsByFishId_Success() throws Exception {
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

        mvc.perform(get("/api/fish/{fishId}/fishLog", testFish.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("물고기 기록 목록이 조회되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").exists())
                .andExpect(jsonPath("$.data[1].status").exists());
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 API - 로그가 없을 때")
    void getLogsByFishId_EmptyList() throws Exception {
        mvc.perform(get("/api/fish/{fishId}/fishLog", testFish.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("물고기 기록 목록이 조회되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("물고기 로그 수정 API 테스트")
    void updateLog_Success() throws Exception {
        FishLog existingLog = FishLog.builder()
                .fish(testFish)
                .status("건강함")
                .logDate(LocalDateTime.now().minusDays(1))
                .build();
        fishLogRepository.save(existingLog);

        String requestBody = """
                {
                  "status": "아픔",
                  "logDate": "2024-01-02T10:00:00"
                }
                """;

        mvc.perform(put("/api/fish/{fishId}/fishLog/{logId}", testFish.getId(), existingLog.getLogId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("물고기 기록이 수정되었습니다."))
                .andExpect(jsonPath("$.data.status").value("아픔"))
                .andExpect(jsonPath("$.data.logId").value(existingLog.getLogId()));
    }

    @Test
    @DisplayName("물고기 로그 삭제 API 테스트")
    void deleteLog_Success() throws Exception {
        FishLog log = FishLog.builder()
                .fish(testFish)
                .status("건강함")
                .logDate(LocalDateTime.now())
                .build();
        fishLogRepository.save(log);

        mvc.perform(delete("/api/fish/{fishId}/fishLog/{logId}", testFish.getId(), log.getLogId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("물고기 기록이 삭제되었습니다."));

        assertThat(fishLogRepository.findById(log.getLogId())).isEmpty();
    }

    @Test
    @DisplayName("물고기 로그 생성 실패 - 존재하지 않는 물고기")
    void createLog_Fail_WhenFishNotFound() throws Exception {
        Long nonExistentFishId = 999L;
        String requestBody = """
                {
                  "status": "건강함",
                  "logDate": "2024-01-01T10:00:00"
                }
                """;

        mvc.perform(post("/api/fish/{fishId}/fishLog", nonExistentFishId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("물고기 로그 수정 실패 - 존재하지 않는 로그")
    void updateLog_Fail_WhenLogNotFound() throws Exception {
        Long nonExistentLogId = 999L;
        String requestBody = """
                {
                  "status": "아픔",
                  "logDate": "2024-01-02T10:00:00"
                }
                """;

        mvc.perform(put("/api/fish/{fishId}/fishLog/{logId}", testFish.getId(), nonExistentLogId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("물고기 로그 삭제 실패 - 존재하지 않는 로그")
    void deleteLog_Fail_WhenLogNotFound() throws Exception {
        Long nonExistentLogId = 999L;

        mvc.perform(delete("/api/fish/{fishId}/fishLog/{logId}", testFish.getId(), nonExistentLogId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
}

