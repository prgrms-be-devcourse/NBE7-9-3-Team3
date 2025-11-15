package org.example.backend.domain.trade.service

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.trade.dto.*
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.image.ImageService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Member가 Java+Lombok일 때 임시로 사용하는 확장 함수
// Member가 Kotlin으로 마이그레이션되면 삭제 예정
private fun Member.getMemberIdValue(): Long {
    return try {
        val method = this::class.java.getMethod("getMemberId")
        method.invoke(this) as Long
    } catch (e: Exception) {
        throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
    }
}

@Service
@Transactional(readOnly = true)
class TradeService(
    private val tradeRepository: TradeRepository,
    private val memberRepository: MemberRepository,
    private val imageService: ImageService
) {
    @Transactional
    fun createTrade(request: TradeCreateRequestDto): TradeResponseDto {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val trade = request.toEntity(member)

        request.imageUrls.forEach { imageUrl ->
            trade.addImage(imageUrl)
        }

        return TradeResponseDto.from(tradeRepository.save(trade))
    }

    fun getAllTrade(
        boardType: BoardType,
        searchRequest: TradeSearchRequestDto
    ): PageResponseDto<TradeResponseDto> {

        val sort = when (searchRequest.getSort()) {
            "price-asc" -> Sort.by(Sort.Direction.ASC, "price")
            "price-desc" -> Sort.by(Sort.Direction.DESC, "price")
            else -> Sort.by(Sort.Direction.DESC, "createDate")
        }

        val pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort)

        val tradePage = if (searchRequest.hasSearchTerm() ||
            searchRequest.hasPriceFilter() ||
            searchRequest.hasStatusFilter()) {
            tradeRepository.searchTrades(
                boardType,
                searchRequest.searchTerm,
                searchRequest.minPrice,
                searchRequest.maxPrice,
                searchRequest.status,
                pageable
            )
        } else {
            tradeRepository.findByBoardType(boardType, pageable)
        }

        return PageResponseDto.from(tradePage.map { TradeResponseDto.from(it) })
    }

    fun getTrade(boardType: BoardType, id: Long): TradeResponseDto {
        val trade = findTradeById(id)
        validateBoardType(trade, boardType)

        return TradeResponseDto.from(trade)
    }

    @Transactional
    fun updateTrade(updateRequest: TradeUpdateRequestDto): TradeResponseDto {
        val trade = findTradeById(updateRequest.tradeId)
        validateBoardType(trade, updateRequest.boardType)
        validateTradeOwner(trade, updateRequest.memberId)

        val request = updateRequest.tradeData
        trade.update(
            title = request.title,
            description = request.description,
            price = request.price,
            status = request.status,
            category = request.category
        )

        updateTradeImages(trade, updateRequest.imageUrls)

        return TradeResponseDto.from(trade)
    }

    @Transactional
    fun deleteTrade(boardType: BoardType, tradeId: Long, memberId: Long) {
        val trade = findTradeById(tradeId)
        validateBoardType(trade, boardType)
        validateTradeOwner(trade, memberId)

        // S3 이미지 삭제
        val imageUrls =  trade.imageUrls
        if (imageUrls.isNotEmpty()) {
            imageService.deleteFiles(imageUrls)
        }

        // Trade 엔티티 삭제 (cascade로 TradeImage도 함께 삭제됨)
        tradeRepository.deleteById(tradeId)
    }

    fun getMyTrades(
        memberId: Long,
        boardType: BoardType,
        page: Int,
        size: Int
    ): PageResponseDto<TradeResponseDto> {
        val sort = Sort.by(Sort.Direction.DESC, "createDate")

        val pageable = PageRequest.of(page, size, sort)

        val tradePage = tradeRepository.findMyTrades(memberId, boardType, pageable)

        return PageResponseDto.from(tradePage.map { TradeResponseDto.from(it) })
    }

    private fun validateBoardType(trade: Trade, boardType: BoardType) {
        if (trade.boardType != boardType) {
            throw BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID)
        }
    }

    private fun validateTradeOwner(trade: Trade, memberId: Long) {
//        if (trade.member.memberId != memberId) {
        if (trade.member.getMemberIdValue() != memberId) {    // TODO: Member 엔티티 코틀린 변환 이후 변경하기
            throw BusinessException(ErrorCode.TRADE_OWNER_MISMATCH)
        }
    }

    fun findTradeById(tradeId: Long): Trade {
        return tradeRepository.findById(tradeId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_NOT_FOUND) }
    }

    private fun updateTradeImages(trade: Trade, newImageUrls: List<String>) {
        val oldImageUrls = trade.imageUrls

        // 삭제할 이미지: 기존에는 있었는데 새 목록에는 없는 것
        val toDelete = oldImageUrls.filter { it !in newImageUrls }

        // S3에서 삭제
        if (toDelete.isNotEmpty()) {
            imageService.deleteFiles(toDelete)
        }

        // DB 이미지 목록 갱신
        trade.clearImages()
        newImageUrls.forEach { trade.addImage(it) }
    }
}
