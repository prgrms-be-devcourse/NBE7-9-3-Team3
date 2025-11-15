package org.example.backend.domain.trade.repository

import jakarta.persistence.LockModeType
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface TradeRepository : JpaRepository<Trade, Long> {
    fun findByBoardType(boardType: BoardType, pageable: Pageable): Page<Trade>

    @Query("Select t FROM Trade t WHERE t.member.memberId = :memberId AND t.boardType = :boardType")
    fun findMyTrades(
        @Param("memberId") memberId: Long,
        @Param("boardType") boardType: BoardType,
        pageable: Pageable
    ): Page<Trade>

    @Query("""
          SELECT t FROM Trade t 
          WHERE t.boardType = :boardType 
          AND (:keyword IS NULL OR t.title LIKE %:keyword% OR t.description LIKE %:keyword% OR t.category LIKE %:keyword%)
          AND (:minPrice IS NULL OR t.price >= :minPrice)
          AND (:maxPrice IS NULL OR t.price <= :maxPrice)
          AND (:status IS NULL OR t.status = :status)
      """)
    fun searchTrades(
        @Param("boardType") boardType: BoardType?,
        @Param("keyword") keyword: String?,
        @Param("minPrice") minPrice: Long?,
        @Param("maxPrice") maxPrice: Long?,
        @Param("status") status: TradeStatus?,
        pageable: Pageable
    ): Page<Trade>

    // 동시 접근을 직렬화
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trade t WHERE t.tradeId = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Optional<Trade>
}
