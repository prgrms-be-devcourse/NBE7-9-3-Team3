package org.example.backend.domain.trade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.trade.dto.PageResponseDto;
import org.example.backend.domain.trade.dto.TradeCreateRequestDto;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.dto.TradeSearchRequestDto;
import org.example.backend.domain.trade.dto.TradeUpdateRequestDto;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.service.TradeService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/market/{boardType}")
@RequiredArgsConstructor
public class TradeController implements TradeControllerSpec {

    private final TradeService tradeService;

    @Override
    @PostMapping
    public ApiResponse<TradeResponseDto> createTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @Valid @RequestBody TradeRequestDto request) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeCreateRequestDto serviceRequest = TradeCreateRequestDto.from(request, type, memberId);
        TradeResponseDto trade = tradeService.createTrade(serviceRequest);
        return ApiResponse.ok("거래 게시글 등록 성공", trade);
    }

    @Override
    @GetMapping
    public ApiResponse<PageResponseDto<TradeResponseDto>> getAllTrade(
        @PathVariable String boardType,
        @Valid @ModelAttribute TradeSearchRequestDto searchRequest) {
        BoardType type = BoardType.from(boardType);
        PageResponseDto<TradeResponseDto> trades = tradeService.getAllTrade(type, searchRequest);
        return ApiResponse.ok("거래 게시글 페이징 조회 성공", trades);
    }

    @Override
    @GetMapping("/{tradeId}")
    public ApiResponse<TradeResponseDto> getTrade(
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.getTrade(type, tradeId);
        return ApiResponse.ok("거래 게시글 조회 성공", trade);
    }

    @Override
    @PutMapping("/{tradeId}")
    public ApiResponse<TradeResponseDto> updateTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @Valid @RequestBody TradeRequestDto request) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeUpdateRequestDto updateRequest = TradeUpdateRequestDto.of(type, tradeId, memberId,
            request, request.imageUrls());
        TradeResponseDto trade = tradeService.updateTrade(updateRequest);
        return ApiResponse.ok("거래 게시글 수정 성공", trade);
    }

    @Override
    @DeleteMapping("/{tradeId}")
    public ApiResponse<Void> deleteTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        tradeService.deleteTrade(type, tradeId, memberId);
        return ApiResponse.ok("거래 게시글 삭제 성공");
    }

    @Override
    @GetMapping("/my")
    public ApiResponse<PageResponseDto<TradeResponseDto>> getMyTrades(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        PageResponseDto<TradeResponseDto> trades = tradeService.getMyTrades(memberId, type, page,
            size);
        return ApiResponse.ok("내 거래 게시글 조회 성공", trades);
    }
}
