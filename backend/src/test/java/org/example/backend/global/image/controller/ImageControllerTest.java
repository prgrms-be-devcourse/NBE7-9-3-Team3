package org.example.backend.global.image.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URL;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private S3Presigner s3Presigner;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenService authTokenService;

    private static final String API_PATH = "/api/images/upload";
    private LoginUtil loginUtil;
    private String accessToken;
    private Member testMember;

    @BeforeEach
    void setUp() throws Exception {
        loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember = loginUtil.createMember("test@test.com", "test1234", "테스트", "https://example.com/img1.jpg");
        accessToken = authTokenService.genAccessToken(testMember);

        // S3Presigner Mock stubbing
        PresignedPutObjectRequest mockPresignedRequest = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(new URL("https://test-bucket.s3.amazonaws.com/presigned-url"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);
    }

    // ========== Presigned URL 발급 테스트 ==========
    @Test
    @DisplayName("t1: Presigned URL 발급 성공 - 거래 이미지")
    void t1() throws Exception {
        // given
        String requestBody = """
            {
                "fileName": "aquarium-tank.jpg",
                "directory": "trades"
            }
            """;

        // when & then
        ResultActions result = mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("Presigned URL 생성 성공"))
            .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
            .andExpect(jsonPath("$.data.fileUrl").isNotEmpty());
    }

    @Test
    @DisplayName("t2: Presigned URL 발급 실패 - fileName 누락")
    void t2() throws Exception {
        // given: fileName 필드 없음
        String requestBody = """
            {
                "directory": "trades"
            }
            """;

        // when & then
        ResultActions result = mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // validation이 없으면 500 에러 발생
        result
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("t3: Presigned URL 발급 성공 - directory 누락 (null 허용)")
    void t3() throws Exception {
        // given: directory 필드 없음 (null로 처리됨)
        String requestBody = """
            {
                "fileName": "test-image.jpg"
            }
            """;

        // when & then
        ResultActions result = mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // directory가 null이어도 동작하므로 200 OK
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("Presigned URL 생성 성공"));
    }

    @Test
    @DisplayName("t4: Presigned URL 발급 실패 - 빈 요청 본문")
    void t4() throws Exception {
        // given: 빈 요청 본문
        String requestBody = "{}";

        // when & then
        ResultActions result = mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // validation이 없으면 500 에러 발생 가능
        result
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("t5: Presigned URL 발급 실패 - 인증 토큰 없음")
    void t5() throws Exception {
        // given: 정상 요청 본문
        String requestBody = """
            {
                "fileName": "test-image.jpg",
                "directory": "trades"
            }
            """;

        // when: Authorization 헤더 없이 요청
        ResultActions result = mvc.perform(
            post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // then: 인증 에러
        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t6: Presigned URL 발급 실패 - 잘못된 토큰")
    void t6() throws Exception {
        // given: 정상 요청 본문
        String requestBody = """
            {
                "fileName": "test-image.jpg",
                "directory": "trades"
            }
            """;

        // when: 잘못된 토큰으로 요청
        ResultActions result = mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer invalid-token-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        // then: 인증 에러
        result
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t7: Presigned URL 발급 성공 - 특수문자 포함 파일명")
    void t7() throws Exception {
        // given: 특수문자 포함 파일명
        String requestBody = """
            {
                "fileName": "수조_60cm-LED조명.jpg",
                "directory": "trades"
            }
            """;

        // when & then
        ResultActions result = mvc.perform(
            post(API_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("Presigned URL 생성 성공"))
            .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
            .andExpect(jsonPath("$.data.fileUrl").isNotEmpty());
    }

}
