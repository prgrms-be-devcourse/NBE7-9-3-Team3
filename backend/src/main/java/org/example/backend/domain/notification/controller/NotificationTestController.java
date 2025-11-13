package org.example.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.service.AquariumNotificationService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

// 실제 서비스에서 사용안되는 테스트를 위한 코드
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationTestController {
    
    private final AquariumNotificationService notificationService;
    
    
    @PostMapping("/test/{aquariumId}")
    public ApiResponse<Void> sendTestNotification(@PathVariable Long aquariumId) {
        notificationService.sendTestNotification(aquariumId);
        return new ApiResponse<>("200", "테스트 알림이 발송되었습니다.");
    }
    
    
    @PostMapping("/send-all")
    public ApiResponse<Void> sendAllNotifications() {
        notificationService.sendAllNotifications();
        return new ApiResponse<>("200", "모든 알림이 발송되었습니다.");
    }
}
