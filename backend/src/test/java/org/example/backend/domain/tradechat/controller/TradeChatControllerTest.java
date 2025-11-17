package org.example.backend.domain.tradechat.controller;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.domain.tradechat.entity.ChatStatus;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;
import org.example.backend.domain.tradechat.repository.TradeChatRoomRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TradeChatControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private TradeChatRoomRepository tradeChatRoomRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthTokenService authTokenService;

    private LoginUtil loginUtil;
    private PointUtil pointUtil;

    private Member testMember;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        pointUtil = new PointUtil(tradeRepository, memberRepository, passwordEncoder);

        jwtToken = loginUtil.createMemberAndGetToken(
                "point@test.com",
                "test1234",
                "point",
                ""
        );
        testMember = loginUtil.getMemberByEmail("point@test.com");
    }

    @Test
    @DisplayName("t1: 채팅방 생성 성공")
    void t1() throws Exception {
        Member seller = pointUtil.createSeller();
        Trade trade = pointUtil.createTrade(seller, 5000L);

        mvc.perform(post("/api/chat/{tradeId}/room", trade.getTradeId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("채팅방이 생성되었습니다."));
    }

    @Test
    @DisplayName("t2: 채팅방 상세 조회 성공")
    void t2() throws Exception {
        Member seller = pointUtil.createSeller();
        Member buyer = testMember;
        Trade trade = pointUtil.createTrade(seller, 5000L);

        // 채팅방 생성
        TradeChatRoom room = tradeChatRoomRepository.save(
                TradeChatRoom.builder()
                        .trade(trade)
                        .sellerId(seller)
                        .buyerId(buyer)
                        .status(ChatStatus.ONGOING)
                        .build()
        );
        Long roomId = room.getId();

        mvc.perform(get("/api/chat/rooms/{roomId}", roomId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("채팅방 거래정보를 조회했습니다."))
                .andExpect(jsonPath("$.data.roomId").value(roomId))
                .andExpect(jsonPath("$.data.tradeId").value(trade.getTradeId()))
                .andExpect(jsonPath("$.data.sellerId").value(seller.getMemberId()))
                .andExpect(jsonPath("$.data.buyerId").value(buyer.getMemberId()));
    }

    @Test
    @DisplayName("t3: 채팅방 상세 조회 실패 - 제 3자 접근")
    void t3() throws Exception {
        Member seller = pointUtil.createSeller();
        Member buyer = testMember;

        // 제 3자 생성(관계없는)
        String otherToken = loginUtil.createMemberAndGetToken(
                "other@test.com",
                "other1234",
                "other",
                ""
        );
        Member other = loginUtil.getMemberByEmail("other@test.com");

        Trade trade = pointUtil.createTrade(seller, 5000L);
        TradeChatRoom room = tradeChatRoomRepository.save(
                TradeChatRoom.builder()
                        .trade(trade)
                        .sellerId(seller)
                        .buyerId(buyer)
                        .status(ChatStatus.ONGOING)
                        .build()
        );
        Long roomId = room.getId();

        mvc.perform(get("/api/chat/rooms/{roomId}", roomId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("CMN007"))
                .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("t4: 채팅방 내 메세지 내역 조회 성공")
    void t4() throws Exception {
        Member seller = pointUtil.createSeller();
        Member buyer = testMember;
        Trade trade = pointUtil.createTrade(seller, 5000L);

        // 채팅방 생성
        TradeChatRoom room = tradeChatRoomRepository.save(
                TradeChatRoom.builder()
                        .trade(trade)
                        .sellerId(seller)
                        .buyerId(buyer)
                        .status(ChatStatus.ONGOING)
                        .build()
        );
        Long roomId = room.getId();

        mvc.perform(get("/api/chat/rooms/messages/{roomId}", roomId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("채팅 내역을 조회했습니다."))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("t5: 채팅방 내 메세지 내역 조회 실패 - 제 3자 접근")
    void t5() throws Exception {
        Member seller = pointUtil.createSeller();
        Member buyer = testMember;

        // 제 3자 생성(관계없는)
        String otherToken = loginUtil.createMemberAndGetToken(
                "other@test.com",
                "other1234",
                "other",
                ""
        );
        Member other = loginUtil.getMemberByEmail("other@test.com");

        Trade trade = pointUtil.createTrade(seller, 5000L);
        TradeChatRoom room = tradeChatRoomRepository.save(
                TradeChatRoom.builder()
                        .trade(trade)
                        .sellerId(seller)
                        .buyerId(buyer)
                        .status(ChatStatus.ONGOING)
                        .build()
        );
        Long roomId = room.getId();

        mvc.perform(get("/api/chat/rooms/messages/{roomId}", roomId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("CMN007"))   // ErrorCode.FORBIDDEN_ACCESS
                .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("t6: 채팅방 목록 조회 성공")
    void t6() throws Exception {
        Member seller = pointUtil.createSeller();
        Member buyer = testMember;
        Trade trade = pointUtil.createTrade(seller, 5000L);

        // 채팅방 생성
        TradeChatRoom room = tradeChatRoomRepository.save(
                TradeChatRoom.builder()
                        .trade(trade)
                        .sellerId(seller)
                        .buyerId(buyer)
                        .status(ChatStatus.ONGOING)
                        .build()
        );

        mvc.perform(get("/api/chat/rooms/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.msg").value("채팅방 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data").isArray());
    }
}
