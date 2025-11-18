package org.example.backend.domain.point.controller

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.point.entity.Point
import org.example.backend.domain.point.repository.PointRepository
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.global.LoginUtil
import org.example.backend.global.PointUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(
    TestInstance.Lifecycle.PER_METHOD
)
class PointControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc
    @Autowired
    private lateinit var memberRepository: MemberRepository
    @Autowired
    private lateinit var pointRepository: PointRepository
    @Autowired
    private lateinit var tradeRepository: TradeRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var pointUtil: PointUtil
    private lateinit var testMember: Member
    private lateinit var jwtToken: String

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        pointUtil = PointUtil(tradeRepository, memberRepository, passwordEncoder)

        jwtToken = loginUtil.createMemberAndGetToken(
            "point@test.com",
            passwordEncoder.encode("point1234"),
            "point",
            ""
        )
        testMember = loginUtil.getMemberByEmail("point@test.com")
    }

    @Test
    @DisplayName("t1: 포인트 충전 성공")
    @Throws(Exception::class)
    fun t1() {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/points/members/charge/{amount}", 5000)
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("포인트 충전 완료"))
    }

    @Test
    @DisplayName("t2: 포인트 내역 조회 성공")
    @Throws(Exception::class)
    fun t2() {
        pointRepository.save<Point>(Point.create(testMember, 5000L, 5000L))

        mvc.perform(
            MockMvcRequestBuilders.get("/api/points/members/history")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("포인트 조회 완료"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].type").value("CHARGE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].points").value(5000))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].afterPoint").value(5000))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].date").exists())
    }

    @Test
    @DisplayName("t3: 포인트 내역 조회 실패 - 포인트 기록 없음")
    @Throws(Exception::class)
    fun t3() {
        mvc.perform(
            MockMvcRequestBuilders.get("/api/points/members/history")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("P002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("포인트 내역이 존재하지 않습니다."))
    }

    @Test
    @DisplayName("t4: 포인트로 상품 결제 성공")
    @Throws(Exception::class)
    fun t4() {
        val seller = pointUtil.createSeller()
        val trade = pointUtil.createTrade(seller, 5000L)

        // 구매자 포인트 충전
        testMember.updatePoints(10000L)
        memberRepository.save<Member?>(testMember)

        val requestBody = pointUtil.purchaseRequest(
            seller.memberId,
            trade.price,
            trade.tradeId
        )

        mvc.perform(
            MockMvcRequestBuilders.post("/api/points/members/purchase")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("포인트 결제 완료"))
    }

    @Test
    @DisplayName("t5: 포인트로 상품 결제 실패 - 이미 판매 완료된 상품")
    fun t5() {
        val seller = pointUtil.createSeller()
        val trade = pointUtil.createTrade(seller, 5000L)

        // 상품을 판매완료로 상태변경
        trade.completeTransaction()
        tradeRepository.save<Trade?>(trade)

        testMember.updatePoints(10000L)
        memberRepository.save<Member?>(testMember)

        val requestBody = pointUtil.purchaseRequest(
            seller.memberId,
            trade.price,
            trade.tradeId
        )

        mvc.perform(
            MockMvcRequestBuilders.post("/api/points/members/purchase")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("T005"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("해당 물품은 이미 판매되었습니다."))
    }

    @Test
    @DisplayName("t6: 포인트로 상품 결제 실패 - 포인트 부족")
    fun t6() {
        val seller = pointUtil.createSeller()
        val trade = pointUtil.createTrade(seller, 5000L)

        // 구매자 포인트 부족하게 설정
        testMember.updatePoints(3000L)
        memberRepository.save(testMember)

        val requestBody = pointUtil.purchaseRequest(
            seller.memberId,
            trade.price,
            trade.tradeId
        )

        mvc.perform(
            MockMvcRequestBuilders.post("/api/points/members/purchase")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("P003"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("포인트가 부족합니다."))
    }
}





