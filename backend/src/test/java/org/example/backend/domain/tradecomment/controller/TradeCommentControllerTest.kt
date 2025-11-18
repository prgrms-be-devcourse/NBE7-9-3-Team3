package org.example.backend.domain.tradecomment.controller

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.domain.tradecomment.entity.TradeComment
import org.example.backend.domain.tradecomment.repository.TradeCommentRepository
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
class TradeCommentControllerTest {
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

    @Autowired
    private lateinit var tradeCommentRepository: TradeCommentRepository

    private lateinit var accessToken: String
    private lateinit var testMember: Member
    private lateinit var testTrade: Trade

    @BeforeEach
    fun setUp() {
        val member = Member(
            "test@test.com",
            "test1234",
            "테스트",
            "https://example.com/img1.jpg"
        )
        testMember = memberRepository.save(member)
        requireNotNull(testMember.memberId)
        accessToken = authTokenService.genAccessToken(testMember)

        // 테스트용 거래 게시글 생성
        val trade = Trade(
            testMember,
            BoardType.SECONDHAND,
            "60cm 수조 판매",
            "사용감 적은 60cm 수조입니다",
            50000L,
            TradeStatus.SELLING,
            "수조",
            LocalDateTime.now()
        )
        testTrade = tradeRepository.save(trade)
    }

    // ========== CREATE 테스트 ==========
    @Test
    @DisplayName("t1: 댓글 생성 성공")
    fun t1() {
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": "수조 아직 판매중인가요?"
            }    
        """.trimIndent()

        mvc.perform(
            post("$API_PATH/${testTrade.tradeId}/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 등록 성공"))
            .andExpect(jsonPath("$.data.commentId").isNumber)
            .andExpect(jsonPath("$.data.memberId").isNumber)
            .andExpect(jsonPath("$.data.memberNickname").value("테스트"))
            .andExpect(jsonPath("$.data.tradeId").value(testTrade.tradeId))
            .andExpect(jsonPath("$.data.comment").value("수조 아직 판매중인가요?"))
    }

    @Test
    @DisplayName("t2: 댓글 생성 실패 - 필수 필드 누락 (content)")
    fun t2() {
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId}
            }
        """.trimIndent()

