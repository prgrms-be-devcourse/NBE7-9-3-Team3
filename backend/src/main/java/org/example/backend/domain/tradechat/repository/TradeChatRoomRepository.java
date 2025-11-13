package org.example.backend.domain.tradechat.repository;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.tradechat.entity.ChatStatus;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeChatRoomRepository extends JpaRepository<TradeChatRoom, Long> {

    Optional<TradeChatRoom> findByTradeAndSellerIdAndBuyerId(Trade trade, Member seller, Member buyer);

    //닉네임, 거래 제목 등 한 번에 가져오는 최적화 쿼리
    @Query("""
        SELECT r
        FROM TradeChatRoom r
        JOIN FETCH r.sellerId s
        JOIN FETCH r.buyerId b
        JOIN FETCH r.trade t
        WHERE (r.status = :status AND s.memberId = :memberId)
           OR (r.status = :status AND b.memberId = :memberId)
    """)
    List<TradeChatRoom> findAllWithMemberAndTrade(
            @Param("status") ChatStatus status,
            @Param("memberId") Long memberId
    );
    
    //채팅방의 가장 최근 메시지 시간 조회
    @Query("""
        SELECT MAX(m.sendDate)
        FROM TradeChatMessage m
        WHERE m.chatRoom.Id = :roomId
    """)
    LocalDateTime findLatestMessageDate(@Param("roomId") Long roomId);
}
