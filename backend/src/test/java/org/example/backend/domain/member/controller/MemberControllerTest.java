package org.example.backend.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test") // 테스트 환경 설정 파일 사용 (예: application-test.yml)
@AutoConfigureMockMvc // MockMvc 자동 구성
@Transactional // 테스트 후 롤백
public class MemberControllerTest {

    @Autowired
    private MockMvc mvc; // HTTP 요청 시뮬레이션을 위한 MockMvc
    private static final String API_PATH = "/api/members/join";

    @Test
    @DisplayName("t1: 회원 가입 성공 후, 동일 이메일로 중복 가입 시도 시 실패")
    void t1_joinSuccessAndThenFailOnDuplicate() throws Exception {

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

        // 1. 성공 케이스 (정상 회원가입)

        System.out.println("--- 1차 시도: 정상 가입 ---");
        ResultActions firstAttempt = mvc
            .perform(
                post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(print());

        firstAttempt
            // 성공 검증
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("회원가입이 완료되었습니다."))
            .andExpect(jsonPath("$.data.memberId").isNumber());

        // 2. 실패 케이스 (동일 이메일로 중복 가입 시도)

        System.out.println("--- 2차 시도: 중복 가입 시도 ---");
        ResultActions secondAttempt = mvc
            .perform(
                post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody) // 💡 동일한 이메일을 사용
            )
            .andDo(print());

        secondAttempt
            // 실패 검증
            // 2. HTTP 상태 코드 검증: 409 Conflict (자원 충돌)
            .andExpect(status().isConflict())

            // 3. RsData 상태 코드 및 메시지 검증
            .andExpect(jsonPath("$.resultCode").value("409"))
            .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."))
            .andExpect(jsonPath("$.data").doesNotExist());
    }
}