package org.example.backend.domain.tradechat.repository;

import org.example.backend.domain.tradechat.entity.TradeChatMessage;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeChatMessageRepository extends JpaRepository<TradeChatMessage, Long> {
    List<TradeChatMessage> findByChatRoomOrderBySendDateAsc(TradeChatRoom room);
}
