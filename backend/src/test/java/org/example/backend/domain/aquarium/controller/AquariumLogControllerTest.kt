package org.example.backend.domain.aquarium.controller

import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.entity.AquariumLog
import org.example.backend.domain.aquarium.repository.AquariumLogRepository
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.aquarium.util.getIdSafely
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
 * 어항 로그 컨트롤러 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AquariumLogControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var aquariumLogRepository: AquariumLogRepository

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
    private lateinit var jwtToken: String
    // 임시 코드: Aquarium이 Kotlin으로 전환되면 Long? 대신 Long으로 변경 가능
    // lateinit은 primitive 타입에 사용할 수 없으므로 nullable로 선언
    private var aquariumId: Long? = null

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        jwtToken = loginUtil.createMemberAndGetToken("aquariumLog@test.com", "test1234", "aquariumLog", null)
        testMember = loginUtil.getMemberByEmail("aquariumLog@test.com")

        testAquarium = Aquarium(testMember, "테스트 어항")
        aquariumRepository.save(testAquarium)
        aquariumId = testAquarium.getIdSafely() ?: throw IllegalStateException("aquariumId is null")
    }

    @Test
    @DisplayName("어항 로그 생성 API 테스트")
    fun createLog_Success() {
        val requestBody = """
                {
                  "temperature": 25.5,
                  "ph": 7.0,
                  "logDate": "2024-01-01T10:00:00"
                }
                """.trimIndent()

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        mvc.perform(post("/api/aquarium/{aquariumId}/aquariumLog", id)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록이 생성되었습니다."))
            .andExpect(jsonPath("$.data.aquariumId").value(id))
            .andExpect(jsonPath("$.data.temperature").value(25.5))
            .andExpect(jsonPath("$.data.ph").value(7.0))
    }

    @Test
    @DisplayName("어항 로그 생성 API - temperature와 ph 없이 생성")
    fun createLog_WithoutTemperatureAndPh_Success() {
        val requestBody = """
                {
                  "logDate": "2024-01-01T10:00:00"
                }
                """.trimIndent()

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        mvc.perform(post("/api/aquarium/{aquariumId}/aquariumLog", id)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록이 생성되었습니다."))
            .andExpect(jsonPath("$.data.aquariumId").value(id))
    }

    @Test
    @DisplayName("어항 로그 목록 조회 API 테스트")
    fun getLogsByAquariumId_Success() {
        val log1 = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now().minusDays(2)
        )
        val log2 = AquariumLog(
            aquarium = testAquarium,
            temperature = 26.0,
            ph = 7.2,
            logDate = LocalDateTime.now().minusDays(1)
        )
        aquariumLogRepository.save(log1)
        aquariumLogRepository.save(log2)

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        mvc.perform(get("/api/aquarium/{aquariumId}/aquariumLog", id)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록 목록이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].temperature").exists())
            .andExpect(jsonPath("$.data[1].temperature").exists())
    }

    @Test
    @DisplayName("어항 로그 목록 조회 API - 로그가 없을 때")
    fun getLogsByAquariumId_EmptyList() {
        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        mvc.perform(get("/api/aquarium/{aquariumId}/aquariumLog", id)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록 목록이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(0))
    }

    @Test
    @DisplayName("어항 로그 수정 API 테스트")
    fun updateLog_Success() {
        val existingLog = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now().minusDays(1)
        )
        aquariumLogRepository.save(existingLog)

        val requestBody = """
                {
                  "temperature": 27.0,
                  "ph": 7.5,
                  "logDate": "2024-01-02T10:00:00"
                }
                """.trimIndent()

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        val logId = existingLog.logId ?: throw IllegalStateException("logId is null")
        mvc.perform(put("/api/aquarium/{aquariumId}/aquariumLog/{logId}", id, logId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록이 수정되었습니다."))
            .andExpect(jsonPath("$.data.temperature").value(27.0))
            .andExpect(jsonPath("$.data.ph").value(7.5))
            .andExpect(jsonPath("$.data.logId").value(logId))
    }

    @Test
    @DisplayName("어항 로그 수정 API - temperature와 ph를 null로 변경")
    fun updateLog_WithNullValues_Success() {
        val existingLog = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )
        aquariumLogRepository.save(existingLog)

        val requestBody = """
                {
                  "temperature": null,
                  "ph": null,
                  "logDate": "2024-01-02T10:00:00"
                }
                """.trimIndent()

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        val logId = existingLog.logId ?: throw IllegalStateException("logId is null")
        mvc.perform(put("/api/aquarium/{aquariumId}/aquariumLog/{logId}", id, logId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록이 수정되었습니다."))
            .andExpect(jsonPath("$.data.temperature").isEmpty)
            .andExpect(jsonPath("$.data.ph").isEmpty)
    }

    @Test
    @DisplayName("어항 로그 삭제 API 테스트")
    fun deleteLog_Success() {
        val log = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )
        aquariumLogRepository.save(log)

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        val logId = log.logId ?: throw IllegalStateException("logId is null")
        mvc.perform(delete("/api/aquarium/{aquariumId}/aquariumLog/{logId}", id, logId)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("어항 기록이 삭제되었습니다."))

        assertThat(aquariumLogRepository.findById(logId)).isEmpty
    }

    @Test
    @DisplayName("어항 로그 생성 실패 - 존재하지 않는 어항")
    fun createLog_Fail_WhenAquariumNotFound() {
        val nonExistentAquariumId = 999L
        val requestBody = """
                {
                  "temperature": 25.5,
                  "ph": 7.0,
                  "logDate": "2024-01-01T10:00:00"
                }
                """.trimIndent()

        mvc.perform(post("/api/aquarium/{aquariumId}/aquariumLog", nonExistentAquariumId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("어항 로그 수정 실패 - 존재하지 않는 로그")
    fun updateLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L
        val requestBody = """
                {
                  "temperature": 27.0,
                  "ph": 7.5,
                  "logDate": "2024-01-02T10:00:00"
                }
                """.trimIndent()

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        mvc.perform(put("/api/aquarium/{aquariumId}/aquariumLog/{logId}", id, nonExistentLogId)
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("어항 로그 삭제 실패 - 존재하지 않는 로그")
    fun deleteLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L

        val id = aquariumId ?: throw IllegalStateException("aquariumId is null")
        mvc.perform(delete("/api/aquarium/{aquariumId}/aquariumLog/{logId}", id, nonExistentLogId)
                .header("Authorization", "Bearer $jwtToken"))
            .andExpect(status().isNotFound)
    }
}
