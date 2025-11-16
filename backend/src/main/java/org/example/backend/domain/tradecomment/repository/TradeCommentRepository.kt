package org.example.backend.domain.tradecomment.repository;

import java.util.List;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.tradecomment.entity.TradeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeCommentRepository extends JpaRepository<TradeComment, Long> {

    List<TradeComment> findByTradeTradeId(Long tradeId);
    
    List<TradeComment> findByMember(Member member);

}
