package org.example.backend.domain.fish.controller

import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.entity.FishLog
import org.example.backend.domain.fish.repository.FishLogRepository
import org.example.backend.domain.fish.repository.FishRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.LoginUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * 물고기 로그 컨트롤러 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class FishLogControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var fishLogRepository: FishLogRepository

    @Autowired
    private lateinit var fishRepository: FishRepository

    @Autowired
    private lateinit var aquariumRepository: AquariumRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var testMember: Member
    private lateinit var testAquarium: Aquarium
    private lateinit var testFish: Fish
    private lateinit var jwtToken: String
    private var fishId: Long? = null

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        jwtToken = loginUtil.createMemberAndGetToken("fishLog@test.com", "test1234", "fishLog", null)
        testMember = loginUtil.getMemberByEmail("fishLog@test.com")

        testAquarium = Aquarium(testMember, "테스트 어항")
        aquariumRepository.save(testAquarium)

        testFish = Fish(testAquarium, "금붕어", "테스트 물고기")
        fishRepository.save(testFish)
        fishId = testFish.id
        if (fishId == 0L) throw IllegalStateException("fishId is 0")
    }

    @Test
    @DisplayName("물고기 로그 생성 API 테스트")
    fun createLog_Success() {
        val requestBody = """
                {
                  "status": "건강함",
                  "logDate": "2024-01-01T10:00:00"
                }
                """.trimIndent()

        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(post("/api/fish/{fishId}/fishLog", id)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("물고기 기록이 생성되었습니다."))
            .andExpect(jsonPath("$.data.fishId").value(id))
            .andExpect(jsonPath("$.data.status").value("건강함"))
            .andExpect(jsonPath("$.data.aquariumId").value(testAquarium.id))
    }

    @Test
    @DisplayName("물고기 로그 생성 API - logDate 없이 생성")
    fun createLog_WithoutLogDate_Success() {
        val requestBody = """
                {
                  "status": "건강함"
                }
                """.trimIndent()

        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(post("/api/fish/{fishId}/fishLog", id)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("물고기 기록이 생성되었습니다."))
            .andExpect(jsonPath("$.data.status").value("건강함"))
            .andExpect(jsonPath("$.data.logDate").exists())
    }

    @Test
    @DisplayName("물고기 로그 생성 API - 필수 필드 검증 실패")
    fun createLog_Fail_WhenRequiredFieldsMissing() {
        val requestBody = """
                {
                  "logDate": "2024-01-01T10:00:00"
                }
                """.trimIndent()

        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(post("/api/fish/{fishId}/fishLog", id)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 API 테스트")
    fun getLogsByFishId_Success() {
        val log1 = FishLog(
            fish = testFish,
            status = "건강함",
            logDate = LocalDateTime.now().minusDays(2)
        )
        val log2 = FishLog(
            fish = testFish,
            status = "약간 아픔",
            logDate = LocalDateTime.now().minusDays(1)
        )
        fishLogRepository.save(log1)
        fishLogRepository.save(log2)

        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(get("/api/fish/{fishId}/fishLog", id)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("물고기 기록 목록이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].status").exists())
            .andExpect(jsonPath("$.data[1].status").exists())
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 API - 로그가 없을 때")
    fun getLogsByFishId_EmptyList() {
        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(get("/api/fish/{fishId}/fishLog", id)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("물고기 기록 목록이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(0))
    }

    @Test
    @DisplayName("물고기 로그 수정 API 테스트")
    fun updateLog_Success() {
        val existingLog = FishLog(
            fish = testFish,
            status = "건강함",
            logDate = LocalDateTime.now().minusDays(1)
        )
        fishLogRepository.save(existingLog)

        val requestBody = """
                {
                  "status": "아픔",
                  "logDate": "2024-01-02T10:00:00"
                }
                """.trimIndent()

        val id = fishId ?: throw IllegalStateException("fishId is null")
        val logId = existingLog.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        mvc.perform(put("/api/fish/{fishId}/fishLog/{logId}", id, logId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("물고기 기록이 수정되었습니다."))
            .andExpect(jsonPath("$.data.status").value("아픔"))
            .andExpect(jsonPath("$.data.logId").value(logId))
    }

    @Test
    @DisplayName("물고기 로그 삭제 API 테스트")
    fun deleteLog_Success() {
        val log = FishLog(
            fish = testFish,
            status = "건강함",
            logDate = LocalDateTime.now()
        )
        fishLogRepository.save(log)

        val id = fishId ?: throw IllegalStateException("fishId is null")
        val logId = log.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        mvc.perform(delete("/api/fish/{fishId}/fishLog/{logId}", id, logId)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("물고기 기록이 삭제되었습니다."))

        assertThat(fishLogRepository.findById(logId)).isEmpty
    }

    @Test
    @DisplayName("물고기 로그 생성 실패 - 존재하지 않는 물고기")
    fun createLog_Fail_WhenFishNotFound() {
        val nonExistentFishId = 999L
        val requestBody = """
                {
                  "status": "건강함",
                  "logDate": "2024-01-01T10:00:00"
                }
                """.trimIndent()

        mvc.perform(post("/api/fish/{fishId}/fishLog", nonExistentFishId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("물고기 로그 수정 실패 - 존재하지 않는 로그")
    fun updateLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L
        val requestBody = """
                {
                  "status": "아픔",
                  "logDate": "2024-01-02T10:00:00"
                }
                """.trimIndent()

        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(put("/api/fish/{fishId}/fishLog/{logId}", id, nonExistentLogId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("물고기 로그 삭제 실패 - 존재하지 않는 로그")
    fun deleteLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L

        val id = fishId ?: throw IllegalStateException("fishId is null")
        mvc.perform(delete("/api/fish/{fishId}/fishLog/{logId}", id, nonExistentLogId)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isNotFound)
    }
}

