package org.example.backend.domain.tradechat.controller

import org.example.backend.domain.tradechat.dto.TradeChatMessageDto
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto
import org.example.backend.domain.tradechat.service.TradeChatService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.response.ApiResponse.Companion.ok
import org.example.backend.global.security.CustomUserDetails
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class TradeChatController(
    private val tradeChatService: TradeChatService
) : TradeChatControllerSpec {

    // STOMP 메시지 전송
    @MessageMapping("/{roomId}")
    override fun sendMessage(@DestinationVariable roomId: Long, request: TradeChatMessageDto) {
        val senderId = request.senderId ?: throw BusinessException(ErrorCode.TRADE_CHAT_SENDER_NOT_FOUND)
        tradeChatService.sendMessage(roomId, request, request.senderId)
    }

    // 현재 거래게시글에 대한 채팅방 생성
    @PostMapping("/{tradeId}/room")
    override fun createChatRoom(
        @PathVariable tradeId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Long> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val roomId = tradeChatService.createChatRoom(tradeId, memberId)
        return ok("채팅방이 생성되었습니다.", roomId)
    }

    // 해당 채팅방의 게시글 경로 확인
    @GetMapping("/rooms/{roomId}")
    override fun getChatRoomDetail(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<TradeChatRoomDto> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val chatRoom = tradeChatService.getChatRoomDetail(roomId, memberId)
        return ok("채팅방 거래정보를 조회했습니다.", chatRoom)
    }

    // 특정 채팅방의 이전 채팅 내역 조회
    @GetMapping("/rooms/messages/{roomId}")
    override fun getMessages(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<TradeChatMessageDto>> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val messages = tradeChatService.getMessages(roomId, memberId)
        return ok("채팅 내역을 조회했습니다.", messages)
    }

    // 내 채팅방 목록 조회
    @GetMapping("/rooms/me")
    override fun getMyChatRooms(@AuthenticationPrincipal userDetails: CustomUserDetails): ApiResponse<List<TradeChatRoomDto>> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val chatRooms = tradeChatService.getMyChatRooms(memberId)
        return ok("채팅방 목록을 조회했습니다.", chatRooms)
    }
}
