package org.example.backend.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.LoginUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("이메일 서비스 테스트")
class EmailServiceTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AquariumRepository aquariumRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenService authTokenService;

    private Member testMember;
    private Aquarium testAquarium;

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember = loginUtil.createMember("email@test.com", "test1234", "email", "");

        testAquarium = new Aquarium(testMember, "테스트어항");
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(7));
        aquariumRepository.save(testAquarium);
    }

    @Test
    @DisplayName("어항 알림 이메일 발송 - 성공")
    void sendAquariumReminderEmail_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendAquariumReminderEmail(testAquarium);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("어항 알림 이메일 발송 - 이메일 발송 실패")
    void sendAquariumReminderEmail_Failure() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP 서버 연결 실패"))
                .when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendAquariumReminderEmail(testAquarium);
        });

        assertEquals("이메일 발송에 실패했습니다.", exception.getMessage());
        assertNotNull(exception.getCause());
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("어항 알림 이메일 발송 - nextDate가 null인 경우")
    void sendAquariumReminderEmail_NextDateNull() throws Exception {
        Aquarium aquariumWithNullNextDate = new Aquarium(testMember, "미설정어항");
        aquariumWithNullNextDate.changeSchedule(7, LocalDateTime.now().minusDays(7), null);
        aquariumRepository.save(aquariumWithNullNextDate);
        
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendAquariumReminderEmail(aquariumWithNullNextDate);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}

