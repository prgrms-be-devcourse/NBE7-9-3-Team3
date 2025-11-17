package org.example.backend.domain.point.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.point.dto.PointHistoryResponseDto
import org.example.backend.domain.point.dto.PurchaseRequestDto
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Point", description = "포인트 관리 API")
interface PointControllerSpec {

    @Operation(summary = "포인트 충전", description = "회원의 포인트를 충전합니다.")
    fun chargePoint(
        @Parameter(description = "포인트 충전 금액", required = true) @PathVariable amount: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void>

    @Operation(summary = "포인트 내역 조회", description = "회원의 포인트 사용 내역을 조회합니다.")
    fun getPointHistory(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<PointHistoryResponseDto>>

    @Operation(summary = "포인트 결제", description = "포인트를 사용하여 아이템을 구매합니다.")
    fun purchaseItem(
        @Parameter(description = "판매 정보", required = true) @RequestBody request: PurchaseRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void>
}
