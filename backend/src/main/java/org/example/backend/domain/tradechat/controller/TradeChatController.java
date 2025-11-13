package org.example.backend.domain.tradechat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.tradechat.dto.TradeChatMessageDto;
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto;
import org.example.backend.domain.tradechat.service.TradeChatService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class TradeChatController implements TradeChatControllerSpec {

    private final TradeChatService tradeChatService;

    /*
    STOMP 메세지 전송
        - 클라이언트 /receive/{roomId} 경로로 수신
        - 서버는 /send/{roomId} 로 메세지를 캐스팅
     */
    @Override
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, TradeChatMessageDto request) {
        tradeChatService.sendMessage(roomId, request, request.senderId());
    }

    /*
     현재 거래게시글에 대한 채팅방 생성
        - 채팅방이 존재하지 않을 때만 채팅방 새로 생성
     */
    @Override
    @PostMapping("/{tradeId}/room")
    public ApiResponse<Long> createChatRoom(
            @PathVariable Long tradeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        Long roomId = tradeChatService.createChatRoom(tradeId, memberId);
        return ApiResponse.ok("채팅방이 생성되었습니다.", roomId);
    }

    // 해당 채팅방의 게시글 경로 확인
    @Override
    @GetMapping("/rooms/{roomId}")
    public ApiResponse<TradeChatRoomDto> getChatRoomDetail(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        TradeChatRoomDto chatRoom = tradeChatService.getChatRoomDetail(roomId, memberId);
        return ApiResponse.ok("채팅방 거래정보를 조회했습니다.", chatRoom);
    }


    // 특정 채팅방의 이전 채팅 내역 조회
    @Override
    @GetMapping("/rooms/messages/{roomId}")
    public ApiResponse<List<TradeChatMessageDto>> getMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        List<TradeChatMessageDto> messages = tradeChatService.getMessages(roomId, memberId);
        return ApiResponse.ok("채팅 내역을 조회했습니다.", messages);
    }

    /*
    내 채팅방 목록 조회
        - ONGOING 상태의 채팅방만 조회
     */
    @Override
    @GetMapping("/rooms/me")
    public ApiResponse<List<TradeChatRoomDto>> getMyChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        List<TradeChatRoomDto> chatRooms = tradeChatService.getMyChatRooms(memberId);
        return ApiResponse.ok("채팅방 목록을 조회했습니다.", chatRooms);
    }
}
