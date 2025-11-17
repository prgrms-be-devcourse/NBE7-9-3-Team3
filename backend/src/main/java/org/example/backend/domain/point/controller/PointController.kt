package org.example.backend.domain.point.controller

import org.example.backend.domain.point.dto.PointHistoryResponseDto
import org.example.backend.domain.point.dto.PurchaseRequestDto
import org.example.backend.domain.point.service.PointService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.response.ApiResponse.Companion.ok
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/points")
class PointController(
    private val pointService: PointService
) : PointControllerSpec {

    @PostMapping("/members/charge/{amount}")
    override fun chargePoint(
        @PathVariable amount: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.POINT_MEMBER_NOT_FOUND)
        pointService.chargePoint(memberId, amount)
        return ApiResponse.ok("포인트 충전 완료")
    }

    @GetMapping("/members/history")
    override fun getPointHistory(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<PointHistoryResponseDto>> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.POINT_MEMBER_NOT_FOUND)
        val pointHistory = pointService.getPointHistory(memberId)
        return ok("포인트 조회 완료", pointHistory)
    }

    @PostMapping("/members/purchase")
    override fun purchaseItem(
        @RequestBody request: PurchaseRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void> {
        val buyerId = userDetails.id ?: throw BusinessException(ErrorCode.POINT_BUYER_NOT_FOUND)
        pointService.purchaseItem(buyerId, request)
        return ApiResponse.ok("포인트 결제 완료")
    }
}
