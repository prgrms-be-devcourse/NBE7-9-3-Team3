/*
package org.example.backend.domain.fish.service

import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.dto.FishLogRequestDto
import org.example.backend.domain.fish.dto.FishLogResponseDto
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.entity.FishLog
import org.example.backend.domain.fish.repository.FishLogRepository
import org.example.backend.domain.fish.repository.FishRepository
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
 * 물고기 로그 서비스 테스트
 *//*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FishLogServiceTest {

    @Autowired
    private lateinit var fishLogService: FishLogService

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

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        testMember = loginUtil.createMember("fishLogService@test.com", "test1234", "fishLogService", null)

        testAquarium = Aquarium(testMember, "테스트 어항")
        aquariumRepository.save(testAquarium)

        testFish = Fish(testAquarium, "금붕어", "테스트 물고기")
        fishRepository.save(testFish)
    }

    @Test
    @DisplayName("물고기 로그 생성 성공")
    fun createLog_Success() {
        val requestDto = FishLogRequestDto(
            fishId = testFish.id,
            status = "건강함",
            logDate = LocalDateTime.now()
        )

        val responseDto = fishLogService.createLog(requestDto)

        assertThat(responseDto).isNotNull
        assertThat(responseDto.fishId).isEqualTo(testFish.id)
        assertThat(responseDto.status).isEqualTo("건강함")
        assertThat(responseDto.aquariumId).isEqualTo(testAquarium.id)
        assertThat(responseDto.logDate).isNotNull

        val savedLog = fishLogRepository.findById(responseDto.logId)
            .orElseThrow { IllegalStateException("Log not found") }
        assertThat(savedLog.status).isEqualTo("건강함")
    }

    @Test
    @DisplayName("물고기 로그 생성 - logDate가 null일 때 자동으로 현재 시간 설정")
    fun createLog_WithNullLogDate_SetsCurrentTime() {
        val requestDto = FishLogRequestDto(
            fishId = testFish.id,
            status = "건강함",
            logDate = null
        )

        val responseDto = fishLogService.createLog(requestDto)

        assertThat(responseDto.logDate).isNotNull
        assertThat(responseDto.logDate).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    @DisplayName("물고기 로그 생성 실패 - 존재하지 않는 물고기")
    fun createLog_Fail_WhenFishNotFound() {
        val nonExistentFishId = 999L
        val requestDto = FishLogRequestDto(
            fishId = nonExistentFishId,
            status = "건강함",
            logDate = LocalDateTime.now()
        )

        assertThatThrownBy { fishLogService.createLog(requestDto) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_NOT_FOUND)
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 성공")
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

        val logs = fishLogService.getLogsByFishId(testFish.id)

        assertThat(logs).hasSize(2)
        assertThat(logs).extracting { it.status }
            .containsExactlyInAnyOrder("건강함", "약간 아픔")
    }

    @Test
    @DisplayName("물고기 로그 목록 조회 - 로그가 없을 때 빈 리스트 반환")
    fun getLogsByFishId_EmptyList_WhenNoLogs() {
        val logs = fishLogService.getLogsByFishId(testFish.id)

        assertThat(logs).isEmpty()
    }

    @Test
    @DisplayName("물고기 로그 수정 성공")
    fun updateLog_Success() {
        val existingLog = FishLog(
            fish = testFish,
            status = "건강함",
            logDate = LocalDateTime.now().minusDays(1)
        )
        fishLogRepository.save(existingLog)

        val newLogDate = LocalDateTime.now()
        val updateDto = FishLogRequestDto(
            fishId = testFish.id,
            status = "아픔",
            logDate = newLogDate
        )

        val logId = existingLog.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        val responseDto = fishLogService.updateLog(logId, updateDto)

        assertThat(responseDto.status).isEqualTo("아픔")
        assertThat(responseDto.logDate).isEqualTo(newLogDate)
        assertThat(responseDto.logId).isEqualTo(existingLog.logId)

        val updatedLog = fishLogRepository.findById(logId)
            .orElseThrow { IllegalStateException("Log not found") }
        assertThat(updatedLog.status).isEqualTo("아픔")
    }

    @Test
    @DisplayName("물고기 로그 수정 실패 - 존재하지 않는 로그")
    fun updateLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L
        val updateDto = FishLogRequestDto(
            fishId = testFish.id,
            status = "아픔",
            logDate = LocalDateTime.now()
        )

        assertThatThrownBy { fishLogService.updateLog(nonExistentLogId, updateDto) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_LOG_NOT_FOUND)
    }

    @Test
    @DisplayName("물고기 로그 수정 실패 - 존재하지 않는 물고기")
    fun updateLog_Fail_WhenFishNotFound() {
        val existingLog = FishLog(
            fish = testFish,
            status = "건강함",
            logDate = LocalDateTime.now()
        )
        fishLogRepository.save(existingLog)

        val nonExistentFishId = 999L
        val updateDto = FishLogRequestDto(
            fishId = nonExistentFishId,
            status = "아픔",
            logDate = LocalDateTime.now()
        )

        val logId = existingLog.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")
        assertThatThrownBy { fishLogService.updateLog(logId, updateDto) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_NOT_FOUND)
    }

    @Test
    @DisplayName("물고기 로그 삭제 성공")
    fun deleteLog_Success() {
        val log = FishLog(
            fish = testFish,
            status = "건강함",
            logDate = LocalDateTime.now()
        )
        fishLogRepository.save(log)
        val logId = log.logId
        if (logId == 0L) throw IllegalStateException("logId is 0")

        fishLogService.deleteLog(logId)

        assertThat(fishLogRepository.findById(logId)).isEmpty
    }

    @Test
    @DisplayName("물고기 로그 삭제 실패 - 존재하지 않는 로그")
    fun deleteLog_Fail_WhenLogNotFound() {
        val nonExistentLogId = 999L

        assertThatThrownBy { fishLogService.deleteLog(nonExistentLogId) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FISH_LOG_NOT_FOUND)
    }
}

*/
