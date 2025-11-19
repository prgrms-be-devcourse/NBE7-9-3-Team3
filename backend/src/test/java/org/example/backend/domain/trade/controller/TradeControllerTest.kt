package org.example.backend.domain.trade.controller

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import org.example.backend.domain.trade.repository.TradeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.LocalDateTime

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TradeControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var s3Client: S3Client

    @MockitoBean
    private lateinit var s3Presigner: S3Presigner

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var tradeRepository: TradeRepository

    private lateinit var accessToken: String
    private lateinit var testMember: Member

    @BeforeEach
    fun setUp() {
        val member = Member(
            "test@test.com",
            "test1234",
            "테스트",
            "https://example.com/img1.jpg"
        )
        testMember = memberRepository.save(member)
        accessToken = authTokenService.genAccessToken(testMember)
    }

    // ========== CREATE 테스트 ==========
    @Test
    @DisplayName("t1: 거래 생성 성공")
    fun t1() {
        val requestBody = """
            {
                "title": "60cm 수조 판매합니다",
                "description": "사용감 적은 60cm 수조 팝니다. 상태 양호합니다.",
                "price": 50000,
                "status": "SELLING",
                "category": "수조",
                "imageUrls": ["https://example.com/img1.jpg"]
            }
        """.trimIndent()

        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("거래 게시글 등록 성공"))
            .andExpect(jsonPath("$.data.tradeId").isNumber)
            .andExpect(jsonPath("$.data.status").value("SELLING"))
            .andExpect(jsonPath("$.data.title").value("60cm 수조 판매합니다"))
            .andExpect(jsonPath("$.data.price").value(50000))
    }

    @Test
    @DisplayName("t2: 거래 생성 실패 - 필수 필드 누락 (title)")
    fun t2() {
        // given: title 필드가 없는 요청
        val requestBody = """
            {
                "description": "사용감 적은 60cm 수조 팝니다.",
                "price": 50000,
                "status": "SELLING",
                "category": "수조",
                "imageUrls": ["https://example.com/img1.jpg"]
            }
        """.trimIndent()

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t3: 거래 생성 실패 - 가격이 음수")
    fun t3() {
        // given: 음수 가격 요청
        val requestBody = """
            {
                "title": "산소공급기 판매합니다",
                "description": "에어펌프 판매합니다. 소음 거의 없습니다.",
                "price": -10000,
                "status": "SELLING",
                "category": "용품",
                "imageUrls": ["https://example.com/img1.jpg"]
            }
        """.trimIndent()

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t4: 거래 생성 실패 - 인증 토큰 없음")
    fun t4() {
        // given: 정상적인 요청 본문
        val requestBody = """
            {
                "title": "여과기 판매합니다",
                "description": "외부 여과기 판매합니다. 거의 새것입니다.",
                "price": 80000,
                "status": "SELLING",
                "category": "여과기",
                "imageUrls": ["https://example.com/img1.jpg"]
            }
        """.trimIndent()

        // when & then: Authorization 헤더 없이 요청시 인증 에러
        mvc.perform(
            post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    // ========== READ 테스트 ==========
    @Test
    @DisplayName("t5: 거래 단건 조회 성공")
    fun t5() {
        // given: 거래 게시글 생성
        val trade = Trade(
            testMember,
            BoardType.SECONDHAND,
            "LED 조명 판매",
            "수족관용 LED 조명 판매합니다. 밝기 조절 가능합니다.",
            35000L,
            TradeStatus.SELLING,
            "조명",
            LocalDateTime.now()
        ).apply {
            addImage("https://example.com/img1.jpg")
        }
        val savedTrade = tradeRepository.save(trade)

        // when & then
        mvc.perform(
            get("$API_PATH/${savedTrade.tradeId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("거래 게시글 조회 성공"))
            .andExpect(jsonPath("$.data.tradeId").value(savedTrade.tradeId))
            .andExpect(jsonPath("$.data.title").value("LED 조명 판매"))
            .andExpect(jsonPath("$.data.description").value("수족관용 LED 조명 판매합니다. 밝기 조절 가능합니다."))
            .andExpect(jsonPath("$.data.price").value(35000))
            .andExpect(jsonPath("$.data.status").value("SELLING"))
            .andExpect(jsonPath("$.data.category").value("조명"))
            .andExpect(jsonPath("$.data.images[0]").value("https://example.com/img1.jpg"))
    }

    @Test
    @DisplayName("t6: 거래 조회 실패 - 존재하지 않는 ID")
    fun t6() {
        // given: 존재하지 않는 거래 ID
        val nonExistentId = 99999L

        // when & then
        mvc.perform(
            get("$API_PATH/$nonExistentId")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t7: 거래 목록 조회 성공 (페이징)")
    fun t7() {
        // given: 여러 개의 거래 게시글 생성
        repeat(5) { i ->
            val trade = Trade(
                testMember,
                BoardType.SECONDHAND,
                "수족관 용품 ${i + 1}",
                "설명 ${i + 1}",
                10000L * (i + 1),
                TradeStatus.SELLING,
                "용품",
                LocalDateTime.now()
            )
            tradeRepository.save(trade)
        }

        // when & then: 첫 페이지 조회 (size=3)
        mvc.perform(
            get(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .param("page", "0")
                .param("size", "3")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("거래 게시글 페이징 조회 성공"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(3))
            .andExpect(jsonPath("$.data.totalElements").value(5))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.currentPage").value(0))
            .andExpect(jsonPath("$.data.pageSize").value(3))
    }

    @Test
    @DisplayName("t8: 내 거래 게시글 조회 성공")
    fun t8() {
        // given: 현재 사용자의 거래 게시글 3개 생성
        repeat(3) { i ->
            val trade = Trade(
                testMember,
                BoardType.SECONDHAND,
                "내 거래 ${i + 1}",
                "설명 ${i + 1}",
                10000L * (i + 1),
                TradeStatus.SELLING,
                "용품",
                LocalDateTime.now()
            )
            tradeRepository.save(trade)
        }

        // 다른 사용자의 거래 게시글도 생성
        val otherMember = memberRepository.save(
            Member(
                "other@test.com",
                "test1234",
                "다른사용자",
                "https://example.com/img2.jpg"
            )
        )
        tradeRepository.save(
            Trade(
                otherMember,
                BoardType.SECONDHAND,
                "다른 사람 거래",
                "설명",
                5000L,
                TradeStatus.SELLING,
                "수조",
                LocalDateTime.now()
            )
        )

        // when & then: 내 거래만 조회
        mvc.perform(
            get("$API_PATH/my")
                .header("Authorization", "Bearer $accessToken")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("내 거래 게시글 조회 성공"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(3))
            .andExpect(jsonPath("$.data.totalElements").value(3))
    }

    // ========== UPDATE 테스트 ==========
    @Test
    @DisplayName("t9: 거래 수정 성공")
    fun t9() {
        // given: 거래 게시글 생성 (이미지 없이)
        val savedTrade = tradeRepository.save(
            Trade(
                testMember,
                BoardType.SECONDHAND,
                "45cm 수조 판매",
                "45cm 수조 판매합니다",
                30000L,
                TradeStatus.SELLING,
                "수조",
                LocalDateTime.now()
            )
        )

        // when & then: 거래 게시글 수정
        val requestBody = """
            {
                "title": "45cm 수조 급매",
                "description": "45cm 수조 급하게 팝니다. 가격 네고 가능",
                "price": 25000,
                "status": "SELLING",
                "category": "수조",
                "imageUrls": []
            }
        """.trimIndent()

        mvc.perform(
            put("$API_PATH/${savedTrade.tradeId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("거래 게시글 수정 성공"))
            .andExpect(jsonPath("$.data.tradeId").value(savedTrade.tradeId))
            .andExpect(jsonPath("$.data.title").value("45cm 수조 급매"))
            .andExpect(jsonPath("$.data.description").value("45cm 수조 급하게 팝니다. 가격 네고 가능"))
            .andExpect(jsonPath("$.data.price").value(25000))
            .andExpect(jsonPath("$.data.status").value("SELLING"))
    }

    @Test
    @DisplayName("t10: 거래 수정 실패 - 다른 사용자의 거래")
    fun t10() {
        // given: 다른 사용자의 거래 게시글 생성
        val otherMember = memberRepository.save(
            Member(
                "other@test.com",
                "test1234",
                "다른사용자",
                "https://example.com/img2.jpg"
            )
        )
        val savedTrade = tradeRepository.save(
            Trade(
                otherMember,
                BoardType.SECONDHAND,
                "다른 사람 거래",
                "설명",
                5000L,
                TradeStatus.SELLING,
                "히터",
                LocalDateTime.now()
            )
        )

        // when: 내 토큰으로 다른 사람의 거래 수정 시도
        val requestBody = """
            {
                "title": "수정 시도",
                "description": "수정",
                "price": 10000,
                "status": "SELLING",
                "category": "히터",
                "imageUrls": []
            }
        """.trimIndent()

        // then: 권한 없음 에러
        mvc.perform(
            put("$API_PATH/${savedTrade.tradeId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    // ========== DELETE 테스트 ==========
    @Test
    @DisplayName("t11: 거래 삭제 성공")
    fun t11() {
        // given: 거래 게시글 생성
        val savedTrade = tradeRepository.save(
            Trade(
                testMember,
                BoardType.SECONDHAND,
                "온도계 판매",
                "디지털 온도계 판매합니다",
                8000L,
                TradeStatus.SELLING,
                "용품",
                LocalDateTime.now()
            )
        )

        // when & then
        mvc.perform(
            delete("$API_PATH/${savedTrade.tradeId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("거래 게시글 삭제 성공"))
    }

    @Test
    @DisplayName("t12: 거래 삭제 실패 - 다른 사용자의 거래")
    fun t12() {
        // given: 다른 사용자의 거래 게시글 생성
        val otherMember = memberRepository.save(
            Member(
                "other2@test.com",
                "test1234",
                "다른사용자2",
                "https://example.com/img3.jpg"
            )
        )
        val savedTrade = tradeRepository.save(
            Trade(
                otherMember,
                BoardType.SECONDHAND,
                "다른 사람 거래",
                "설명",
                5000L,
                TradeStatus.SELLING,
                "조명",
                LocalDateTime.now()
            )
        )

        // when & then: 권한 없음 에러
        mvc.perform(
            delete("$API_PATH/${savedTrade.tradeId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    companion object {
        private const val API_PATH = "/api/market/secondhand"
    }
}
