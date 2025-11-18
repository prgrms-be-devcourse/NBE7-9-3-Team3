package org.example.backend.domain.tradecomment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.domain.tradecomment.entity.TradeComment;
import org.example.backend.domain.tradecomment.repository.TradeCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class TradeCommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private S3Presigner s3Presigner;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeCommentRepository tradeCommentRepository;

    private String accessToken;
    private Member testMember;
    private Trade testTrade;

    @BeforeEach
    void setUp() {
        Member member = new Member(
            "test@test.com",
            "test1234",
            "테스트",
            "https://example.com/img1.jpg"
        );
        testMember = memberRepository.save(member);
        accessToken = authTokenService.genAccessToken(testMember);

        // 테스트용 거래 게시글 생성
        Trade trade = new Trade(
            testMember,
            BoardType.SECONDHAND,
            "60cm 수조 판매",
            "사용감 적은 60cm 수조입니다",
            50000L,
            TradeStatus.SELLING,
            "수조",
            LocalDateTime.now()
        );
        testTrade = tradeRepository.save(trade);
    }

    // ========== CREATE 테스트 ==========
    @Test
    @DisplayName("t1: 댓글 생성 성공")
    void t1() throws Exception {
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": "수조 아직 판매중인가요?"
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            post("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 등록 성공"))
            .andExpect(jsonPath("$.data.commentId").isNumber())
