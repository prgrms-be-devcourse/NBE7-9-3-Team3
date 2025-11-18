package org.example.backend.domain.follow.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.LoginUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class FollowControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthTokenService authTokenService;

    private LoginUtil loginUtil;

    @BeforeEach
    void setUp() {
        loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
    }

    @Test
    @DisplayName("t1: 팔로우 성공")
    void t1() throws Exception {
        String followerEmail = "follower@test.com";
        String followeeEmail = "followee@test.com";

        Member follower = loginUtil.createMember(followerEmail, "password123", "follower", "");
        Member followee = loginUtil.createMember(followeeEmail, "password123", "followee", "");
        String followerToken = authTokenService.genAccessToken(follower);

        Long followeeId = followee.getMemberId();

        mvc.perform(post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("팔로우가 완료되었습니다."))
            .andExpect(jsonPath("$.data.memberId").value(followeeId));
    }

    @Test
    @DisplayName("t2: 팔로우 실패 - 자기 자신 팔로우")
    void t2() throws Exception {
        String email = "self@test.com";
        Member member = loginUtil.createMember(email, "password123", "selfuser", "");
        String token = authTokenService.genAccessToken(member);
        Long memberId = member.getMemberId();

        mvc.perform(post("/api/follows/{followeeId}", memberId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("F001"))
            .andExpect(jsonPath("$.msg").value("자기 자신을 팔로우할 수 없습니다."));
    }

    @Test
    @DisplayName("t3: 팔로우 실패 - 이미 팔로우 중")
    void t3() throws Exception {
        String followerEmail = "follower2@test.com";
        String followeeEmail = "followee2@test.com";

        Member follower = loginUtil.createMember(followerEmail, "password123", "follower2", "");
        Member followee = loginUtil.createMember(followeeEmail, "password123", "followee2", "");
        String followerToken = authTokenService.genAccessToken(follower);

        Long followeeId = followee.getMemberId();

        mvc.perform(post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk());

        mvc.perform(post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("F002"))
            .andExpect(jsonPath("$.msg").value("이미 팔로우하고 있습니다."));
    }

    @Test
    @DisplayName("t4: 언팔로우 성공")
    void t4() throws Exception {
        String followerEmail = "unfollow1@test.com";
        String followeeEmail = "unfollow2@test.com";

        Member follower = loginUtil.createMember(followerEmail, "password123", "unfollow1", "");
        Member followee = loginUtil.createMember(followeeEmail, "password123", "unfollow2", "");
        String followerToken = authTokenService.genAccessToken(follower);

        Long followeeId = followee.getMemberId();

        mvc.perform(post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk());

        mvc.perform(delete("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("언팔로우가 완료되었습니다."));
    }

    @Test
    @DisplayName("t5: 팔로워 목록 조회 성공")
    void t5() throws Exception {
        String followeeEmail = "followee3@test.com";
        String follower1Email = "follower3@test.com";
        String follower2Email = "follower4@test.com";

        Member followee = loginUtil.createMember(followeeEmail, "password123", "followee3", "");
        Member follower1 = loginUtil.createMember(follower1Email, "password123", "follower3", "");
        Member follower2 = loginUtil.createMember(follower2Email, "password123", "follower4", "");
        String followeeToken = authTokenService.genAccessToken(followee);
        String follower1Token = authTokenService.genAccessToken(follower1);
        String follower2Token = authTokenService.genAccessToken(follower2);

        Long followeeId = followee.getMemberId();

        mvc.perform(post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + follower1Token))
            .andExpect(status().isOk());

        mvc.perform(post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer " + follower2Token))
            .andExpect(status().isOk());

        mvc.perform(get("/api/follows/{memberId}/followers", followeeId)
                .header("Authorization", "Bearer " + followeeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("팔로워 목록을 조회했습니다."))
            .andExpect(jsonPath("$.data.totalCount").value(2))
            .andExpect(jsonPath("$.data.users").isArray())
            .andExpect(jsonPath("$.data.users.length()").value(2));
    }

    @Test
    @DisplayName("t6: 팔로잉 목록 조회 성공")
    void t6() throws Exception {
        String followerEmail = "follower5@test.com";
        String followee1Email = "followee4@test.com";
        String followee2Email = "followee5@test.com";

        Member follower = loginUtil.createMember(followerEmail, "password123", "follower5", "");
        Member followee1 = loginUtil.createMember(followee1Email, "password123", "followee4", "");
        Member followee2 = loginUtil.createMember(followee2Email, "password123", "followee5", "");
        String followerToken = authTokenService.genAccessToken(follower);

        Long followee1Id = followee1.getMemberId();
        Long followee2Id = followee2.getMemberId();

        mvc.perform(post("/api/follows/{followeeId}", followee1Id)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk());

        mvc.perform(post("/api/follows/{followeeId}", followee2Id)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk());

        Long followerId = follower.getMemberId();
        mvc.perform(get("/api/follows/{memberId}/followings", followerId)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("팔로잉 목록을 조회했습니다."))
            .andExpect(jsonPath("$.data.totalCount").value(2))
            .andExpect(jsonPath("$.data.users").isArray())
            .andExpect(jsonPath("$.data.users.length()").value(2));
    }

    @Test
    @DisplayName("t7: 팔로우 실패 - 존재하지 않는 회원")
    void t7() throws Exception {
        String email = "notfound@test.com";
        Member member = loginUtil.createMember(email, "password123", "notfound", "");
        String token = authTokenService.genAccessToken(member);

        mvc.perform(post("/api/follows/{followeeId}", 99999L)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("F004"));
    }
}

