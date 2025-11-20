package org.example.backend.global

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import org.example.backend.domain.trade.repository.TradeRepository
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class PointUtil(
    private val tradeRepository: TradeRepository,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {
    // 판매자 생성
    fun createSeller(): Member {
        val seller = Member("seller@test.com", passwordEncoder.encode("seller1234"), "seller", "")

        return memberRepository.save(seller)
    }

    // 거래 게시글 생성
    fun createTrade(seller: Member, price: Long): Trade {
        val trade = Trade(
            seller,
            BoardType.FISH,
            "테스트 제목",
            "테스트 설명",
            price,
            TradeStatus.SELLING,
            null,
            LocalDateTime.now()
        )
        return tradeRepository.save(trade)
    }

    // 구매 요청 JSON 생성
    fun purchaseRequest(sellerId: Long, amount: Long, tradeId: Long): String {
        return """
        {
            "sellerId": $sellerId,
            "amount": $amount,
            "tradeId": $tradeId
        }
        
        """.trimIndent()
    }
}
