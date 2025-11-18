package org.example.backend.domain.like.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import org.example.backend.config.TestContainerConfig;
import org.example.backend.domain.like.entity.Like;
import org.example.backend.domain.like.repository.LikeRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.PostImage;
import org.example.backend.domain.post.repository.PostRepository;
import org.example.backend.global.LoginUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@Import(TestContainerConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class LikeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthTokenService authTokenService;

    private String accessToken;
    private Member testMember;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // DB 초기화
        postRepository.deleteAll();
        memberRepository.deleteAll();
        likeRepository.deleteAll();

        em.createNativeQuery("ALTER TABLE likes ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE post_comment ALTER COLUMN id RESTART WITH 1").executeUpdate();

        // 테스트 멤버 생성
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        accessToken = loginUtil.createMemberAndGetToken( "test1@test.com","test1234", "테스트1", "https://example.com/img1.jpg");
        testMember = loginUtil.getMemberByEmail("test1@test.com");

        // 게시글 3개 생성
        List<String> imageUrls = List.of("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg");
        for (int i = 1; i <= 3; i++) {
            PostWriteRequestDto postDto = new PostWriteRequestDto(
                "제목%d".formatted(i),
                "내용%d".formatted(i),
                Post.BoardType.SHOWOFF,
                imageUrls,
                null
            );
            Post post = new Post(postDto, testMember);

            postDto.imageUrls().forEach(url ->
                post.addImage(new PostImage(url, post)));

            postRepository.save(post);
        }

        // 1번 2번에 좋아요 남기기
        Post post1 = postRepository.findById(1L).get();
        likeRepository.save(new Like(testMember,post1));
        post1.increaseLikeCount();

        Post post2 = postRepository.findById(2L).get();
        likeRepository.save(new Like(testMember,post2));
        post2.increaseLikeCount();

    }

    @Test
    @DisplayName("좋아요 남기기 성공")
    void makeLikes() throws Exception {

        ResultActions result = mvc.perform(
            post("/api/posts/3/likes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("좋아요 토글 완료"))
            .andExpect(jsonPath("$.data.likeCount").value("1"))
            .andExpect(jsonPath("$.data.liked").value("true"));

    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void cancelLikes() throws Exception {

        ResultActions result = mvc.perform(
            post("/api/posts/1/likes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("좋아요 토글 완료"))
            .andExpect(jsonPath("$.data.likeCount").value("0"))
            .andExpect(jsonPath("$.data.liked").value("false"));

    }

    @Test
    @DisplayName("좋아요 실패 - 게시글이 없는 경우")
    void likesNoMember() throws Exception {

        ResultActions result = mvc.perform(
            post("/api/posts/4/likes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("CMN008"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));

    }

    @Test
    @DisplayName("좋아요 남긴 게시글 조회 성공")
    void getLikedPosts() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/likes/my")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("좋아요한 글 조회 성공"))
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].title").value("제목1"))
            .andExpect(jsonPath("$.data[1].id").value("2"))
            .andExpect(jsonPath("$.data[1].title").value("제목2"))
            .andExpect(jsonPath("$.data.length()").value("2"));


    }


}
