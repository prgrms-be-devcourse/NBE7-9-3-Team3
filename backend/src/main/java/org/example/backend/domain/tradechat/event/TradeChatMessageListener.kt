package org.example.backend.domain.tradechat.event

import lombok.RequiredArgsConstructor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
@RequiredArgsConstructor
@EnableAsync
class TradeChatMessageListener(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * == DB에 저장된(커밋된) 메시지만 비동기적으로 전송 ==
     *
     * - 이전 방식: 메시지 저장과 전송이 같은 트랜잭션내 처리
     *   - 서버 직렬화로 인해 전송이 지연되는 병목 발생
     *   - 예. 메시지0 저장(200ms) -> 메시지0 전송(200ms) -> 메시지1 저장(200ms) -> 메시지1 전송(200ms) -> ..
     *
     * - 개선 방식:
     *   - DB 저장이 완료된 메시지는 이벤트를 발행하여 별도 스레드에서 비동기 전송 처리
     *   - 병렬적으로 처리가능 -> 응답 지연 개선
     *   - 예. 메시지0 저장(200ms) -> 메시지1 저장(200ms) -> ..
     *          ↘ 메시지0 전송         ↘ 메시지1 전송
     *
     * - 안정성
     *   - WebSocket 전송 실패 가능성 대비, 최대 3회까지 재전송 시도
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleTradeChatMessageEvent(event: TradeChatMessageEvent) {
        val maxRetry = 3 // 최대 재시도 횟수
        var attempt = 0
        var sent = false

        while (!sent && attempt < maxRetry) {
            try {
                messagingTemplate.convertAndSend("/receive/" + event.roomId, event.messageDto)
                sent = true
            } catch (e: Exception) {
                attempt++
                // 로그 기록
                System.err.println("메시지 전송 실패, 재시도 " + attempt + "회: " + e.message)

                try {
                    Thread.sleep(100L * attempt) // 재시도 간 짧은 딜레이
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }

        if (!sent) {
            // 최종 실패 시 알람, DB 기록 등 추가 처리 가능
            System.err.println("메시지 전송 최종 실패: roomId=" + event.roomId)
        }
    }
}

