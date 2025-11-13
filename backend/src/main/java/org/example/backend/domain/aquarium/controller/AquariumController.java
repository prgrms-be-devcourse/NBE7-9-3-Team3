package org.example.backend.domain.aquarium.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto;
import org.example.backend.domain.aquarium.service.AquariumService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aquarium")
public class AquariumController implements AquariumControllerSpec {

  private final AquariumService aquariumService;

  @Override
  @PostMapping
  public ApiResponse<AquariumListResponseDto> createAquarium(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody AquariumRequestDto requestDto
  ) {
    AquariumListResponseDto responseDto = aquariumService.create(userDetails, requestDto);

    return ApiResponse.ok("어항이 생성되었습니다.", responseDto);
  }

  @Override
  @GetMapping
  public ApiResponse<List<AquariumResponseDto>> getAquariums(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<AquariumResponseDto> responseDto = aquariumService.findAllByMemberId(userDetails);

    return ApiResponse.ok("어항 목록이 조회되었습니다.", responseDto);
  }

  @Override
  @GetMapping("/{id}")
  public ApiResponse<AquariumResponseDto> getAquariumName(@PathVariable Long id) {
    AquariumResponseDto responseDto = aquariumService.findById(id);

    return ApiResponse.ok("어항이 조회되었습니다.", responseDto);
  }

  @Override
  @PutMapping("/{id}")
  public ApiResponse<AquariumResponseDto> updateAquariumName(
      @PathVariable Long id,
      @RequestBody AquariumRequestDto requestDto
  ) {
    AquariumResponseDto responseDto = aquariumService.updateAquariumName(id, requestDto);

    return ApiResponse.ok("어항이 수정되었습니다.", responseDto);
  }

  @Override
  @GetMapping("/{id}/delete")
  public ApiResponse<Boolean> checkFishInAquarium(@PathVariable Long id) {
    boolean hasFish = aquariumService.hasFish(id);

    return ApiResponse.ok("어항의 물고기 존재 여부를 확인했습니다.", hasFish);
  }

  @Override
  @PutMapping("/{id}/delete")
  public ApiResponse<String> moveFishToOwnedAquarium(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long id
  ) {
    aquariumService.moveFishToOwnedAquarium(userDetails, id);

    return ApiResponse.ok("물고기들이 '내가 키운 물고기' 어항으로 이동되었습니다.", "물고기 이동 완료");
  }

  @Override
  @DeleteMapping("/{id}/delete")
  public ApiResponse<Void> deleteAquarium(@PathVariable Long id) {
    aquariumService.delete(id);

    return ApiResponse.ok("어항이 삭제되었습니다.");
  }

  @Override
  @PostMapping("/{id}/schedule")
  public ApiResponse<AquariumResponseDto> scheduleSetting(
      @PathVariable Long id,
      @Valid @RequestBody AquariumScheduleRequestDto requestDto
  ) {
    AquariumResponseDto responseDto = aquariumService.scheduleSetting(id, requestDto);

    return ApiResponse.ok("물갈이&어항세척 스케줄 알림이 설정되었습니다.", responseDto);
  }

}
