package org.example.backend.global;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class PointUtil {

    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public PointUtil(TradeRepository tradeRepository,
                     MemberRepository memberRepository,
                     PasswordEncoder passwordEncoder) {
        this.tradeRepository = tradeRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 판매자 생성
    public Member createSeller() {
        Member seller = new Member("seller@test.com", passwordEncoder.encode("seller1234"),"seller", "");

        return memberRepository.save(seller);
    }

    // 거래 게시글 생성
    public Trade createTrade(org.example.backend.domain.member.entity.Member seller, Long price) {
        Trade trade = new Trade(
                seller,
                BoardType.FISH,
                "테스트 제목",
                "테스트 설명",
                price,
                TradeStatus.SELLING,
                null,
                LocalDateTime.now()
        );
        return tradeRepository.save(trade);
    }

    // 구매 요청 JSON 생성
    public String purchaseRequest(Long sellerId, Long amount, Long tradeId) {
        return """
        {
            "sellerId": %d,
            "amount": %d,
            "tradeId": %d
        }
        """.formatted(sellerId, amount, tradeId);
    }
}
