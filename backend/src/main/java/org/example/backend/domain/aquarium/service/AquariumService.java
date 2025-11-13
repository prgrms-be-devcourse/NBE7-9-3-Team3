package org.example.backend.domain.aquarium.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumLogRepository;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AquariumService {

  private final AquariumRepository aquariumRepository;
  private final AquariumLogRepository aquariumLogRepository;
  private final MemberRepository memberRepository;
  private final FishRepository fishRepository;

  public long count() {
    return aquariumRepository.count();
  }

  public AquariumListResponseDto create(CustomUserDetails userDetails, AquariumRequestDto requestDto) {
    Long memberId = userDetails.getId(); // JWT 토큰을 이용해 로그인한 member의 id를 가져옴
    String aquariumName = requestDto.aquariumName();

    if (aquariumName.equals("내가 키운 물고기")) {
      throw new BusinessException(ErrorCode.AQUARIUM_OWNED_ALREADY_HAVE);
    }

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    Aquarium aquarium = new Aquarium(member, aquariumName);
    aquariumRepository.save(aquarium);

    AquariumListResponseDto responseDto = new AquariumListResponseDto(aquarium);

    return responseDto;
  }

  public List<AquariumResponseDto> findAllByMemberId(CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();

    return aquariumRepository.findAllByMember_MemberId(memberId)
        .reversed().stream().map(AquariumResponseDto::new).toList();
  }

  public AquariumResponseDto findById(Long id) {
    Aquarium aquarium = aquariumRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.AQUARIUM_NOT_FOUND));

    AquariumResponseDto responseDto = new AquariumResponseDto(aquarium);
    return responseDto;
  }

  public boolean hasFish(Long id) {
    long fishCount = fishRepository.countByAquarium_Id(id);

    if (fishCount >= 1) {
      return true;
    } else {
      return false;
    }
  }

  public void moveFishToOwnedAquarium(CustomUserDetails userDetails, Long aquariumId) {
    Long memberId = userDetails.getId();

    // 해당 member가 "내가 키운 물고기" 어항을 가지고 있는 지 확인
    if (checkMemberHaveOwnedAquarium(memberId)) {
      // true라면, 물고기 이동 실행
      moveFish(memberId, aquariumId);
    } else {
      // false라면, "내가 키운 물고기" 어항 생성 후 물고기 이동 실행
      createOwnedAquarium(memberId);
      moveFish(memberId, aquariumId);
    }
  }

  public void moveFish(Long memberId, Long aquariumId) {
    // 삭제할 어항의 모든 물고기 가져오기
    List<Fish> fishList = fishRepository.findAllByAquarium_Id(aquariumId);

    // '내가 키운 물고기' 어항 찾기
    Aquarium myOwnedAquarium = aquariumRepository.findByMember_MemberIdAndOwnedAquariumTrue(
            memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.AQUARIUM_OWNED_NOT_FOUND));

    // 물고기들을 '내가 키운 물고기' 어항으로 이동
    for (Fish fish : fishList) {
      fish.changeAquarium(myOwnedAquarium);
    }
    fishRepository.saveAll(fishList);
  }

  // "내가 키운 물고기" 어항을 가지고 있는 지 확인
  public boolean checkMemberHaveOwnedAquarium(Long memberId) {
    return aquariumRepository.existsByMember_MemberIdAndOwnedAquariumTrue(memberId);
  }

  // "내가 키운 물고기" 어항 생성
  public void createOwnedAquarium(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    Aquarium aquarium = new Aquarium(member, "내가 키운 물고기", true);

    aquariumRepository.save(aquarium);
  }

  @Transactional
  public void delete(Long id) {
    Aquarium aquarium = aquariumRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.AQUARIUM_NOT_FOUND));

    aquariumLogRepository.deleteAllByAquarium(aquarium);
    aquariumRepository.deleteById(id);
  }

  /*
  어항 알림 스케줄 세팅

  1. 기본 배경
    - cycleDate의 기본 값은 0이다.
    - cycleDate가 0이라면, 알림 기능은 작동하지 않는다.
      - cycleDate = 0
      - lastDate, nextDate  = null

  2. 알림 스케줄 세팅 로직
    - 사용자로부터 입력 받은 cycleDate가 0이라면,
      - cycleDate = 0
      - lastDate, nextDate = null
    - 기존의 cycleDate가 0이라면,
      - lastDate, nextDate를 현재 시간 기준으로 계산
    - 기존의 cycleDate가 0이 아니라면,
      - nextDate를 사용자로부터 입력 받은 cycleDate 기준으로 재 설정
  */
  public AquariumResponseDto scheduleSetting(Long aquariumId, AquariumScheduleRequestDto requestDto) {
    Aquarium aquarium = aquariumRepository.findById(aquariumId)
        .orElseThrow(() -> new BusinessException(ErrorCode.AQUARIUM_NOT_FOUND));
    int preCycleDate = aquarium.getCycleDate();  // 기존의 cycleDate
    int cycleDate = requestDto.cycleDate();  // 입력받은 cycleDate

    // 사용자로부터 입력 받은 cycleDate가 0이라면
    if (cycleDate == 0) {
      aquarium.changeSchedule(cycleDate, null, null);
      aquariumRepository.save(aquarium);

      AquariumResponseDto responseDto = new AquariumResponseDto(aquarium);
      return responseDto;
    }

    LocalDateTime lastDate = aquarium.getLastDate();
    LocalDateTime nextDate;

    // 기존의 cycleDate가 0이라면
    if (preCycleDate == 0) {
      lastDate = LocalDateTime.now();
      nextDate = lastDate.plusDays(cycleDate);

      aquarium.changeSchedule(cycleDate, lastDate, nextDate);
    }
    // 기존의 cycleDate이 0이 아니라면,
    else if (preCycleDate != 0) {
      nextDate = lastDate.plusDays(cycleDate);

      aquarium.changeSchedule(cycleDate, lastDate, nextDate);
    }

    aquariumRepository.save(aquarium);

    AquariumResponseDto responseDto = new AquariumResponseDto(aquarium);
    return responseDto;
  }

  public AquariumResponseDto updateAquariumName(Long id, AquariumRequestDto requestDto) {
    Aquarium aquarium = aquariumRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.AQUARIUM_NOT_FOUND));
    String newName = requestDto.aquariumName();

    if (newName.equals("내가 키운 물고기")) {
      throw new BusinessException(ErrorCode.AQUARIUM_OWNED_ALREADY_HAVE);
    }

    aquarium.changeName(newName);
    aquariumRepository.save(aquarium);

    AquariumResponseDto responseDto = new AquariumResponseDto(aquarium);
    return responseDto;
  }
}
