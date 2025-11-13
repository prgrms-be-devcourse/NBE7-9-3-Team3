package org.example.backend.domain.point.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.point.dto.PointHistoryResponseDto;
import org.example.backend.domain.point.dto.PurchaseRequestDto;
import org.example.backend.domain.point.service.PointService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController implements PointControllerSpec {

    private final PointService pointService;

    @Override
    @PostMapping("/members/charge/{amount}")
    public ApiResponse<Void> chargePoint(
            @PathVariable Long amount,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        pointService.chargePoint(memberId, amount);
        return ApiResponse.ok("포인트 충전 완료");
    }

    @Override
    @GetMapping("/members/{id}/history")
    public ApiResponse<List<PointHistoryResponseDto>> getPointHistory(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<PointHistoryResponseDto> pointHistory = pointService.getPointHistory(memberId);
        return ApiResponse.ok(pointHistory);
    }

    @Override
    @PostMapping("/members/purchase")
    public ApiResponse<Void> purchaseItem(
        @RequestBody PurchaseRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long buyerId = userDetails.getId();
        pointService.purchaseItem(buyerId, request);
        return ApiResponse.ok("포인트 결제 완료");
    }
}
