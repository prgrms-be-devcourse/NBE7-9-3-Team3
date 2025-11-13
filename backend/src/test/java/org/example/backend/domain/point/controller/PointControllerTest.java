package org.example.backend.domain.point.controller;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.point.repository.PointRepository;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.global.LoginUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    private Member testMember;
    private String jwtToken;

    @BeforeAll
    void Login() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        jwtToken = loginUtil.createMemberAndGetToken(
                "point@test.com",
                "test1234",
                "point",
                ""
        );
        testMember = loginUtil.getMemberByEmail("point@test.com");
    }

    @Test
    @DisplayName("t1: 포인트 충전")
    void chargePoint() throws Exception {
        mvc.perform(post("/api/points/members/charge/{amount}", 5000)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("포인트 충전 완료"));
    }
}



