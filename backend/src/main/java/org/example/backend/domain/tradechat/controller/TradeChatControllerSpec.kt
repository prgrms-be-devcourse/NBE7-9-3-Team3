package org.example.backend.domain.tradechat.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.tradechat.dto.TradeChatMessageDto
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Trade Chat", description = "거래 채팅 관리 API")
interface TradeChatControllerSpec {

    @MessageMapping("/{roomId}")
    fun sendMessage(
        @DestinationVariable roomId: Long,
        request: TradeChatMessageDto
    )

    @Operation(summary = "채팅방 생성", description = "현재 거래게시글에 대한 채팅방을 생성합니다.")
    fun createChatRoom(
        @Parameter(description = "거래 게시글 ID", required = true)
        @PathVariable tradeId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Long>

    @Operation(summary = "채팅방 거래정보 조회", description = "채팅방의 거래정보를 조회합니다.")
    fun getChatRoomDetail(
        @Parameter(description = "채팅방 ID", required = true)
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<TradeChatRoomDto>

    @Operation(summary = "채팅 내역 조회", description = "특정 채팅방의 이전 채팅 내역을 조회합니다.")
    fun getMessages(
        @Parameter(description = "채팅방 ID", required = true)
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<TradeChatMessageDto>>

    @Operation(summary = "내 채팅방 목록 조회", description = "내가 참여한 채팅방 목록을 조회합니다.")
    fun getMyChatRooms(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<TradeChatRoomDto>>
}
