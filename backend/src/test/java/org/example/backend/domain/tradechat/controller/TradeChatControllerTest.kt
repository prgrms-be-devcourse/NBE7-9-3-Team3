package org.example.backend.domain.tradechat.controller

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.domain.tradechat.entity.ChatStatus
import org.example.backend.domain.tradechat.entity.TradeChatRoom
import org.example.backend.domain.tradechat.repository.TradeChatRoomRepository
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
class TradeChatControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc
    @Autowired
    private lateinit var memberRepository: MemberRepository
    @Autowired
    private lateinit var tradeRepository: TradeRepository
    @Autowired
    private lateinit var tradeChatRoomRepository: TradeChatRoomRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var loginUtil: LoginUtil
    private lateinit var pointUtil: PointUtil
    private lateinit var testMember: Member
    private lateinit var jwtToken: String

    @BeforeEach
    fun setUp() {
        loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        pointUtil = PointUtil(tradeRepository, memberRepository, passwordEncoder)

        jwtToken = loginUtil!!.createMemberAndGetToken(
            "point@test.com",
            "test1234",
            "point",
            ""
        )
        testMember = loginUtil.getMemberByEmail("point@test.com")
    }

    @Test
    @DisplayName("t1: 채팅방 생성 성공")
    fun t1() {
        val seller = pointUtil.createSeller()
        val trade = pointUtil.createTrade(seller, 5000L)

        mvc.perform(
            MockMvcRequestBuilders.post("/api/chat/{tradeId}/room", trade.tradeId)
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("채팅방이 생성되었습니다."))
    }

    @Test
    @DisplayName("t2: 채팅방 상세 조회 성공")
    fun t2() {
        val seller = pointUtil.createSeller()
        val buyer = testMember
        val trade = pointUtil.createTrade(seller, 5000L)

        // 채팅방 생성
        val room = tradeChatRoomRepository.save<TradeChatRoom>(
            TradeChatRoom.builder()
                .trade(trade)
                .sellerId(seller)
                .buyerId(buyer)
                .status(ChatStatus.ONGOING)
                .build()
        )
        val roomId = room.id

        mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms/{roomId}", roomId)
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("채팅방 거래정보를 조회했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.roomId").value(roomId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.tradeId").value(trade.tradeId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.sellerId").value(seller.memberId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.buyerId").value(buyer.memberId))
    }

    @Test
    @DisplayName("t3: 채팅방 상세 조회 실패 - 제 3자 접근")
    fun t3() {
        val seller = pointUtil.createSeller()
        val buyer = testMember

        // 제 3자 생성(관계없는)
        val otherToken = loginUtil.createMemberAndGetToken(
            "other@test.com",
            "other1234",
            "other",
            ""
        )
        val other = loginUtil.getMemberByEmail("other@test.com")

        val trade = pointUtil.createTrade(seller, 5000L)
        val room = tradeChatRoomRepository.save<TradeChatRoom>(
            TradeChatRoom.builder()
                .trade(trade)
                .sellerId(seller)
                .buyerId(buyer)
                .status(ChatStatus.ONGOING)
                .build()
        )
        val roomId = room.id

        mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms/{roomId}", roomId)
                .header("Authorization", "Bearer $otherToken")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }

    @Test
    @DisplayName("t4: 채팅방 내 메세지 내역 조회 성공")
    fun t4() {
        val seller = pointUtil.createSeller()
        val buyer = testMember
        val trade = pointUtil.createTrade(seller, 5000L)

        // 채팅방 생성
        val room = tradeChatRoomRepository.save<TradeChatRoom>(
            TradeChatRoom.builder()
                .trade(trade)
                .sellerId(seller)
                .buyerId(buyer)
                .status(ChatStatus.ONGOING)
                .build()
        )
        val roomId = room.id

        mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms/messages/{roomId}", roomId)
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("채팅 내역을 조회했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
    }

    @Test
    @DisplayName("t5: 채팅방 내 메세지 내역 조회 실패 - 제 3자 접근")
    fun t5() {
        val seller = pointUtil.createSeller()
        val buyer = testMember

        // 제 3자 생성(관계없는)
        val otherToken = loginUtil.createMemberAndGetToken(
            "other@test.com",
            "other1234",
            "other",
            ""
        )
        val other = loginUtil.getMemberByEmail("other@test.com")

        val trade = pointUtil.createTrade(seller, 5000L)
        val room = tradeChatRoomRepository.save<TradeChatRoom>(
            TradeChatRoom.builder()
                .trade(trade)
                .sellerId(seller)
                .buyerId(buyer)
                .status(ChatStatus.ONGOING)
                .build()
        )
        val roomId = room.id

        mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms/messages/{roomId}", roomId)
                .header("Authorization", "Bearer $otherToken")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN007")) // ErrorCode.FORBIDDEN_ACCESS
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }

    @Test
    @DisplayName("t6: 채팅방 목록 조회 성공")
    fun t6() {
        val seller = pointUtil.createSeller()
        val buyer = testMember
        val trade = pointUtil.createTrade(seller, 5000L)

        // 채팅방 생성
        val room = tradeChatRoomRepository.save<TradeChatRoom>(
            TradeChatRoom.builder()
                .trade(trade)
                .sellerId(seller)
                .buyerId(buyer)
                .status(ChatStatus.ONGOING)
                .build()
        )

        mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms/me")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("채팅방 목록을 조회했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
    }
}
