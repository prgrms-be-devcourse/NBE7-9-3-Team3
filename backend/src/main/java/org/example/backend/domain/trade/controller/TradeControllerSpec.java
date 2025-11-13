package org.example.backend.domain.trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backend.domain.trade.dto.PageResponseDto;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.dto.TradeSearchRequestDto;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Trade", description = "거래 게시글 관리 API")
public interface TradeControllerSpec {

    @Operation(summary = "거래 게시글 등록", description = "새로운 거래 게시글을 등록합니다.")
    ApiResponse<TradeResponseDto> createTrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 정보", required = true)
            @RequestBody TradeRequestDto request
    );

    @Operation(summary = "거래 게시글 목록 조회", description = "특정 게시판의 모든 거래 게시글을 조회합니다.")
    ApiResponse<PageResponseDto<TradeResponseDto>> getAllTrade(
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @ModelAttribute TradeSearchRequestDto searchRequest
    );

    @Operation(summary = "거래 게시글 조회", description = "특정 거래 게시글의 상세 정보를 조회합니다.")
    ApiResponse<TradeResponseDto> getTrade(
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId
    );

    @Operation(summary = "거래 게시글 수정", description = "기존 거래 게시글을 수정합니다.")
    ApiResponse<TradeResponseDto> updateTrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId,
            @Parameter(description = "거래 게시글 수정 정보", required = true)
            @RequestBody TradeRequestDto request
    );

    @Operation(summary = "거래 게시글 삭제", description = "기존 거래 게시글을 삭제합니다.")
    ApiResponse<Void> deleteTrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId
    );

    @Operation(summary = "내 거래 게시글 조회", description = "사용자가 작성한 거래 게시글 목록을 조회합니다. (최신순 정렬)")
    ApiResponse<PageResponseDto<TradeResponseDto>> getMyTrades(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    );
}
