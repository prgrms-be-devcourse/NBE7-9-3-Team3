package org.example.backend.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

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
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("어항 알림 서비스 테스트")
class AquariumNotificationServiceTest {

    @Autowired
    private AquariumNotificationService notificationService;

    @Autowired
    private AquariumRepository aquariumRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenService authTokenService;

    @MockitoBean
    private EmailService emailService;

    private Member testMember;
    private Aquarium testAquarium;

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember = loginUtil.createMember("notification@test.com", "test1234", "notification", "");

        testAquarium = new Aquarium(testMember, "테스트어항");
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(7));
        aquariumRepository.save(testAquarium);
    }

    @Test
    @DisplayName("일일 알림 발송 - 알림 대상 어항이 없는 경우")
    void sendDailyAquariumReminders_NoAquariums() {
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(7));
        aquariumRepository.save(testAquarium);
        doNothing().when(emailService).sendAquariumReminderEmail(any(Aquarium.class));

        notificationService.sendDailyAquariumReminders();

        verify(emailService, never()).sendAquariumReminderEmail(any(Aquarium.class));
    }

    @Test
    @DisplayName("일일 알림 발송 - 알림 대상 어항이 있는 경우")
    void sendDailyAquariumReminders_WithAquariums() {
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1));
        aquariumRepository.save(testAquarium);
        doNothing().when(emailService).sendAquariumReminderEmail(any(Aquarium.class));

        notificationService.sendDailyAquariumReminders();

        verify(emailService, times(1)).sendAquariumReminderEmail(any(Aquarium.class));
        Aquarium updatedAquarium = aquariumRepository.findById(testAquarium.getId()).orElseThrow();
        assertNotNull(updatedAquarium.getNextDate());
        assertTrue(updatedAquarium.getNextDate().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("일일 알림 발송 - 여러 어항에 대한 알림 발송")
    void sendDailyAquariumReminders_MultipleAquariums() {
        Aquarium aquarium1 = new Aquarium(testMember, "어항1");
        aquarium1.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1));
        aquariumRepository.save(aquarium1);
        
        Aquarium aquarium2 = new Aquarium(testMember, "어항2");
        aquarium2.changeSchedule(14, LocalDateTime.now().minusDays(14), LocalDateTime.now().minusDays(1));
        aquariumRepository.save(aquarium2);

        doNothing().when(emailService).sendAquariumReminderEmail(any(Aquarium.class));

        notificationService.sendDailyAquariumReminders();

        verify(emailService, times(2)).sendAquariumReminderEmail(any(Aquarium.class));
        Aquarium updatedAquarium1 = aquariumRepository.findById(aquarium1.getId()).orElseThrow();
        Aquarium updatedAquarium2 = aquariumRepository.findById(aquarium2.getId()).orElseThrow();
        assertNotNull(updatedAquarium1.getNextDate());
        assertNotNull(updatedAquarium2.getNextDate());
    }

    @Test
    @DisplayName("일일 알림 발송 - 이메일 발송 실패 시에도 다음 어항 처리 계속")
    void sendDailyAquariumReminders_EmailFailure_ContinueProcessing() {
        Aquarium aquarium1 = new Aquarium(testMember, "어항1");
        aquarium1.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1));
        aquariumRepository.save(aquarium1);
        
        Aquarium aquarium2 = new Aquarium(testMember, "어항2");
        aquarium2.changeSchedule(14, LocalDateTime.now().minusDays(14), LocalDateTime.now().minusDays(1));
        aquariumRepository.save(aquarium2);
        
        doThrow(new RuntimeException("이메일 발송 실패"))
                .doNothing()
                .when(emailService).sendAquariumReminderEmail(any(Aquarium.class));

        notificationService.sendDailyAquariumReminders();

        verify(emailService, times(2)).sendAquariumReminderEmail(any(Aquarium.class));
        Aquarium updatedAquarium2 = aquariumRepository.findById(aquarium2.getId()).orElseThrow();
        assertNotNull(updatedAquarium2.getNextDate());
    }

    @Test
    @DisplayName("테스트 알림 발송 - 성공")
    void sendTestNotification_Success() {
        Long aquariumId = testAquarium.getId();
        doNothing().when(emailService).sendAquariumReminderEmail(any(Aquarium.class));

        notificationService.sendTestNotification(aquariumId);

        verify(emailService, times(1)).sendAquariumReminderEmail(any(Aquarium.class));
    }

    @Test
    @DisplayName("테스트 알림 발송 - 어항을 찾을 수 없는 경우")
    void sendTestNotification_AquariumNotFound() {
        Long aquariumId = 999L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendTestNotification(aquariumId);
        });

        assertEquals("어항을 찾을 수 없습니다. ID: " + aquariumId, exception.getMessage());
        verify(emailService, never()).sendAquariumReminderEmail(any(Aquarium.class));
    }

    @Test
    @DisplayName("테스트 알림 발송 - 알림이 비활성화된 어항")
    void sendTestNotification_NotificationDisabled() {
        Aquarium disabledAquarium = new Aquarium(testMember, "비활성화어항");
        disabledAquarium.changeSchedule(0, null, null);
        aquariumRepository.save(disabledAquarium);
        Long aquariumId = disabledAquarium.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendTestNotification(aquariumId);
        });

        assertEquals("알림이 비활성화된 어항입니다. 관리주기를 설정해주세요.", exception.getMessage());
        verify(emailService, never()).sendAquariumReminderEmail(any(Aquarium.class));
    }

    @Test
    @DisplayName("수동 알림 발송 - sendAllNotifications 호출")
    void sendAllNotifications_Success() {
        testAquarium.changeSchedule(7, LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1));
        aquariumRepository.save(testAquarium);
        doNothing().when(emailService).sendAquariumReminderEmail(any(Aquarium.class));

        notificationService.sendAllNotifications();

        verify(emailService, times(1)).sendAquariumReminderEmail(any(Aquarium.class));
    }

    @Test
    @DisplayName("다음 알림일 업데이트 - 성공")
    void updateNextNotificationDate_Success() {
        int cycleDate = 7;
        testAquarium.changeSchedule(cycleDate, null, null);
        aquariumRepository.save(testAquarium);

        notificationService.updateNextNotificationDate(testAquarium);

        Aquarium updatedAquarium = aquariumRepository.findById(testAquarium.getId()).orElseThrow();
        assertNotNull(updatedAquarium.getLastDate());
        assertNotNull(updatedAquarium.getNextDate());
        assertEquals(cycleDate, updatedAquarium.getCycleDate());
        assertEquals(updatedAquarium.getLastDate().plusDays(cycleDate), updatedAquarium.getNextDate());
    }

    @Test
    @DisplayName("다음 알림일 업데이트 - 다양한 관리주기")
    void updateNextNotificationDate_DifferentCycleDates() {
        int[] cycleDates = {7, 14, 21, 30};

        for (int cycleDate : cycleDates) {
            Aquarium aquarium = new Aquarium(testMember, "어항" + cycleDate);
            aquarium.changeSchedule(cycleDate, null, null);
            aquariumRepository.save(aquarium);
            
            notificationService.updateNextNotificationDate(aquarium);
            
            Aquarium updatedAquarium = aquariumRepository.findById(aquarium.getId()).orElseThrow();
            LocalDateTime expectedNextDate = updatedAquarium.getLastDate().plusDays(cycleDate);
            assertEquals(expectedNextDate, updatedAquarium.getNextDate());
        }
    }
}

