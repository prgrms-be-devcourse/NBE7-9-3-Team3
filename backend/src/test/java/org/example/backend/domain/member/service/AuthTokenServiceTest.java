package org.example.backend.domain.member.service;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
    "custom.jwt.secretPattern=testSecretKey123456789012345678901234567890",
    "custom.jwt.expireSeconds=86400",
    "custom.jwt.shortExpireSeconds=600"
})
public class AuthTokenServiceTest {

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("t1: 액세스 토큰 생성 성공")
    void t1() {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("testuser")
                .profileImage(null)
                .build();
        Member savedMember = memberRepository.save(member);

        // when
        String token = authTokenService.genAccessToken(savedMember);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 토큰에서 payload 추출하여 검증
        Map<String, Object> payload = authTokenService.payloadOrNull(token);
        assertThat(payload).isNotNull();
        assertThat(payload.get("id")).isEqualTo(savedMember.getMemberId());
        assertThat(payload.get("email")).isEqualTo(savedMember.getEmail());
        assertThat(payload.get("nickname")).isEqualTo(savedMember.getNickname());
    }

    @Test
    @DisplayName("t2: 임시 토큰 생성 성공")
    void t2() {
        // given
        Member member = Member.builder()
                .email("temp@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("tempuser")
                .profileImage(null)
                .build();
        Member savedMember = memberRepository.save(member);

        // when
        String tempToken = authTokenService.genTempToken(savedMember);

        // then
        assertThat(tempToken).isNotNull();
        assertThat(tempToken).isNotEmpty();
        
        // 토큰에서 payload 추출하여 검증
        Map<String, Object> payload = authTokenService.payloadOrNull(tempToken);
        assertThat(payload).isNotNull();
        assertThat(payload.get("id")).isEqualTo(savedMember.getMemberId());
        assertThat(payload.get("email")).isEqualTo(savedMember.getEmail());
        assertThat(payload.get("nickname")).isEqualTo(savedMember.getNickname());
    }

    @Test
    @DisplayName("t3: 유효한 토큰에서 payload 추출 성공")
    void t3() {
        // given
        Member member = Member.builder()
                .email("payload@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("payloaduser")
                .profileImage(null)
                .build();
        Member savedMember = memberRepository.save(member);
        String token = authTokenService.genAccessToken(savedMember);

        // when
        Map<String, Object> payload = authTokenService.payloadOrNull(token);

        // then
        assertThat(payload).isNotNull();
        assertThat(payload.get("id")).isEqualTo(savedMember.getMemberId());
        assertThat(payload.get("email")).isEqualTo(savedMember.getEmail());
        assertThat(payload.get("nickname")).isEqualTo(savedMember.getNickname());
    }

    @Test
    @DisplayName("t4: 유효하지 않은 토큰에서 payload 추출 시 null 반환")
    void t4() {
        // given
        String invalidToken = "invalid.token.string";

        // when
        Map<String, Object> payload = authTokenService.payloadOrNull(invalidToken);

        // then
        assertThat(payload).isNull();
    }

    @Test
    @DisplayName("t5: 빈 문자열 토큰에서 payload 추출 시 null 반환")
    void t5() {
        // given
        String emptyToken = "";

        // when
        Map<String, Object> payload = authTokenService.payloadOrNull(emptyToken);

        // then
        assertThat(payload).isNull();
    }

    @Test
    @DisplayName("t6: 다른 시크릿 키로 생성된 토큰에서 payload 추출 시 null 반환")
    void t6() {
        // given
        // 다른 시크릿 키로 토큰을 생성하는 것은 JwtUtil을 직접 사용해야 하므로
        // 여기서는 간단히 잘못된 형식의 토큰을 테스트합니다.
        String wrongToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // when
        Map<String, Object> payload = authTokenService.payloadOrNull(wrongToken);

        // then
        assertThat(payload).isNull();
    }

    @Test
    @DisplayName("t7: 액세스 토큰과 임시 토큰의 payload 내용이 동일한지 확인")
    void t7() {
        // given
        Member member = Member.builder()
                .email("compare@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("compareuser")
                .profileImage(null)
                .build();
        Member savedMember = memberRepository.save(member);

        // when
        String accessToken = authTokenService.genAccessToken(savedMember);
        String tempToken = authTokenService.genTempToken(savedMember);

        // then
        Map<String, Object> accessPayload = authTokenService.payloadOrNull(accessToken);
        Map<String, Object> tempPayload = authTokenService.payloadOrNull(tempToken);

        assertThat(accessPayload).isNotNull();
        assertThat(tempPayload).isNotNull();
        
        // payload 내용이 동일한지 확인
        assertThat(accessPayload.get("id")).isEqualTo(tempPayload.get("id"));
        assertThat(accessPayload.get("email")).isEqualTo(tempPayload.get("email"));
        assertThat(accessPayload.get("nickname")).isEqualTo(tempPayload.get("nickname"));
    }
}

