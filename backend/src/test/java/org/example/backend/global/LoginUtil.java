package org.example.backend.global;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;

// 테스트에서 Repository를 직접 사용하여 빠르게 회원 생성 및 로그인 토큰을 발급하는 유틸리티 클래스
public class LoginUtil {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public LoginUtil(MemberRepository memberRepository, 
                     PasswordEncoder passwordEncoder, 
                     AuthTokenService authTokenService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    // 회원을 생성하고 Member 엔티티를 반환하는 메서드
    public Member createMember(String email, String password, String nickname, String profileImage) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 회원 생성
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();

        return memberRepository.save(member);
    }

    // 회원을 생성하고 JWT 토큰을 반환하는 메서드
    public String createMemberAndGetToken(String email, String password, String nickname, String profileImage) {
        Member savedMember = createMember(email, password, nickname, profileImage);
        // JWT 토큰 생성
        return authTokenService.genAccessToken(savedMember);
    }

    // 이메일로 저장된 Member 엔티티를 조회하는 메서드
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}

