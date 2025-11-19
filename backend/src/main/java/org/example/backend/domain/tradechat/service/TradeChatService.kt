package org.example.backend.domain.tradechat.service

import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.domain.tradechat.dto.TradeChatMessageDto
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto
import org.example.backend.domain.tradechat.entity.ChatStatus
import org.example.backend.domain.tradechat.entity.TradeChatMessage
import org.example.backend.domain.tradechat.entity.TradeChatRoom
import org.example.backend.domain.tradechat.event.TradeChatMessageEvent
import org.example.backend.domain.tradechat.repository.TradeChatMessageRepository
import org.example.backend.domain.tradechat.repository.TradeChatRoomRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class TradeChatService(
    private val chatRoomRepository: TradeChatRoomRepository,
    private val chatMessageRepository: TradeChatMessageRepository,
    private val tradeRepository: TradeRepository,
    private val memberRepository: MemberRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val eventPublisher: ApplicationEventPublisher
) {

    // 메시지 전송
    @Transactional
    fun sendMessage(roomId: Long, request: TradeChatMessageDto, memberId: Long) {
        val room = chatRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_CHAT_ROOM_NOT_FOUND) }
        val sender = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_CHAT_SENDER_NOT_FOUND) }

        val message = TradeChatMessage.create(room, sender, request.content)
        val savedMessage = chatMessageRepository.save(message)
        val messageDto = TradeChatMessageDto.from(savedMessage)

        // 트랜잭션 커밋 후 이벤트 발행
        eventPublisher.publishEvent(TradeChatMessageEvent(roomId, messageDto))
    }

    // 로그인 사용자의 채팅방 목록 조회
    fun getMyChatRooms(memberId: Long): List<TradeChatRoomDto> {
        val rooms = chatRoomRepository.findAllWithMemberAndTrade(ChatStatus.ONGOING, memberId)

        return rooms.sortedByDescending { r ->
            chatRoomRepository.findLatestMessageDate(r.id) ?: r.createDate
        }.map { TradeChatRoomDto.from(it) }
    }

    // 채팅방 생성
    fun createChatRoom(tradeId: Long, memberId: Long): Long {
        val trade = tradeRepository.findById(tradeId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_CHAT_TRADE_NOT_FOUND) }
        val seller = trade.member
        val buyer = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_CHAT_BUYER_NOT_FOUND) }

        return chatRoomRepository.findByTradeAndSellerIdAndBuyerId(trade, seller, buyer)
            .map { it.id }
            .orElseGet {
                val newRoom = TradeChatRoom.create(trade, seller, buyer)
                chatRoomRepository.save(newRoom)
                newRoom.id
            }
    }

    // 채팅방 거래정보 조회
    fun getChatRoomDetail(roomId: Long, memberId: Long): TradeChatRoomDto {
        val room = chatRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_CHAT_ROOM_NOT_FOUND) }

        val sellerId = room.sellerId.memberId
        val buyerId = room.buyerId.memberId

        if (memberId != sellerId && memberId != buyerId) {
            throw BusinessException(ErrorCode.FORBIDDEN_ACCESS)
        }

        return TradeChatRoomDto.from(room)
    }

    // 이전 채팅내역 조회
    fun getMessages(roomId: Long, memberId: Long): List<TradeChatMessageDto> {
        val room = chatRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_CHAT_ROOM_NOT_FOUND) }

        val sellerId = room.sellerId.memberId
        val buyerId = room.buyerId.memberId

        if (memberId != sellerId && memberId != buyerId) {
            throw BusinessException(ErrorCode.FORBIDDEN_ACCESS)
        }

        val messages = chatMessageRepository.findByChatRoomOrderBySendDateAsc(room)
        return messages.map { TradeChatMessageDto.from(it) }
    }
}
