package org.example.backend.domain.trade.controller

import jakarta.validation.Valid
import org.example.backend.domain.trade.dto.PageResponseDto
import org.example.backend.domain.trade.dto.TradeCreateRequestDto
import org.example.backend.domain.trade.dto.TradeRequestDto
import org.example.backend.domain.trade.dto.TradeResponseDto
import org.example.backend.domain.trade.dto.TradeSearchRequestDto
import org.example.backend.domain.trade.dto.TradeUpdateRequestDto
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.service.TradeService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/market/{boardType}")
class TradeController(
    private val tradeService: TradeService
) : TradeControllerSpec {

    @PostMapping
    override fun createTrade(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable boardType: String,
        @RequestBody @Valid request: TradeRequestDto
    ): ApiResponse<TradeResponseDto> {
        val memberId = userDetails.getId()
        val type = BoardType.from(boardType)
        val serviceRequest = TradeCreateRequestDto.from(request, type, memberId)
        val trade = tradeService.createTrade(serviceRequest)
        return ApiResponse.ok("거래 게시글 등록 성공", trade)
    }

    @GetMapping
    override fun getAllTrade(
        @PathVariable boardType: String,
        @ModelAttribute @Valid searchRequest: TradeSearchRequestDto
    ): ApiResponse<PageResponseDto<TradeResponseDto>> {
        val type = BoardType.from(boardType)
        val trades = tradeService.getAllTrade(type, searchRequest)
        return ApiResponse.ok("거래 게시글 페이징 조회 성공", trades)
    }

    @GetMapping("/{tradeId}")
    override fun getTrade(
        @PathVariable boardType: String,
        @PathVariable tradeId: Long
    ): ApiResponse<TradeResponseDto> {
        val type = BoardType.from(boardType)
        val trade = tradeService.getTrade(type, tradeId)
        return ApiResponse.ok("거래 게시글 조회 성공", trade)
    }

    @PutMapping("/{tradeId}")
    override fun updateTrade(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable boardType: String,
        @PathVariable tradeId: Long,
        @RequestBody @Valid request: TradeRequestDto
    ): ApiResponse<TradeResponseDto> {
        val memberId = userDetails.getId()
        val type = BoardType.from(boardType)
        val updateRequest = TradeUpdateRequestDto.of(
            type, tradeId, memberId,
            request, request.imageUrls
        )
        val trade = tradeService.updateTrade(updateRequest)
        return ApiResponse.ok("거래 게시글 수정 성공", trade)
    }

    @DeleteMapping("/{tradeId}")
    override fun deleteTrade(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable boardType: String,
        @PathVariable tradeId: Long
    ): ApiResponse<Void> {
        val memberId = userDetails.getId()
        val type = BoardType.from(boardType)
        tradeService.deleteTrade(type, tradeId, memberId)
        return ApiResponse.ok("거래 게시글 삭제 성공")
    }

    @GetMapping("/my")
    override fun getMyTrades(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable boardType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<PageResponseDto<TradeResponseDto>> {
        val memberId = userDetails.getId()
        val type = BoardType.from(boardType)
        val trades = tradeService.getMyTrades(memberId, type, page, size)
        return ApiResponse.ok("내 거래 게시글 조회 성공", trades)
    }
}
