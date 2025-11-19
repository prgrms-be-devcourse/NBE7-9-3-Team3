package org.example.backend.domain.notification.service

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.LoginUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDateTime

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("어항 알림 서비스 테스트")
class AquariumNotificationServiceTest {

    @Autowired
    private lateinit var notificationService: AquariumNotificationService

    @Autowired
    private lateinit var aquariumRepository: AquariumRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @MockitoBean
    private lateinit var emailService: EmailService

    private lateinit var testMember: Member
    private lateinit var testAquarium: Aquarium

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        testMember = loginUtil.createMember("notification@test.com", "test1234", "notification", "")

        testAquarium = Aquarium(testMember, "테스트어항")
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(7))
        aquariumRepository.save(testAquarium)
    }

    @Test
    @DisplayName("일일 알림 발송 - 알림 대상 어항이 없는 경우")
    fun sendDailyAquariumReminders_NoAquariums() {
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(7))
        aquariumRepository.save(testAquarium)
        doNothing().`when`(emailService).sendAquariumReminderEmail(any())

        notificationService.sendDailyAquariumReminders()

        verify(emailService, never()).sendAquariumReminderEmail(any())
    }

    @Test
    @DisplayName("일일 알림 발송 - 알림 대상 어항이 있는 경우")
    fun sendDailyAquariumReminders_WithAquariums() {
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1))
        aquariumRepository.save(testAquarium)
        doNothing().`when`(emailService).sendAquariumReminderEmail(any())

        notificationService.sendDailyAquariumReminders()

        verify(emailService, times(1)).sendAquariumReminderEmail(any())
        val updatedAquarium = aquariumRepository.findById(testAquarium.id).orElseThrow()
        assertNotNull(updatedAquarium.nextDate)
        updatedAquarium.nextDate?.let {
            assertTrue(it.isAfter(LocalDateTime.now()))
        } ?: fail("nextDate should not be null")
    }

    @Test
    @DisplayName("일일 알림 발송 - 여러 어항에 대한 알림 발송")
    fun sendDailyAquariumReminders_MultipleAquariums() {
        val aquarium1 = Aquarium(testMember, "어항1")
        aquarium1.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1))
        aquariumRepository.save(aquarium1)
        
        val aquarium2 = Aquarium(testMember, "어항2")
        aquarium2.changeSchedule(14, LocalDateTime.now().minusDays(14), LocalDateTime.now().minusDays(1))
        aquariumRepository.save(aquarium2)

        doNothing().`when`(emailService).sendAquariumReminderEmail(any())

        notificationService.sendDailyAquariumReminders()

        verify(emailService, times(2)).sendAquariumReminderEmail(any())
        val updatedAquarium1 = aquariumRepository.findById(aquarium1.id).orElseThrow()
        val updatedAquarium2 = aquariumRepository.findById(aquarium2.id).orElseThrow()
        assertNotNull(updatedAquarium1.nextDate)
        assertNotNull(updatedAquarium2.nextDate)
    }

    @Test
    @DisplayName("일일 알림 발송 - 이메일 발송 실패 시에도 다음 어항 처리 계속")
    fun sendDailyAquariumReminders_EmailFailure_ContinueProcessing() {
        val aquarium1 = Aquarium(testMember, "어항1")
        aquarium1.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1))
        aquariumRepository.save(aquarium1)
        
        val aquarium2 = Aquarium(testMember, "어항2")
        aquarium2.changeSchedule(14, LocalDateTime.now().minusDays(14), LocalDateTime.now().minusDays(1))
        aquariumRepository.save(aquarium2)
        
        doThrow(RuntimeException("이메일 발송 실패"))
            .doNothing()
            .`when`(emailService).sendAquariumReminderEmail(any())

        notificationService.sendDailyAquariumReminders()

        verify(emailService, times(2)).sendAquariumReminderEmail(any())
        val updatedAquarium2 = aquariumRepository.findById(aquarium2.id).orElseThrow()
        assertNotNull(updatedAquarium2.nextDate)
    }

    @Test
    @DisplayName("테스트 알림 발송 - 성공")
    fun sendTestNotification_Success() {
        val aquariumId = testAquarium.id
        doNothing().`when`(emailService).sendAquariumReminderEmail(any())

        notificationService.sendTestNotification(aquariumId)

        verify(emailService, times(1)).sendAquariumReminderEmail(any())
    }

    @Test
    @DisplayName("테스트 알림 발송 - 어항을 찾을 수 없는 경우")
    fun sendTestNotification_AquariumNotFound() {
        val aquariumId = 999L

        val exception = assertThrows(IllegalArgumentException::class.java) {
            notificationService.sendTestNotification(aquariumId)
        }

        assertEquals("어항을 찾을 수 없습니다. ID: $aquariumId", exception.message)
        verify(emailService, never()).sendAquariumReminderEmail(any())
    }

    @Test
    @DisplayName("테스트 알림 발송 - 알림이 비활성화된 어항")
    fun sendTestNotification_NotificationDisabled() {
        val disabledAquarium = Aquarium(testMember, "비활성화어항")
        disabledAquarium.changeSchedule(0, null, null)
        aquariumRepository.save(disabledAquarium)
        val aquariumId = disabledAquarium.id

        val exception = assertThrows(IllegalArgumentException::class.java) {
            notificationService.sendTestNotification(aquariumId)
        }

        assertEquals("알림이 비활성화된 어항입니다. 관리주기를 설정해주세요.", exception.message)
        verify(emailService, never()).sendAquariumReminderEmail(any())
    }

    @Test
    @DisplayName("수동 알림 발송 - sendAllNotifications 호출")
    fun sendAllNotifications_Success() {
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1))
        aquariumRepository.save(testAquarium)
        doNothing().`when`(emailService).sendAquariumReminderEmail(any())

        notificationService.sendAllNotifications()

        verify(emailService, times(1)).sendAquariumReminderEmail(any())
    }

    @Test
    @DisplayName("다음 알림일 업데이트 - 성공")
    fun updateNextNotificationDate_Success() {
        val cycleDate = 7
        testAquarium.changeSchedule(cycleDate, null, null)
        aquariumRepository.save(testAquarium)

        notificationService.updateNextNotificationDate(testAquarium)

        val updatedAquarium = aquariumRepository.findById(testAquarium.id).orElseThrow()
        assertNotNull(updatedAquarium.lastDate)
        assertNotNull(updatedAquarium.nextDate)
        assertEquals(cycleDate, updatedAquarium.cycleDate)
        updatedAquarium.lastDate?.let { lastDate ->
            updatedAquarium.nextDate?.let { nextDate ->
                assertEquals(lastDate.plusDays(cycleDate.toLong()), nextDate)
            }
        }
    }

    @Test
    @DisplayName("다음 알림일 업데이트 - 다양한 관리주기")
    fun updateNextNotificationDate_DifferentCycleDates() {
        val cycleDates = intArrayOf(7, 14, 21, 30)

        for (cycleDate in cycleDates) {
            val aquarium = Aquarium(testMember, "어항$cycleDate")
            aquarium.changeSchedule(cycleDate, null, null)
            aquariumRepository.save(aquarium)
            
            notificationService.updateNextNotificationDate(aquarium)
            
            val updatedAquarium = aquariumRepository.findById(aquarium.id).orElseThrow()
            updatedAquarium.lastDate?.let { lastDate ->
                updatedAquarium.nextDate?.let { nextDate ->
                    val expectedNextDate = lastDate.plusDays(cycleDate.toLong())
                    assertEquals(expectedNextDate, nextDate)
                } ?: fail("nextDate should not be null")
            } ?: fail("lastDate should not be null")
        }
    }
}

