/*
package org.example.backend.domain.aquarium.service

import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.entity.AquariumLog
import org.example.backend.domain.aquarium.repository.AquariumLogRepository
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.aquarium.util.getIdSafely
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.LoginUtil
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy

*/
/**
 * 어항 로그 서비스 테스트
 *//*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AquariumLogServiceTest {

    @Autowired
    private lateinit var aquariumLogService: AquariumLogService

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

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        testMember = loginUtil.createMember("aquariumLogService@test.com", "test1234", "aquariumLogService", null)

        testAquarium = Aquarium(testMember, "테스트 어항")
        aquariumRepository.save(testAquarium)
    }

    @Test
    @DisplayName("어항 로그 생성 성공")
    fun createLog_Success() {
        val requestDto = AquariumLogRequestDto(
            aquariumId = testAquarium.getIdSafely(),
            temperature = 25.5,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )

        val responseDto = aquariumLogService.createLog(requestDto)

        assertThat(responseDto).isNotNull
        assertThat(responseDto.aquariumId).isEqualTo(testAquarium.getIdSafely())
        assertThat(responseDto.temperature).isEqualTo(25.5)
        assertThat(responseDto.ph).isEqualTo(7.0)
        assertThat(responseDto.logDate).isNotNull

        val savedLog = aquariumLogRepository.findById(responseDto.logId)
            .orElseThrow { IllegalStateException("Log not found") }
        assertThat(savedLog.temperature).isEqualTo(25.5)
        assertThat(savedLog.ph).isEqualTo(7.0)
    }

    @Test
    @DisplayName("어항 로그 생성 - temperature와 ph가 null일 수 있음")
    fun createLog_WithNullValues_Success() {
        val requestDto = AquariumLogRequestDto(
            aquariumId = testAquarium.getIdSafely(),
            temperature = null,
            ph = null,
            logDate = LocalDateTime.now()
        )

        val responseDto = aquariumLogService.createLog(requestDto)

        assertThat(responseDto).isNotNull
        assertThat(responseDto.temperature).isNull()
        assertThat(responseDto.ph).isNull()
    }

    @Test
    @DisplayName("어항 로그 생성 실패 - 존재하지 않는 어항")
    fun createLog_Fail_WhenAquariumNotFound() {
        val nonExistentAquariumId = 999L
        val requestDto = AquariumLogRequestDto(
            aquariumId = nonExistentAquariumId,
            temperature = 25.5,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )

        assertThatThrownBy { aquariumLogService.createLog(requestDto) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_NOT_FOUND)
    }

    @Test
    @DisplayName("어항 로그 목록 조회 성공")
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

        val logs = aquariumLogService.getLogsByAquariumId(testAquarium.getIdSafely() ?: throw IllegalStateException("aquariumId is null"))

        assertThat(logs).hasSize(2)
        assertThat(logs).extracting { it.temperature }
            .containsExactlyInAnyOrder(25.0, 26.0)
    }

    @Test
    @DisplayName("어항 로그 목록 조회 - 로그가 없을 때 빈 리스트 반환")
    fun getLogsByAquariumId_EmptyList_WhenNoLogs() {
        val logs = aquariumLogService.getLogsByAquariumId(testAquarium.getIdSafely() ?: throw IllegalStateException("aquariumId is null"))

        assertThat(logs).isEmpty()
    }

    @Test
    @DisplayName("어항 로그 수정 성공")
    fun updateLog_Success() {
        val existingLog = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now().minusDays(1)
        )
        aquariumLogRepository.save(existingLog)

        val newLogDate = LocalDateTime.now()
        val updateDto = AquariumLogRequestDto(
            aquariumId = testAquarium.getIdSafely(),
            temperature = 27.0,
            ph = 7.5,
            logDate = newLogDate
        )

        val logId = existingLog.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        val responseDto = aquariumLogService.updateLog(logId, updateDto)

        assertThat(responseDto.temperature).isEqualTo(27.0)
        assertThat(responseDto.ph).isEqualTo(7.5)
        assertThat(responseDto.logDate).isEqualTo(newLogDate)
        assertThat(responseDto.logId).isEqualTo(existingLog.logId)

        val updatedLog = aquariumLogRepository.findById(logId)
            .orElseThrow { IllegalStateException("Log not found") }
        assertThat(updatedLog.temperature).isEqualTo(27.0)
        assertThat(updatedLog.ph).isEqualTo(7.5)
    }

    @Test
    @DisplayName("어항 로그 수정 - temperature와 ph를 null로 변경 가능")
    fun updateLog_WithNullValues_Success() {
        val existingLog = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )
        aquariumLogRepository.save(existingLog)

        val updateDto = AquariumLogRequestDto(
            aquariumId = testAquarium.getIdSafely(),
            temperature = null,
            ph = null,
            logDate = LocalDateTime.now()
        )

        val logId = existingLog.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        val responseDto = aquariumLogService.updateLog(logId, updateDto)

        assertThat(responseDto.temperature).isNull()
        assertThat(responseDto.ph).isNull()
    }

    @Test
    @DisplayName("어항 로그 수정 실패 - 존재하지 않는 로그")
    fun updateLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L
        val updateDto = AquariumLogRequestDto(
            aquariumId = testAquarium.getIdSafely(),
            temperature = 27.0,
            ph = 7.5,
            logDate = LocalDateTime.now()
        )

        assertThatThrownBy { aquariumLogService.updateLog(nonExistentLogId, updateDto) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_LOG_NOT_FOUND)
    }

    @Test
    @DisplayName("어항 로그 수정 실패 - 존재하지 않는 어항")
    fun updateLog_Fail_WhenAquariumNotFound() {
        val existingLog = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )
        aquariumLogRepository.save(existingLog)

        val nonExistentAquariumId = 999L
        val updateDto = AquariumLogRequestDto(
            aquariumId = nonExistentAquariumId,
            temperature = 27.0,
            ph = 7.5,
            logDate = LocalDateTime.now()
        )

        val logId = existingLog.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        assertThatThrownBy { aquariumLogService.updateLog(logId, updateDto) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_NOT_FOUND)
    }

    @Test
    @DisplayName("어항 로그 삭제 성공")
    fun deleteLog_Success() {
        val log = AquariumLog(
            aquarium = testAquarium,
            temperature = 25.0,
            ph = 7.0,
            logDate = LocalDateTime.now()
        )
        aquariumLogRepository.save(log)
        val logId = log.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")

        aquariumLogService.deleteLog(logId)

        assertThat(aquariumLogRepository.findById(logId)).isEmpty
    }

    @Test
    @DisplayName("어항 로그 삭제 실패 - 존재하지 않는 로그")
    fun deleteLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L

        assertThatThrownBy { aquariumLogService.deleteLog(nonExistentLogId) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AQUARIUM_LOG_NOT_FOUND)
    }
}
*/
