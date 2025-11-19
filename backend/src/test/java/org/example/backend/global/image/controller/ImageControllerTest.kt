package org.example.backend.global.image.controller

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.LoginUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URL

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ImageControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var s3Client: S3Client

    @MockitoBean
    private lateinit var s3Presigner: S3Presigner

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var loginUtil: LoginUtil
    private lateinit var accessToken: String
    private lateinit var testMember: Member

    @BeforeEach
    fun setUp() {
        loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        testMember = loginUtil.createMember(
            "test@test.com",
            "test1234",
            "테스트",
            "https://example.com/img1.jpg"
        )
        accessToken = authTokenService.genAccessToken(testMember)

        // S3Presigner Mock stubbing
        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()) doReturn URL("https://test-bucket.s3.amazonaws.com/presigned-url")
        whenever(s3Presigner.presignPutObject(ArgumentMatchers.any(PutObjectPresignRequest::class.java))) doReturn mockPresignedRequest
    }

    // ========== Presigned URL 발급 테스트 ==========
    @Test
    @DisplayName("t1: Presigned URL 발급 성공 - 거래 이미지")
    fun t1() {
        // given
        val requestBody = """
            {
                "fileName": "aquarium-tank.jpg",
                "directory": "trades"
            }
        """.trimIndent()

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("Presigned URL 생성 성공"))
            .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
            .andExpect(jsonPath("$.data.fileUrl").isNotEmpty())
    }

    @Test
    @DisplayName("t2: Presigned URL 발급 실패 - fileName 누락")
    fun t2() {
        // given: fileName 필드 없음
        val requestBody = """
            {
                "directory": "trades"
            }    
        """.trimIndent()

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t3: Presigned URL 발급 성공 - directory 누락 (null 허용)")
    fun t3() {
        // given: directory 필드 없음 (null로 처리됨)
        val requestBody = """
            {
                "fileName": "test-image.jpg"
            }
        """.trimIndent()

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("Presigned URL 생성 성공"))
    }

    @Test
    @DisplayName("t4: Presigned URL 발급 실패 - 빈 요청 본문")
    fun t4() {
        // given: 빈 요청 본문
        val requestBody = "{}"

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t5: Presigned URL 발급 실패 - 인증 토큰 없음")
    fun t5() {
        // given: 정상 요청 본문
        val requestBody = """
            {
                "fileName": "test-image.jpg",
                "directory": "trades"
            }
        """.trimIndent()

        // when & then: Authorization 헤더 없이 요청시 인증 에러
        mvc.perform(
            post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t6: Presigned URL 발급 실패 - 잘못된 토큰")
    fun t6() {
        // given: 정상 요청 본문
        val requestBody = """
            {
                "fileName": "test-image.jpg",
                "directory": "trades"
            }
        """.trimIndent()

        // when & then: 잘못된 토큰으로 요청시 인증 에러
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer invalid-token-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().is4xxClientError())
    }

    @Test
    @DisplayName("t7: Presigned URL 발급 성공 - 특수문자 포함 파일명")
    fun t7() {
        // given: 특수문자 포함 파일명
        val requestBody = """
            {
                "fileName": "수조_60cm-LED조명.jpg",
                "directory": "trades"
            }
        """.trimIndent()

        // when & then
        mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("Presigned URL 생성 성공"))
            .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
            .andExpect(jsonPath("$.data.fileUrl").isNotEmpty())
    }

    companion object {
        private const val API_PATH = "/api/images/upload"
    }
}
