package org.example.backend.domain.notification.service

import org.example.backend.domain.aquarium.entity.Aquarium
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    companion object {
        private val log = LoggerFactory.getLogger(EmailService::class.java)
    }

    /**
     * 어항 관리 알림 이메일 발송
     * @param aquarium 알림을 보낼 어항 정보
     */
    fun sendAquariumReminderEmail(aquarium: Aquarium) {
        try {
            val toEmail = aquarium.member.email
            val subject = "🐠 어항 관리 알림 - ${aquarium.name}"
            val htmlContent = createEmailTemplate(aquarium)

            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(toEmail)
            helper.setSubject(subject)
            helper.setText(htmlContent, true)

            mailSender.send(message)

            log.info("어항 알림 이메일 발송 성공: {} -> {}", aquarium.name, toEmail)
        } catch (e: Exception) {
            log.error("어항 알림 이메일 발송 실패: {}", e.message)
            throw RuntimeException("이메일 발송에 실패했습니다.", e)
        }
    }

    /**
     * 이메일 템플릿 생성
     * @param aquarium 어항 정보
     * @return HTML 형식의 이메일 내용
     */
    private fun createEmailTemplate(aquarium: Aquarium): String {
        val template = loadEmailTemplate()

        return template
            .replace("{{memberName}}", aquarium.member.nickname)
            .replace("{{aquariumName}}", aquarium.name)
            .replace("{{cycleDate}}", aquarium.cycleDate.toString())
            .replace(
                "{{nextDate}}",
                aquarium.nextDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "미설정"
            )
            .replace("{{aquariumId}}", aquarium.id.toString())
    }

    /**
     * 이메일 템플릿 파일 로드
     * @return HTML 템플릿 문자열
     */
    private fun loadEmailTemplate(): String {
        try {
            val resource = ClassPathResource("templates/email/aquarium-reminder.html")
            return String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            log.error("이메일 템플릿 로드 실패: {}", e.message)
            throw RuntimeException("이메일 템플릿을 로드할 수 없습니다.", e)
        }
    }
}

