package org.example.backend.domain.tradecomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.tradecomment.dto.MyTradeCommentReadResponseDto;
import org.example.backend.domain.tradecomment.service.TradeCommentService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market/comments")
@RequiredArgsConstructor
@Tag(name = "My Trade Comment", description = "내가 작성한 거래글 댓글 관리 API")
public class MyTradeCommentController {

    private final TradeCommentService tradeCommentService;

    @Operation(summary = "내가 작성한 거래글 댓글 조회", description = "현재 사용자가 작성한 모든 거래글 댓글을 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<List<MyTradeCommentReadResponseDto>> getMyTradeComments(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MyTradeCommentReadResponseDto> comments = tradeCommentService.getMyTradeComments(userDetails.getId());
        return ApiResponse.ok("내가 작성한 거래글 댓글 조회 성공", comments);
    }
}
