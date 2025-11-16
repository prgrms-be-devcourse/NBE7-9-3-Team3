package org.example.backend.domain.trade.dto

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import java.time.LocalDateTime

data class TradeCreateRequestDto(
    val memberId: Long,
    val boardType: BoardType,
    val title: String,
    val description: String,
    val price: Long,
    val category: String?,
    val imageUrls: List<String>
) {
    fun toEntity(member: Member): Trade {
        return Trade(
            member = member,
            boardType = this.boardType,
            title = this.title,
            description = this.description,
            price = this.price,
            status = TradeStatus.SELLING,
            category = this.category,
            createDate = LocalDateTime.now()
        )
    }

    companion object {
        fun from(dto: TradeRequestDto, type: BoardType, memberId: Long): TradeCreateRequestDto {
            return TradeCreateRequestDto(
                memberId = memberId,
                boardType = type,
                title = dto.title,
                description = dto.description,
                price = dto.price,
                category = dto.category,
                imageUrls = dto.imageUrls
            )
        }
    }
}