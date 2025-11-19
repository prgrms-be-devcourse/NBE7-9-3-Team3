package org.example.backend.domain.notification.service

import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AquariumNotificationService(
    private val aquariumRepository: AquariumRepository,
    private val emailService: EmailService
) {
    companion object {
        private val log = LoggerFactory.getLogger(AquariumNotificationService::class.java)
    }

    /**
     * 매일 오전 9시에 실행되는 스케줄러
     * 관리주기가 도래한 어항들에 대해 이메일 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * ?")
    fun sendDailyAquariumReminders() {
        log.info("어항 관리 알림 발송 시작")

        try {
            val now = LocalDateTime.now()
            val aquariumsToNotify = aquariumRepository.findAquariumsForNotification(now)

            if (aquariumsToNotify.isEmpty()) {
                log.info("알림을 보낼 어항이 없습니다.")
                return
            }

            var successCount = 0
            var failureCount = 0

            for (aquarium in aquariumsToNotify) {
                try {
                    // 이메일 발송
                    emailService.sendAquariumReminderEmail(aquarium)

                    // 다음 알림일 업데이트
                    updateNextNotificationDate(aquarium)

                    successCount++
                    log.info(
                        "어항 알림 발송 성공: {} (ID: {}) -> {}",
                        aquarium.name, aquarium.id, aquarium.member.email
                    )
                } catch (e: Exception) {
                    failureCount++
                    log.error("어항 ID {} 알림 발송 실패: {}", aquarium.id, e.message)
                }
            }

            log.info(
                "어항 관리 알림 발송 완료: 성공 {}건, 실패 {}건, 전체 {}건",
                successCount, failureCount, aquariumsToNotify.size
            )
        } catch (e: Exception) {
            log.error("어항 관리 알림 발송 중 오류 발생: {}", e.message)
        }
    }

    /**
     * 특정 어항에 대해 테스트 알림 발송
     * @param aquariumId 어항 ID
     */
    @Transactional
    fun sendTestNotification(aquariumId: Long) {
        val aquarium = aquariumRepository.findById(aquariumId)
            .orElseThrow { IllegalArgumentException("어항을 찾을 수 없습니다. ID: $aquariumId") }

        require(aquarium.cycleDate > 0) { "알림이 비활성화된 어항입니다. 관리주기를 설정해주세요." }

        emailService.sendAquariumReminderEmail(aquarium)
        log.info("테스트 알림 발송 완료: {} (ID: {})", aquarium.name, aquariumId)
    }

    /**
     * 모든 알림 대상 어항에 대해 수동으로 알림 발송 (테스트용)
     */
    @Transactional
    fun sendAllNotifications() {
        log.info("수동 알림 발송 시작")
        sendDailyAquariumReminders()
    }

    /**
     * 어항의 다음 알림일을 업데이트
     * @param aquarium 어항 정보
     */
    @Transactional
    fun updateNextNotificationDate(aquarium: Aquarium) {
        val lastDate = LocalDateTime.now()
        val nextDate = lastDate.plusDays(aquarium.cycleDate.toLong())

        aquarium.changeSchedule(aquarium.cycleDate, lastDate, nextDate)
        aquariumRepository.save(aquarium)

        log.debug("다음 알림일 업데이트: {} -> {}", aquarium.name, nextDate)
    }
}
