package org.example.backend.domain.member.controller

import jakarta.servlet.http.Cookie
import org.example.backend.config.TestContainerConfig
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("t1: 회원 가입 성공 후, 동일 이메일로 중복 가입 시도 시 실패")
    fun t1() {
        // 테스트 데이터 준비
        val email = "sequential@test.com"
        val password = "securepassword"
        val nickname = "순차테스터"

        val requestBody = """
            {
                "email": "$email",
                "password": "$password",
                "nickname": "$nickname",
                "profileImage": null
            }
        """.trimIndent()

        // 첫 번째 회원가입 시도
        val firstAttempt = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/members/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(MockMvcResultHandlers.print())

        firstAttempt
            .andExpect(MockMvcResultMatchers.handler().handlerType(MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원가입이 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberId").isNumber())

        // 동일 이메일로 중복 가입 시도
        val secondAttempt = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/members/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(MockMvcResultHandlers.print())

        secondAttempt
            .andExpect(MockMvcResultMatchers.status().isConflict())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("M002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("이미 사용 중인 이메일입니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
    }

    @Test
    @DisplayName("t2: 로그인 성공")
    fun t2() {
        // 테스트 데이터 준비
        val email = "login@test.com"
        val password = "password123"
        val nickname = "loginuser"

        // 회원가입
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password",
                        "nickname": "$nickname",
                        "profileImage": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // 로그인
        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.handler().handlerType(MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인에 성공했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value(nickname))
            .andReturn()

        // accessToken과 refreshToken 쿠키 확인
        val accessTokenCookie = result.response.getCookie("accessToken")
        val refreshTokenCookie = result.response.getCookie("refreshToken")
        requireNotNull(accessTokenCookie) { "accessToken 쿠키가 존재해야 합니다." }
        requireNotNull(refreshTokenCookie) { "refreshToken 쿠키가 존재해야 합니다." }
    }

    @Test
    @DisplayName("t3: 로그인 실패 - 존재하지 않는 이메일")
    fun t3() {
        // 존재하지 않는 이메일로 로그인 시도
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "notfound@test.com",
                        "password": "password123"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("M001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 회원입니다."))
    }

    @Test
    @DisplayName("t4: 로그인 실패 - 비밀번호 불일치")
    fun t4() {
        // 테스트 데이터 준비
        val email = "wrongpass@test.com"
        val password = "password123"

        // 회원가입
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password",
                        "nickname": "wrongpass",
                        "profileImage": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // 잘못된 비밀번호로 로그인 시도
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "wrongpassword"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("M004"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("현재 비밀번호가 일치하지 않습니다."))
    }

    @Test
    @DisplayName("t5: 회원정보 수정 성공")
    fun t5() {
        // 테스트 데이터 준비
        val email = "edit@test.com"
        val password = "password123"
        val jwtToken = createMemberAndGetToken(email, password, "edituser")

        // 회원정보 수정
        mvc.perform(
            MockMvcRequestBuilders.put("/api/members/me")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "edited@test.com",
                        "currentPassword": "$password",
                        "newPassword": "newpassword123",
                        "nickname": "editeduser",
                        "profileImageUrl": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.handler().handlerType(MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("edit"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원정보 수정에 성공했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("edited@test.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value("editeduser"))
    }

    @Test
    @DisplayName("t6: 내 정보 조회 성공")
    fun t6() {
        // 테스트 데이터 준비
        val email = "mypage@test.com"
        val password = "password123"
        val nickname = "mypageuser"
        val jwtToken = createMemberAndGetToken(email, password, nickname)

        // 내 정보 조회
        mvc.perform(
            MockMvcRequestBuilders.get("/api/members/me")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.handler().handlerType(MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("myPage"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원 정보 조회에 성공했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value(nickname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.followerCount").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.followingCount").exists())
    }

    @Test
    @DisplayName("t7: 회원 검색 성공")
    fun t7() {
        // 테스트 데이터 준비
        val email1 = "search1@test.com"
        val email2 = "search2@test.com"
        val currentEmail = "current@test.com"

        // 검색 대상 회원들 가입
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email1",
                        "password": "password123",
                        "nickname": "searchuser1",
                        "profileImage": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email2",
                        "password": "password123",
                        "nickname": "searchuser2",
                        "profileImage": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        val jwtToken = createMemberAndGetToken(currentEmail, "password123", "currentuser")

        // 회원 검색
        mvc.perform(
            MockMvcRequestBuilders.get("/api/members/search")
                .header("Authorization", "Bearer $jwtToken")
                .param("nickname", "searchuser")
        )
            .andExpect(MockMvcResultMatchers.handler().handlerType(MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("searchMembers"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원 검색에 성공했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.members").isArray())
    }

    @Test
    @DisplayName("t8: 로그아웃 성공")
    fun t8() {
        // 테스트 데이터 준비
        val email = "logout@test.com"
        val password = "password123"
        val jwtToken = createMemberAndGetToken(email, password, "logoutuser")

        // 로그아웃
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/logout")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(MockMvcResultMatchers.handler().handlerType(MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("logout"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그아웃에 성공했습니다."))
    }

    @Test
    @DisplayName("t9: 리프레시 토큰으로 토큰 갱신 성공")
    fun t9() {
        // 테스트 데이터 준비
        val email = "refresh@test.com"
        val password = "password123"
        val nickname = "refreshuser"

        // 회원가입
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password",
                        "nickname": "$nickname",
                        "profileImage": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // 로그인
        val loginResult = mvc.perform(
            MockMvcRequestBuilders.post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()

        val refreshTokenCookie = requireNotNull(loginResult.response.getCookie("refreshToken")) {
            "refreshToken 쿠키가 존재해야 합니다."
        }
        val refreshToken = refreshTokenCookie.value

        // 토큰 갱신 (쿠키로 리프레시 토큰 전송)
        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/members/refresh")
                .cookie(refreshTokenCookie)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("토큰이 갱신되었습니다."))
            .andReturn()

        // 새로운 토큰들이 쿠키에 설정되었는지 확인
        val newAccessTokenCookie = result.response.getCookie("accessToken")
        val newRefreshTokenCookie = result.response.getCookie("refreshToken")
        requireNotNull(newAccessTokenCookie) { "새로운 accessToken 쿠키가 존재해야 합니다." }
        requireNotNull(newRefreshTokenCookie) { "새로운 refreshToken 쿠키가 존재해야 합니다." }

        // 새로운 refreshToken이 기존과 다른지 확인
        val newRefreshToken = newRefreshTokenCookie.value
        require(refreshToken != newRefreshToken) { "새로운 refreshToken은 기존과 달라야 합니다." }
    }

    @Test
    @DisplayName("t10: 유효하지 않은 리프레시 토큰으로 갱신 시도 시 실패")
    fun t10() {
        // 유효하지 않은 리프레시 토큰 쿠키로 갱신 시도
        val invalidCookie = Cookie("refreshToken", "invalid-refresh-token")
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/refresh")
                .cookie(invalidCookie)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("M006"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("유효하지 않은 리프레시 토큰입니다."))
    }

    @Test
    @DisplayName("t11: 로그아웃 후 리프레시 토큰으로 갱신 시도 시 실패")
    fun t11() {
        // 테스트 데이터 준비
        val email = "logoutrefresh@test.com"
        val password = "password123"
        val jwtToken = createMemberAndGetToken(email, password, "logoutrefreshuser")
        val loginResult = mvc.perform(
            MockMvcRequestBuilders.post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()

        val refreshTokenCookie = requireNotNull(loginResult.response.getCookie("refreshToken")) {
            "refreshToken 쿠키가 존재해야 합니다."
        }

        // 로그아웃 (refreshToken 쿠키 포함)
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/logout")
                .header("Authorization", "Bearer $jwtToken")
                .cookie(refreshTokenCookie)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // 로그아웃 후 리프레시 토큰으로 갱신 시도 (실패해야 함 - 쿠키로 전송)
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/refresh")
                .cookie(refreshTokenCookie)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("M006"))
    }

    //회원가입 및 로그인을 수행하고 JWT 토큰을 반환하는 헬퍼 메서드
    private fun createMemberAndGetToken(
        email: String,
        password: String,
        nickname: String
    ): String {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password",
                        "nickname": "$nickname",
                        "profileImage": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        val loginResult = mvc.perform(
            MockMvcRequestBuilders.post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()

        val accessTokenCookie = loginResult.response.getCookie("accessToken")
        return requireNotNull(accessTokenCookie?.value) { "accessToken 쿠키가 존재해야 합니다." }
    }

}
