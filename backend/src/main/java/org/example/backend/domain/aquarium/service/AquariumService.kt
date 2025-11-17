package org.example.backend.domain.aquarium.service

import lombok.RequiredArgsConstructor
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto
import org.example.backend.domain.aquarium.dto.AquariumRequestDto
import org.example.backend.domain.aquarium.dto.AquariumResponseDto
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumLogRepository
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.repository.FishRepository
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.security.CustomUserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.function.Supplier

@Service
@RequiredArgsConstructor
class AquariumService {
    private val aquariumRepository: AquariumRepository? = null
    private val aquariumLogRepository: AquariumLogRepository? = null
    private val memberRepository: MemberRepository? = null
    private val fishRepository: FishRepository? = null

    fun count(): Long {
        return aquariumRepository!!.count()
    }

    fun create(userDetails: CustomUserDetails, requestDto: AquariumRequestDto): AquariumListResponseDto {
        val memberId = userDetails.id // JWT 토큰을 이용해 로그인한 member의 id를 가져옴
        val aquariumName = requestDto.aquariumName

        if (aquariumName == "내가 키운 물고기") {
            throw BusinessException(ErrorCode.AQUARIUM_OWNED_ALREADY_HAVE)
        }

        val member = memberRepository!!.findById(memberId!!)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.MEMBER_NOT_FOUND) })

        val aquarium = Aquarium(member, aquariumName)
        aquariumRepository!!.save<Aquarium?>(aquarium)

        val responseDto = AquariumListResponseDto(aquarium)

        return responseDto
    }

    fun findAllByMemberId(userDetails: CustomUserDetails): MutableList<AquariumResponseDto?> {
        val memberId = userDetails.id

        return aquariumRepository!!.findAllByMember_MemberId(memberId!!)
            .reversed().stream().map<AquariumResponseDto?> { aquarium: Aquarium? -> AquariumResponseDto(aquarium!!) }
            .toList()
    }

    fun findById(id: Long): AquariumResponseDto {
        val aquarium = aquariumRepository!!.findById(id)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.AQUARIUM_NOT_FOUND) })

        val responseDto = AquariumResponseDto(aquarium)
        return responseDto
    }

    fun hasFish(id: Long?): Boolean {
        val fishCount = fishRepository!!.countByAquarium_Id(id)

        if (fishCount >= 1) {
            return true
        } else {
            return false
        }
    }

    fun moveFishToOwnedAquarium(userDetails: CustomUserDetails, aquariumId: Long?) {
        val memberId = userDetails.id

        // 해당 member가 "내가 키운 물고기" 어항을 가지고 있는 지 확인
        if (checkMemberHaveOwnedAquarium(memberId!!)) {
            // true라면, 물고기 이동 실행
            moveFish(memberId, aquariumId)
        } else {
            // false라면, "내가 키운 물고기" 어항 생성 후 물고기 이동 실행
            createOwnedAquarium(memberId)
            moveFish(memberId, aquariumId)
        }
    }

    fun moveFish(memberId: Long, aquariumId: Long?) {
        // 삭제할 어항의 모든 물고기 가져오기
        val fishList = fishRepository!!.findAllByAquarium_Id(aquariumId)

        // '내가 키운 물고기' 어항 찾기
        val myOwnedAquarium: Aquarium? = aquariumRepository!!.findByMember_MemberIdAndOwnedAquariumTrue(
            memberId
        )
            .orElseThrow({ BusinessException(ErrorCode.AQUARIUM_OWNED_NOT_FOUND) })

        // 물고기들을 '내가 키운 물고기' 어항으로 이동
        for (fish in fishList) {
            fish.changeAquarium(myOwnedAquarium)
        }
        fishRepository.saveAll<Fish?>(fishList)
    }

    // "내가 키운 물고기" 어항을 가지고 있는 지 확인
    fun checkMemberHaveOwnedAquarium(memberId: Long): Boolean {
        return aquariumRepository!!.existsByMember_MemberIdAndOwnedAquariumTrue(memberId)
    }

    // "내가 키운 물고기" 어항 생성
    fun createOwnedAquarium(memberId: Long) {
        val member = memberRepository!!.findById(memberId)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.MEMBER_NOT_FOUND) })
        val aquarium = Aquarium(member, "내가 키운 물고기", true)

        aquariumRepository!!.save<Aquarium?>(aquarium)
    }

    @Transactional
    fun delete(id: Long) {
        val aquarium = aquariumRepository!!.findById(id)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.AQUARIUM_NOT_FOUND) })

        aquariumLogRepository!!.deleteAllByAquarium(aquarium)
        aquariumRepository.deleteById(id)
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
    fun scheduleSetting(aquariumId: Long, requestDto: AquariumScheduleRequestDto): AquariumResponseDto {
        val aquarium = aquariumRepository!!.findById(aquariumId)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.AQUARIUM_NOT_FOUND) })
        val preCycleDate = aquarium.cycleDate // 기존의 cycleDate
        val cycleDate = requestDto.cycleDate // 입력받은 cycleDate

        // 사용자로부터 입력 받은 cycleDate가 0이라면
        if (cycleDate == 0) {
            aquarium.changeSchedule(cycleDate, null, null)
            aquariumRepository.save<Aquarium?>(aquarium)

            val responseDto = AquariumResponseDto(aquarium)
            return responseDto
        }

        var lastDate = aquarium.lastDate
        val nextDate: LocalDateTime?

        // 기존의 cycleDate가 0이라면
        if (preCycleDate == 0) {
            lastDate = LocalDateTime.now()
            nextDate = lastDate.plusDays(cycleDate.toLong())

            aquarium.changeSchedule(cycleDate, lastDate, nextDate)
        } else if (preCycleDate != 0) {
            nextDate = lastDate!!.plusDays(cycleDate.toLong())

            aquarium.changeSchedule(cycleDate, lastDate, nextDate)
        }

        aquariumRepository.save<Aquarium?>(aquarium)

        val responseDto = AquariumResponseDto(aquarium)
        return responseDto
    }

    fun updateAquariumName(id: Long, requestDto: AquariumRequestDto): AquariumResponseDto {
        val aquarium = aquariumRepository!!.findById(id)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.AQUARIUM_NOT_FOUND) })
        val newName = requestDto.aquariumName

        if (newName == "내가 키운 물고기") {
            throw BusinessException(ErrorCode.AQUARIUM_OWNED_ALREADY_HAVE)
        }

        aquarium.changeName(newName)
        aquariumRepository.save<Aquarium?>(aquarium)

        val responseDto = AquariumResponseDto(aquarium)
        return responseDto
    }
}
