package org.example.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    /**
     * 어항 관리 알림 이메일 발송
     * @param aquarium 알림을 보낼 어항 정보
     */
    public void sendAquariumReminderEmail(Aquarium aquarium) {
        try {
            String toEmail = aquarium.getMember().getEmail();
            String subject = "🐠 어항 관리 알림 - " + aquarium.getName();
            String htmlContent = createEmailTemplate(aquarium);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("어항 알림 이메일 발송 성공: {} -> {}", aquarium.getName(), toEmail);
            
        } catch (Exception e) {
            log.error("어항 알림 이메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
    
    /**
     * 이메일 템플릿 생성
     * @param aquarium 어항 정보
     * @return HTML 형식의 이메일 내용
     */
    private String createEmailTemplate(Aquarium aquarium) {
        String template = loadEmailTemplate();
        
        return template
            .replace("{{memberName}}", aquarium.getMember().getNickname())
            .replace("{{aquariumName}}", aquarium.getName())
            .replace("{{cycleDate}}", String.valueOf(aquarium.getCycleDate()))
            .replace("{{nextDate}}", aquarium.getNextDate() != null ? 
                aquarium.getNextDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "미설정")
            .replace("{{aquariumId}}", String.valueOf(aquarium.getId()));
    }
    
    /**
     * 이메일 템플릿 파일 로드
     * @return HTML 템플릿 문자열
     */
    private String loadEmailTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/aquarium-reminder.html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("이메일 템플릿 로드 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 템플릿을 로드할 수 없습니다.", e);
        }
    }
}

