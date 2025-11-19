package org.example.backend.domain.point.service

import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.point.dto.PointHistoryResponseDto
import org.example.backend.domain.point.dto.PurchaseRequestDto
import org.example.backend.domain.point.entity.Point
import org.example.backend.domain.point.repository.PointRepository
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PointService(
    private val pointRepository: PointRepository,
    private val memberRepository: MemberRepository,
    private val tradeRepository: TradeRepository
) {

    // 포인트 충전
    fun chargePoint(memberId: Long, amount: Long) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.POINT_MEMBER_NOT_FOUND) }

        val newPoints = member.points + amount
        member.updatePoints(newPoints)

        val point = Point.create(member, amount, newPoints)
        pointRepository.save(point)
    }

    /*
    포인트 전체 내역 조회
    - 현재 최신순으로 전체 내역 조회 기능만
    - 내역 타입 별 조회 기능 도입 예정
     */
    fun getPointHistory(memberId: Long): List<PointHistoryResponseDto> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.POINT_MEMBER_NOT_FOUND) }

        val pointHistory = pointRepository.findAllByMemberOrderByCreateDateDesc(member)
        if (pointHistory.isEmpty()) {
            throw BusinessException(ErrorCode.POINT_HISTORY_NOT_FOUND)
        }

        return pointHistory.map { PointHistoryResponseDto.from(it) }
    }

    // 결제 (동시 구매 방지 적용)
    @Transactional
    fun purchaseItem(buyerId: Long, request: PurchaseRequestDto) {
        val buyer = memberRepository.findById(buyerId)
            .orElseThrow { BusinessException(ErrorCode.POINT_BUYER_NOT_FOUND) }
        val seller = memberRepository.findById(request.sellerId)
            .orElseThrow { BusinessException(ErrorCode.POINT_SELLER_NOT_FOUND) }

        val trade = request.tradeId?.let { tradeRepository.findByIdForUpdate(it) }
            ?.orElseThrow { BusinessException(ErrorCode.TRADE_NOT_FOUND) }
            ?: throw BusinessException(ErrorCode.TRADE_NOT_FOUND)

        // 이미 판매 완료된 상품인지 확인
        if (trade.isSoldOut) {
            throw BusinessException(ErrorCode.TRADE_ALREADY_SOLD)
        }

        // 포인트 보유량 확인
        if (buyer.points < request.amount!!) {
            throw BusinessException(ErrorCode.POINT_INSUFFICIENT)
        }

        val buyerNewPoints = buyer.points - request.amount
        val sellerNewPoints = seller.points + request.amount

        buyer.updatePoints(buyerNewPoints)
        seller.updatePoints(sellerNewPoints)

        pointRepository.save(Point.createPurchase(buyer, request.amount, buyerNewPoints))
        pointRepository.save(Point.createSale(seller, request.amount, sellerNewPoints))

        // 거래 상태를 판매 완료로 변경 (엔티티 내부에서 중복 방지 체크)
        trade.completeTransaction()

        memberRepository.save(buyer)
        memberRepository.save(seller)
        tradeRepository.save(trade)
    }
}
