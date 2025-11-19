package org.example.backend.domain.notification.controller

import org.example.backend.domain.notification.service.AquariumNotificationService
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 실제 서비스에서 사용안되는 테스트를 위한 코드
@RestController
@RequestMapping("/api/notification")
class NotificationTestController(
    private val notificationService: AquariumNotificationService
) {

    @PostMapping("/test/{aquariumId}")
    fun sendTestNotification(@PathVariable aquariumId: Long): ApiResponse<Void> {
        notificationService.sendTestNotification(aquariumId)
        return ApiResponse.ok("테스트 알림이 발송되었습니다.")
    }

    @PostMapping("/send-all")
    fun sendAllNotifications(): ApiResponse<Void> {
        notificationService.sendAllNotifications()
        return ApiResponse.ok("모든 알림이 발송되었습니다.")
    }
}