//            .andExpect(jsonPath("$.data.memberId").value(testMember.getMemberId()))  // TODO : Member Kotlin 전환 후 활성화
            .andExpect(jsonPath("$.data.memberNickname").value("테스트"))
            .andExpect(jsonPath("$.data.tradeId").value(testTrade.getTradeId()))
            .andExpect(jsonPath("$.data.comment").value("수조 아직 판매중인가요?"));
    }

    @Test
    @DisplayName("t2: 댓글 생성 실패 - 필수 필드 누락 (content)")
    void t2() throws Exception {
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            post("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t3: 댓글 생성 실패 - content가 빈 문자열")
    void t3() throws Exception {
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": ""
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            post("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t4: 댓글 생성 실패 - 인증 토큰 없음")
    void t4() throws Exception {
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": "테스트 댓글"
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            post("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().is4xxClientError());
    }

    // ========== READ 테스트 ==========
    @Test
    @DisplayName("t5: 댓글 목록 조회 성공")
    void t5() throws Exception {
        // given: 여러 개의 댓글 생성
        for (int i = 1; i <= 3; i++) {
            TradeComment comment = new TradeComment(
                testMember,
                testTrade,
                "테스트 댓글 " + i
            );
            tradeCommentRepository.save(comment);
        }

        // when & then
        ResultActions result = mvc.perform(
            get("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].comment").value("테스트 댓글 1"))
            .andExpect(jsonPath("$.data[1].comment").value("테스트 댓글 2"))
            .andExpect(jsonPath("$.data[2].comment").value("테스트 댓글 3"));
    }

    @Test
    @DisplayName("t6: 댓글 목록 조회 성공 - 댓글이 없는 경우")
    void t6() throws Exception {
        // given: 댓글이 없는 상태

        // when & then
        ResultActions result = mvc.perform(
            get("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("t7: 댓글 목록 조회 성공 - 다른 사용자의 댓글 포함")
    void t7() throws Exception {
        // given: 테스트 사용자의 댓글 생성
        TradeComment comment1 = new TradeComment(
            testMember,
            testTrade,
            "내 댓글"
        );
        tradeCommentRepository.save(comment1);

        // 다른 사용자 생성 및 댓글 작성
        Member otherMember = new Member(
            "other@test.com",
            "test1234",
            "다른사용자",
            "https://example.com/img2.jpg"
        );
        Member savedOtherMember = memberRepository.save(otherMember);
        TradeComment comment2 = new TradeComment(
            savedOtherMember,
            testTrade,
            "다른 사람 댓글"
        );
        tradeCommentRepository.save(comment2);

        // when & then
        ResultActions result = mvc.perform(
            get("/api/market/secondhand/" + testTrade.getTradeId() + "/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ========== UPDATE 테스트 ==========
    @Test
    @DisplayName("t8: 댓글 수정 성공")
    void t8() throws Exception {
        // given: 댓글 생성
        TradeComment comment = new TradeComment(
            testMember,
            testTrade,
            "원래 댓글 내용"
        );
        TradeComment savedComment = tradeCommentRepository.save(comment);

        // when: 댓글 수정
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": "수정된 댓글 내용"
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            put("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + savedComment.getCommentId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 수정 성공"))
            .andExpect(jsonPath("$.data.commentId").value(savedComment.getCommentId()))
            .andExpect(jsonPath("$.data.comment").value("수정된 댓글 내용"));
    }

    @Test
    @DisplayName("t9: 댓글 수정 실패 - 다른 사용자의 댓글")
    void t9() throws Exception {
        // given: 다른 사용자의 댓글 생성
        Member otherMember = new Member(
            "other@test.com",
            "test1234",
            "다른사용자",
            "https://example.com/img2.jpg"
        );
        Member savedOtherMember = memberRepository.save(otherMember);
        TradeComment comment = new TradeComment(
            savedOtherMember,
            testTrade,
            "다른 사람의 댓글"
        );
        TradeComment savedComment = tradeCommentRepository.save(comment);

        // when: 내 토큰으로 다른 사람의 댓글 수정 시도
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": "수정 시도"
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            put("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + savedComment.getCommentId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // then: 권한 없음 에러
        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t10: 댓글 수정 실패 - 존재하지 않는 댓글 ID")
    void t10() throws Exception {
        // given: 존재하지 않는 댓글 ID
        Long nonExistentId = 99999L;

        // when: 존재하지 않는 댓글 수정 시도
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": "수정 시도"
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            put("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + nonExistentId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // then: 에러
        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t11: 댓글 수정 실패 - content가 빈 문자열")
    void t11() throws Exception {
        // given: 댓글 생성
        TradeComment comment = new TradeComment(
            testMember,
            testTrade,
            "원래 댓글 내용"
        );
        TradeComment savedComment = tradeCommentRepository.save(comment);

        // when: 빈 내용으로 수정 시도
        String requestBody = String.format("""
            {
                "memberId": %d,
                "tradeId": %d,
                "content": ""
            }
            """, testMember.getMemberId(), testTrade.getTradeId());

        ResultActions result = mvc.perform(
            put("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + savedComment.getCommentId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // then: 유효성 검증 에러
        result
            .andExpect(status().is4xxClientError());
    }

    // ========== DELETE 테스트 ==========
    @Test
    @DisplayName("t12: 댓글 삭제 성공")
    void t12() throws Exception {
        // given: 댓글 생성
        TradeComment comment = new TradeComment(
            testMember,
            testTrade,
            "삭제할 댓글"
        );
        TradeComment savedComment = tradeCommentRepository.save(comment);

        // when: 댓글 삭제
        ResultActions result = mvc.perform(
            delete("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + savedComment.getCommentId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 삭제 성공"));
    }

    @Test
    @DisplayName("t13: 댓글 삭제 실패 - 다른 사용자의 댓글")
    void t13() throws Exception {
        // given: 다른 사용자의 댓글 생성
        Member otherMember = new Member(
            "other2@test.com",
            "test1234",
            "다른사용자2",
            "https://example.com/img3.jpg"
        );
        Member savedOtherMember = memberRepository.save(otherMember);
        TradeComment comment = new TradeComment(
            savedOtherMember,
            testTrade,
            "다른 사람의 댓글"
        );
        TradeComment savedComment = tradeCommentRepository.save(comment);

        // when: 내 토큰으로 다른 사람의 댓글 삭제 시도
        ResultActions result = mvc.perform(
            delete("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + savedComment.getCommentId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then: 권한 없음 에러
        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t14: 댓글 삭제 실패 - 존재하지 않는 댓글 ID")
    void t14() throws Exception {
        // given: 존재하지 않는 댓글 ID
        Long nonExistentId = 99999L;

        // when: 존재하지 않는 댓글 삭제 시도
        ResultActions result = mvc.perform(
            delete("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + nonExistentId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then: 에러
        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t15: 댓글 삭제 실패 - 인증 토큰 없음")
    void t15() throws Exception {
        // given: 댓글 생성
        TradeComment comment = new TradeComment(
            testMember,
            testTrade,
            "삭제할 댓글"
        );
        TradeComment savedComment = tradeCommentRepository.save(comment);

        // when: Authorization 헤더 없이 삭제 시도
        ResultActions result = mvc.perform(
            delete("/api/market/secondhand/" + testTrade.getTradeId() + "/comments/" + savedComment.getCommentId())
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then: 인증 에러
        result
            .andExpect(status().is4xxClientError());
    }
}

