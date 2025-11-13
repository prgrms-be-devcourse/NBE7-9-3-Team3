package org.example.backend.domain.trade.repository;

import jakarta.persistence.LockModeType;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByBoardType(BoardType boardType, Pageable pageable);

    @Query("Select t FROM Trade t WHERE t.member.memberId = :memberId AND t.boardType = :boardType")
    Page<Trade> findMyTrades(@Param("memberId") Long memberId,
        @Param("boardType") BoardType boardType,
        Pageable pageable);

    @Query("SELECT t FROM Trade t WHERE t.boardType = :boardType " +
        "AND (:keyword IS NULL OR t.title LIKE %:keyword% OR t.description LIKE %:keyword% OR t.category LIKE %:keyword%) " +
        "AND (:minPrice IS NULL OR t.price >= :minPrice)" +
        "AND (:maxPrice IS NULL OR t.price <= :maxPrice)" +
        "AND (:status IS NULL OR t.status = :status)")
    Page<Trade> searchTrades(
        @Param("boardType") BoardType boardType,
        @Param("keyword") String keyword,
        @Param("minPrice") Long minPrice,
        @Param("maxPrice") Long maxPrice,
        @Param("status") TradeStatus status,
        Pageable pageable
    );

    // 동시 접근을 직렬화
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trade t WHERE t.id = :id")
    Optional<Trade> findByIdForUpdate(@Param("id") Long id);
}