        mvc.perform(
            post("$API_PATH/${testTrade.tradeId}/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t3: 댓글 생성 실패 - content가 빈 문자열")
    fun t3() {
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": ""
            }
        """.trimIndent()

        mvc.perform(
            post("$API_PATH/${testTrade.tradeId}/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t4: 댓글 생성 실패 - 인증 토큰 없음")
    fun t4() {
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": "테스트 댓글"
            }
        """.trimIndent()

        mvc.perform(
            post("$API_PATH/${testTrade.tradeId}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    // ========== READ 테스트 ==========
    @Test
    @DisplayName("t5: 댓글 목록 조회 성공")
    fun t5() {
        // given: 여러 개의 댓글 생성
        repeat(3) { i ->
            val comment = TradeComment(
                testMember,
                testTrade,
                "테스트 댓글 ${i + 1}"
            )
            tradeCommentRepository.save(comment)
        }

        // when & then
        mvc.perform(
            get("$API_PATH/${testTrade.tradeId}/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].comment").value("테스트 댓글 1"))
            .andExpect(jsonPath("$.data[1].comment").value("테스트 댓글 2"))
            .andExpect(jsonPath("$.data[2].comment").value("테스트 댓글 3"))
    }

    @Test
    @DisplayName("t6: 댓글 목록 조회 성공 - 댓글이 없는 경우")
    fun t6() {
        // given: 댓글이 없는 상태
        // when & then
        mvc.perform(
            get("$API_PATH/${testTrade.tradeId}/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(0))
    }

    @Test
    @DisplayName("t7: 댓글 목록 조회 성공 - 다른 사용자의 댓글 포함")
    fun t7() {
        // given: 테스트 사용자의 댓글 생성
        val comment1 = TradeComment(
            testMember,
            testTrade,
            "내 댓글"
        )
        tradeCommentRepository.save(comment1)

        // 다른 사용자 생성 및 댓글 작성
        val otherMember = Member(
            "other@test.com",
            "test1234",
            "다른사용자",
            "https://example.com/img2.jpg"
        )
        val savedOtherMember = memberRepository.save(otherMember)
        val comment2 = TradeComment(
            savedOtherMember,
            testTrade,
            "다른 사람 댓글"
        )
        tradeCommentRepository.save(comment2)

        // when & then
        mvc.perform(
            get("$API_PATH/${testTrade.tradeId}/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
    }

    // ========== UPDATE 테스트 ==========
    @Test
    @DisplayName("t8: 댓글 수정 성공")
    fun t8() {
        // given: 댓글 생성
        val comment = TradeComment(
            testMember,
            testTrade,
            "원래 댓글 내용"
        )
        val savedComment = tradeCommentRepository.save(comment)

        // when: 댓글 수정
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": "수정된 댓글 내용"
            }
        """.trimIndent()

        // then
        mvc.perform(
            put("$API_PATH/${testTrade.tradeId}/comments/${savedComment.commentId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 수정 성공"))
            .andExpect(jsonPath("$.data.commentId").value(savedComment.commentId))
            .andExpect(jsonPath("$.data.comment").value("수정된 댓글 내용"))
    }

    @Test
    @DisplayName("t9: 댓글 수정 실패 - 다른 사용자의 댓글")
    fun t9() {
        // given: 다른 사용자의 댓글 생성
        val otherMember = Member(
            "other@test.com",
            "test1234",
            "다른사용자",
            "https://example.com/img2.jpg"
        )
        val savedOtherMember = memberRepository.save(otherMember)
        val comment = TradeComment(
            savedOtherMember,
            testTrade,
            "다른 사람의 댓글"
        )
        val savedComment = tradeCommentRepository.save(comment)

        // when: 내 토큰으로 다른 사람의 댓글 수정 시도
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": "수정 시도"
            }
        """.trimIndent()

        // then: 권한 없음 에러
        mvc.perform(
            put("$API_PATH/${testTrade.tradeId}/comments/${savedComment.commentId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t10: 댓글 수정 실패 - 존재하지 않는 댓글 ID")
    fun t10() {
        // given: 존재하지 않는 댓글 ID
        val nonExistentId = 99999L

        // when & then
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": "수정 시도"
            }
        """.trimIndent()

        mvc.perform(
            put("$API_PATH/${testTrade.tradeId}/comments/$nonExistentId")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t11: 댓글 수정 실패 - content가 빈 문자열")
    fun t11() {
        // given: 댓글 생성
        val comment = TradeComment(
            testMember,
            testTrade,
            "원래 댓글 내용"
        )
        val savedComment = tradeCommentRepository.save(comment)

        // when: 빈 내용으로 수정 시도
        val requestBody = """
            {
                "memberId": ${testMember.memberId},
                "tradeId": ${testTrade.tradeId},
                "content": ""
            }
        """.trimIndent()

        // then: 유효성 검증 에러
        mvc.perform(
            put("$API_PATH/${testTrade.tradeId}/comments/${savedComment.commentId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    // ========== DELETE 테스트 ==========
    @Test
    @DisplayName("t12: 댓글 삭제 성공")
    fun t12() {
        // given: 댓글 생성
        val comment = TradeComment(
            testMember,
            testTrade,
            "삭제할 댓글"
        )
        val savedComment = tradeCommentRepository.save(comment)

        // when & then: 댓글 삭제
        mvc.perform(
            delete("$API_PATH/${testTrade.tradeId}/comments/${savedComment.commentId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 삭제 성공"))
    }

    @Test
    @DisplayName("t13: 댓글 삭제 실패 - 다른 사용자의 댓글")
    fun t13() {
        // given: 다른 사용자의 댓글 생성
        val otherMember = Member(
            "other2@test.com",
            "test1234",
            "다른사용자2",
            "https://example.com/img3.jpg"
        )
        val savedOtherMember = memberRepository.save(otherMember)
        val comment = TradeComment(
            savedOtherMember,
            testTrade,
            "다른 사람의 댓글"
        )
        val savedComment = tradeCommentRepository.save(comment)

        // when & then: 내 토큰으로 다른 사람의 댓글 삭제 시도시 권한 없음 에러
        mvc.perform(
            delete("$API_PATH/${testTrade.tradeId}/comments/${savedComment.commentId}")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t14: 댓글 삭제 실패 - 존재하지 않는 댓글 ID")
    fun t14() {
        // given: 존재하지 않는 댓글 ID
        val nonExistentId = 99999L

        // when & then
        mvc.perform(
            delete("$API_PATH/${testTrade.tradeId}/comments/$nonExistentId")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t15: 댓글 삭제 실패 - 인증 토큰 없음")
    fun t15() {
        // given: 댓글 생성
        val comment = TradeComment(
            testMember,
            testTrade,
            "삭제할 댓글"
        )
        val savedComment = tradeCommentRepository.save(comment)

        // when & then: Authorization 헤더 없이 삭제 시도시 인증 에러
        mvc.perform(
            delete("$API_PATH/${testTrade.tradeId}/comments/${savedComment.commentId}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    companion object {
        private const val API_PATH = "/api/market/secondhand"
    }
}

