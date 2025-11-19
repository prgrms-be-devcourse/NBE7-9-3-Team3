package org.example.backend.domain.follow.controller

import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.LoginUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class FollowControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var loginUtil: LoginUtil

    @BeforeEach
    fun setUp() {
        loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
    }

    @Test
    @DisplayName("t1: 팔로우 성공")
    fun t1() {
        val followerEmail = "follower@test.com"
        val followeeEmail = "followee@test.com"

        val follower = loginUtil.createMember(followerEmail, "password123", "follower", "")
        val followee = loginUtil.createMember(followeeEmail, "password123", "followee", "")
        val followerToken = authTokenService.genAccessToken(follower)

        val followeeId = requireNotNull(followee.memberId) { "followee.memberId는 null일 수 없습니다." }

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("팔로우가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberId").value(followeeId))
    }

    @Test
    @DisplayName("t2: 팔로우 실패 - 자기 자신 팔로우")
    fun t2() {
        val email = "self@test.com"
        val member = loginUtil.createMember(email, "password123", "selfuser", "")
        val token = authTokenService.genAccessToken(member)
        val memberId = requireNotNull(member.memberId) { "member.memberId는 null일 수 없습니다." }

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", memberId)
                .header("Authorization", "Bearer $token")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("F001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("자기 자신을 팔로우할 수 없습니다."))
    }

    @Test
    @DisplayName("t3: 팔로우 실패 - 이미 팔로우 중")
    fun t3() {
        val followerEmail = "follower2@test.com"
        val followeeEmail = "followee2@test.com"

        val follower = loginUtil.createMember(followerEmail, "password123", "follower2", "")
        val followee = loginUtil.createMember(followeeEmail, "password123", "followee2", "")
        val followerToken = authTokenService.genAccessToken(follower)

        val followeeId = requireNotNull(followee.memberId) { "followee.memberId는 null일 수 없습니다." }

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("F002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("이미 팔로우하고 있습니다."))
    }

    @Test
    @DisplayName("t4: 언팔로우 성공")
    fun t4() {
        val followerEmail = "unfollow1@test.com"
        val followeeEmail = "unfollow2@test.com"

        val follower = loginUtil.createMember(followerEmail, "password123", "unfollow1", "")
        val followee = loginUtil.createMember(followeeEmail, "password123", "unfollow2", "")
        val followerToken = authTokenService.genAccessToken(follower)

        val followeeId = requireNotNull(followee.memberId) { "followee.memberId는 null일 수 없습니다." }

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("언팔로우가 완료되었습니다."))
    }

    @Test
    @DisplayName("t5: 팔로워 목록 조회 성공")
    fun t5() {
        val followeeEmail = "followee3@test.com"
        val follower1Email = "follower3@test.com"
        val follower2Email = "follower4@test.com"

        val followee = loginUtil.createMember(followeeEmail, "password123", "followee3", "")
        val follower1 = loginUtil.createMember(follower1Email, "password123", "follower3", "")
        val follower2 = loginUtil.createMember(follower2Email, "password123", "follower4", "")
        val followeeToken = authTokenService.genAccessToken(followee)
        val follower1Token = authTokenService.genAccessToken(follower1)
        val follower2Token = authTokenService.genAccessToken(follower2)

        val followeeId = requireNotNull(followee.memberId) { "followee.memberId는 null일 수 없습니다." }

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $follower1Token")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followeeId)
                .header("Authorization", "Bearer $follower2Token")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mvc.perform(
            MockMvcRequestBuilders.get("/api/follows/{memberId}/followers", followeeId)
                .header("Authorization", "Bearer $followeeToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("팔로워 목록을 조회했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalCount").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.users").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.users.length()").value(2))
    }

    @Test
    @DisplayName("t6: 팔로잉 목록 조회 성공")
    fun t6() {
        val followerEmail = "follower5@test.com"
        val followee1Email = "followee4@test.com"
        val followee2Email = "followee5@test.com"

        val follower = loginUtil.createMember(followerEmail, "password123", "follower5", "")
        val followee1 = loginUtil.createMember(followee1Email, "password123", "followee4", "")
        val followee2 = loginUtil.createMember(followee2Email, "password123", "followee5", "")
        val followerToken = authTokenService.genAccessToken(follower)

        val followee1Id = requireNotNull(followee1.memberId) { "followee1.memberId는 null일 수 없습니다." }
        val followee2Id = requireNotNull(followee2.memberId) { "followee2.memberId는 null일 수 없습니다." }

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followee1Id)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", followee2Id)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        val followerId = requireNotNull(follower.memberId) { "follower.memberId는 null일 수 없습니다." }
        mvc.perform(
            MockMvcRequestBuilders.get("/api/follows/{memberId}/followings", followerId)
                .header("Authorization", "Bearer $followerToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("팔로잉 목록을 조회했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalCount").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.users").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.users.length()").value(2))
    }

    @Test
    @DisplayName("t7: 팔로우 실패 - 존재하지 않는 회원")
    fun t7() {
        val email = "notfound@test.com"
        val member = loginUtil.createMember(email, "password123", "notfound", "")
        val token = authTokenService.genAccessToken(member)

        mvc.perform(
            MockMvcRequestBuilders.post("/api/follows/{followeeId}", 99999L)
                .header("Authorization", "Bearer $token")
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("F004"))
    }
}

