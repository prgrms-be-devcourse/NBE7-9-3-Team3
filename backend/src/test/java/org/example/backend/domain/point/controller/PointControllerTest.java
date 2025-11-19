package org.example.backend.domain.point.controller;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.point.entity.Point;
import org.example.backend.domain.point.repository.PointRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.global.LoginUtil;
import org.example.backend.global.PointUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PointControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthTokenService authTokenService;

    private PointUtil pointUtil;

    private Member testMember;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        pointUtil = new PointUtil(tradeRepository, memberRepository, passwordEncoder);

        jwtToken = loginUtil.createMemberAndGetToken(
                "point@test.com",
                passwordEncoder.encode("point1234"),
                "point",
                ""
        );
        testMember = loginUtil.getMemberByEmail("point@test.com");
    }

    @Test
    @DisplayName("t1: 포인트 충전 성공")
    void t1() throws Exception {
        mvc.perform(post("/api/points/members/charge/{amount}", 5000)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("포인트 충전 완료"));
    }

    @Test
    @DisplayName("t2: 포인트 내역 조회 성공")
    void t2() throws Exception {
        pointRepository.save(Point.Companion.create(testMember, 5000L, 5000L));

        mvc.perform(get("/api/points/members/history")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("포인트 조회 완료"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("CHARGE"))
                .andExpect(jsonPath("$.data[0].points").value(5000))
                .andExpect(jsonPath("$.data[0].afterPoint").value(5000))
                .andExpect(jsonPath("$.data[0].date").exists());
    }

    @Test
    @DisplayName("t3: 포인트 내역 조회 실패 - 포인트 기록 없음")
    void t3() throws Exception {
        mvc.perform(get("/api/points/members/history")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("P002"))
                .andExpect(jsonPath("$.msg").value("포인트 내역이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("t4: 포인트로 상품 결제 성공")
    void t4() throws Exception {
        Member seller = pointUtil.createSeller();
        Trade trade = pointUtil.createTrade(seller, 5000L);

        // 구매자 포인트 충전
        testMember.updatePoints(10000L);
        memberRepository.save(testMember);

        String requestBody = pointUtil.purchaseRequest(
                seller.getMemberId(),
                trade.getPrice(),
                trade.getTradeId()
        );

        mvc.perform(post("/api/points/members/purchase")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("포인트 결제 완료"));
    }

    @Test
    @DisplayName("t5: 포인트로 상품 결제 실패 - 이미 판매 완료된 상품")
    void t5() throws Exception {
        Member seller = pointUtil.createSeller();
        Trade trade = pointUtil.createTrade(seller, 5000L);

        // 상품을 판매완료로 상태변경
        trade.completeTransaction();
        tradeRepository.save(trade);

        testMember.updatePoints(10000L);
        memberRepository.save(testMember);

        String requestBody = pointUtil.purchaseRequest(
                seller.getMemberId(),
                trade.getPrice(),
                trade.getTradeId()
        );

        mvc.perform(post("/api/points/members/purchase")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("T005"))
                .andExpect(jsonPath("$.msg").value("해당 물품은 이미 판매되었습니다."));
    }

    @Test
    @DisplayName("t6: 포인트로 상품 결제 실패 - 포인트 부족")
    void t6() throws Exception {
        Member seller = pointUtil.createSeller();
        Trade trade = pointUtil.createTrade(seller, 5000L);

        // 구매자 포인트 상품보다 적게 충전
        testMember.updatePoints(3000L);
        memberRepository.save(testMember);

        String requestBody = pointUtil.purchaseRequest(
                seller.getMemberId(),
                trade.getPrice(),
                trade.getTradeId()
        );

        mvc.perform(post("/api/points/members/purchase")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("P003"))
                .andExpect(jsonPath("$.msg").value("포인트가 부족합니다."));
    }
}



