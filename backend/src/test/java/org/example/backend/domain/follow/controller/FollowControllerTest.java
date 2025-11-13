package org.example.backend.domain.follow.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class FollowControllerTest {

    @Autowired
    private MockMvc mvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("t1: 팔로우 성공")
    void t1() throws Exception {
        String followerEmail = "follower@test.com";
        String followeeEmail = "followee@test.com";
        
        String followerToken = createMemberAndGetToken(followerEmail, "password123", "follower");
        String followeeToken = createMemberAndGetToken(followeeEmail, "password123", "followee");
        
        Long followeeId = getMemberIdFromToken(followeeToken);

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
        String token = createMemberAndGetToken(email, "password123", "selfuser");
        Long memberId = getMemberIdFromToken(token);

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
        
        String followerToken = createMemberAndGetToken(followerEmail, "password123", "follower2");
        String followeeToken = createMemberAndGetToken(followeeEmail, "password123", "followee2");
        
        Long followeeId = getMemberIdFromToken(followeeToken);

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
        
        String followerToken = createMemberAndGetToken(followerEmail, "password123", "unfollow1");
        String followeeToken = createMemberAndGetToken(followeeEmail, "password123", "unfollow2");
        
        Long followeeId = getMemberIdFromToken(followeeToken);

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
        
        String followeeToken = createMemberAndGetToken(followeeEmail, "password123", "followee3");
        String follower1Token = createMemberAndGetToken(follower1Email, "password123", "follower3");
        String follower2Token = createMemberAndGetToken(follower2Email, "password123", "follower4");
        
        Long followeeId = getMemberIdFromToken(followeeToken);

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
        
        String followerToken = createMemberAndGetToken(followerEmail, "password123", "follower5");
        String followee1Token = createMemberAndGetToken(followee1Email, "password123", "followee4");
        String followee2Token = createMemberAndGetToken(followee2Email, "password123", "followee5");
        
        Long followee1Id = getMemberIdFromToken(followee1Token);
        Long followee2Id = getMemberIdFromToken(followee2Token);

        mvc.perform(post("/api/follows/{followeeId}", followee1Id)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk());

        mvc.perform(post("/api/follows/{followeeId}", followee2Id)
                .header("Authorization", "Bearer " + followerToken))
            .andExpect(status().isOk());

        Long followerId = getMemberIdFromToken(followerToken);
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
        String token = createMemberAndGetToken(email, "password123", "notfound");

        mvc.perform(post("/api/follows/{followeeId}", 99999L)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("F004"));
    }

    /**
     * 회원가입 및 로그인을 수행하고 JWT 토큰을 반환하는 헬퍼 메서드
     */
    private String createMemberAndGetToken(String email, String password, String nickname) throws Exception {
        mvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "%s",
                        "nickname": "%s",
                        "profileImage": null
                    }
                    """.formatted(email, password, nickname)))
            .andExpect(status().isOk());

        MvcResult loginResult = mvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();

        return loginResult.getResponse().getCookie("accessToken").getValue();
    }

    private Long getMemberIdFromToken(String token) throws Exception {
        MvcResult result = mvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("data").get("memberId").asLong();
    }
}

