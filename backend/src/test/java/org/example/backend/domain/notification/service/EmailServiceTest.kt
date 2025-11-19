package org.example.backend.domain.notification.service

import jakarta.mail.internet.MimeMessage
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
import org.springframework.mail.javamail.JavaMailSender
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import java.time.LocalDateTime

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("이메일 서비스 테스트")
class EmailServiceTest {

    @MockitoBean
    private lateinit var mailSender: JavaMailSender

    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var aquariumRepository: AquariumRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var testMember: Member
    private lateinit var testAquarium: Aquarium

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        testMember = loginUtil.createMember("email@test.com", "test1234", "email", "")

        testAquarium = Aquarium(testMember, "테스트어항")
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(7))
        aquariumRepository.save(testAquarium)
    }

    @Test
    @DisplayName("어항 알림 이메일 발송 - 성공")
    fun sendAquariumReminderEmail_Success() {
        val mimeMessage = mock(MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        doNothing().`when`(mailSender).send(any(MimeMessage::class.java))

        emailService.sendAquariumReminderEmail(testAquarium)

        verify(mailSender, times(1)).createMimeMessage()
        verify(mailSender, times(1)).send(any(MimeMessage::class.java))
    }

    @Test
    @DisplayName("어항 알림 이메일 발송 - 이메일 발송 실패")
    fun sendAquariumReminderEmail_Failure() {
        val mimeMessage = mock(MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        doThrow(RuntimeException("SMTP 서버 연결 실패"))
            .`when`(mailSender).send(any(MimeMessage::class.java))

        val exception = assertThrows(RuntimeException::class.java) {
            emailService.sendAquariumReminderEmail(testAquarium)
        }

        assertEquals("이메일 발송에 실패했습니다.", exception.message)
        assertNotNull(exception.cause)
        verify(mailSender, times(1)).createMimeMessage()
        verify(mailSender, times(1)).send(any(MimeMessage::class.java))
    }

    @Test
    @DisplayName("어항 알림 이메일 발송 - nextDate가 null인 경우")
    fun sendAquariumReminderEmail_NextDateNull() {
        val aquariumWithNullNextDate = Aquarium(testMember, "미설정어항")
        aquariumWithNullNextDate.changeSchedule(7, LocalDateTime.now().minusDays(7), null)
        aquariumRepository.save(aquariumWithNullNextDate)
        
        val mimeMessage = mock(MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        doNothing().`when`(mailSender).send(any(MimeMessage::class.java))

        emailService.sendAquariumReminderEmail(aquariumWithNullNextDate)

        verify(mailSender, times(1)).createMimeMessage()
        verify(mailSender, times(1)).send(any(MimeMessage::class.java))
    }
}

