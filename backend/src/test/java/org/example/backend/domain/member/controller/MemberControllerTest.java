package org.example.backend.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("t1: 회원 가입 성공 후, 동일 이메일로 중복 가입 시도 시 실패")
    void t1() throws Exception {
        // 테스트 데이터 준비
        String email = "sequential@test.com";
        String password = "securepassword";
        String nickname = "순차테스터";

        String requestBody = """
            {
                "email": "%s",
                "password": "%s",
                "nickname": "%s",
                "profileImage": null
            }
            """.formatted(email, password, nickname);

        // 첫 번째 회원가입 시도
        ResultActions firstAttempt = mvc
            .perform(
                post("/api/members/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(print());

        firstAttempt
            .andExpect(handler().handlerType(MemberController.class))
            .andExpect(handler().methodName("join"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("회원가입이 완료되었습니다."))
            .andExpect(jsonPath("$.data.memberId").isNumber());

        // 동일 이메일로 중복 가입 시도
        ResultActions secondAttempt = mvc
            .perform(
                post("/api/members/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(print());

        secondAttempt
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("M002"))
            .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("t2: 로그인 성공")
    void t2() throws Exception {
        // 테스트 데이터 준비
        String email = "login@test.com";
        String password = "password123";
        String nickname = "loginuser";

        // 회원가입
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

        // 로그인
        MvcResult result = mvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(email, password)))
            .andExpect(handler().handlerType(MemberController.class))
            .andExpect(handler().methodName("login"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("로그인에 성공했습니다."))
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.nickname").value(nickname))
            .andReturn();

        // accessToken 쿠키 확인
        assert result.getResponse().getCookie("accessToken") != null;
    }

    @Test
    @DisplayName("t3: 로그인 실패 - 존재하지 않는 이메일")
    void t3() throws Exception {
        // 존재하지 않는 이메일로 로그인 시도
        mvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "notfound@test.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("M001"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 회원입니다."));
    }

    @Test
    @DisplayName("t4: 로그인 실패 - 비밀번호 불일치")
    void t4() throws Exception {
        // 테스트 데이터 준비
        String email = "wrongpass@test.com";
        String password = "password123";

        // 회원가입
        mvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "%s",
                        "nickname": "wrongpass",
                        "profileImage": null
                    }
                    """.formatted(email, password)))
            .andExpect(status().isOk());

        // 잘못된 비밀번호로 로그인 시도
        mvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "wrongpassword"
                    }
                    """.formatted(email)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("M004"))
            .andExpect(jsonPath("$.msg").value("현재 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("t5: 회원정보 수정 성공")
    void t5() throws Exception {
        // 테스트 데이터 준비
        String email = "edit@test.com";
        String password = "password123";
        String jwtToken = createMemberAndGetToken(email, password, "edituser");

        // 회원정보 수정
        mvc.perform(put("/api/members/me")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "edited@test.com",
                        "currentPassword": "%s",
                        "newPassword": "newpassword123",
                        "nickname": "editeduser",
                        "profileImageUrl": null
                    }
                    """.formatted(password)))
            .andExpect(handler().handlerType(MemberController.class))
            .andExpect(handler().methodName("edit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("회원정보 수정에 성공했습니다."))
            .andExpect(jsonPath("$.data.email").value("edited@test.com"))
            .andExpect(jsonPath("$.data.nickname").value("editeduser"));
    }

    @Test
    @DisplayName("t6: 내 정보 조회 성공")
    void t6() throws Exception {
        // 테스트 데이터 준비
        String email = "mypage@test.com";
        String password = "password123";
        String nickname = "mypageuser";
        String jwtToken = createMemberAndGetToken(email, password, nickname);

        // 내 정보 조회
        mvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(handler().handlerType(MemberController.class))
            .andExpect(handler().methodName("myPage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("회원 정보 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.nickname").value(nickname))
            .andExpect(jsonPath("$.data.followerCount").exists())
            .andExpect(jsonPath("$.data.followingCount").exists());
    }

    @Test
    @DisplayName("t7: 회원 검색 성공")
    void t7() throws Exception {
        // 테스트 데이터 준비
        String email1 = "search1@test.com";
        String email2 = "search2@test.com";
        String currentEmail = "current@test.com";
        
        // 검색 대상 회원들 가입
        mvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "password123",
                        "nickname": "searchuser1",
                        "profileImage": null
                    }
                    """.formatted(email1)))
            .andExpect(status().isOk());

        mvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "password123",
                        "nickname": "searchuser2",
                        "profileImage": null
                    }
                    """.formatted(email2)))
            .andExpect(status().isOk());

        String jwtToken = createMemberAndGetToken(currentEmail, "password123", "currentuser");

        // 회원 검색
        mvc.perform(get("/api/members/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("nickname", "searchuser"))
            .andExpect(handler().handlerType(MemberController.class))
            .andExpect(handler().methodName("searchMembers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("회원 검색에 성공했습니다."))
            .andExpect(jsonPath("$.data.members").isArray());
    }

    @Test
    @DisplayName("t8: 로그아웃 성공")
    void t8() throws Exception {
        // 테스트 데이터 준비
        String email = "logout@test.com";
        String password = "password123";
        String jwtToken = createMemberAndGetToken(email, password, "logoutuser");

        // 로그아웃
        mvc.perform(post("/api/members/logout")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(handler().handlerType(MemberController.class))
            .andExpect(handler().methodName("logout"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("로그아웃에 성공했습니다."));
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
}
