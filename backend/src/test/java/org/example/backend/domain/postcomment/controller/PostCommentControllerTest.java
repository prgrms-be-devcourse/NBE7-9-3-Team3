package org.example.backend.domain.postcomment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.PostImage;
import org.example.backend.domain.post.repository.PostRepository;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.example.backend.domain.postcomment.repository.PostCommentRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PostCommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @PersistenceContext
    private EntityManager em;

    private String accessToken;
    private Member testMember1;
    private Member testMember2;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @BeforeEach
    void setUp() {
        // DB 초기화
        postRepository.deleteAll();
        memberRepository.deleteAll();
        postCommentRepository.deleteAll();
        em.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE post_comment ALTER COLUMN id RESTART WITH 1")
            .executeUpdate();

        // 테스트 멤버 생성
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember1 = memberRepository.save(
            new Member("test1@test.com", "test1234", "테스트1", "https://example.com/img1.jpg")
        );
        // 테스트용 JWT 발급 (서비스와 동일한 secret 사용)
        accessToken = authTokenService.genAccessToken(testMember1);

        testMember2 = memberRepository.save(
            new Member("test2@test.com", "test1234", "테스트2", "https://example.com/img1.jpg")
        );

        // 게시글 2개 생성
        List<String> imageUrls = List.of(
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg");

        PostWriteRequestDto postDto = new PostWriteRequestDto(
            "제목1",
            "내용1",
            Post.BoardType.SHOWOFF,
            imageUrls,
            null
        );
        Post post = new Post(postDto, testMember1);

        PostWriteRequestDto postDto2 = new PostWriteRequestDto(
            "제목2",
            "내용2",
            Post.BoardType.SHOWOFF,
            imageUrls,
            null
        );
        Post post2 = new Post(postDto2, testMember1);

        postDto.imageUrls().forEach(url ->
            post.addImage(new PostImage(url, post)));

        postDto2.imageUrls().forEach(url ->
            post2.addImage(new PostImage(url, post)));

        postRepository.save(post);
        postRepository.save(post2);

        // 댓글 생성
        postCommentRepository.save(new PostComment("댓글1-1",post,testMember1));
        postCommentRepository.save(new PostComment("댓글1-2",post,testMember1));
        postCommentRepository.save(new PostComment("댓글2-1",post,testMember2));
        postCommentRepository.save(new PostComment("댓글2-2",post,testMember2));

    }

    @Test
    @DisplayName("댓글 목록 다건 조회 성공")
    void getMyPostComments() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/comments")
                .param("postId", "1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글 목록 조회"))
            .andExpect(jsonPath("$.data[0].id").value("4"))
            .andExpect(jsonPath("$.data[0].content").value("댓글2-2"))
            .andExpect(jsonPath("$.data[0].nickname").value("테스트2"))
            .andExpect(jsonPath("$.data[0].isMine").value("false"))
            .andExpect(jsonPath("$.data.length()").value("4"));
        // 다건조회는 생성시간순으로 반환

    }

    @Test
    @DisplayName("내가 쓴 댓글 다건 조회 성공")
    void getMyPosts() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/comments/my")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("내가 쓴 댓글 목록 조회"))
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].postId").value("1"))
            .andExpect(jsonPath("$.data[0].postTitle").value("제목1"))
            .andExpect(jsonPath("$.data[0].content").value("댓글1-1"))
            .andExpect(jsonPath("$.data[0].boardType").value("SHOWOFF"))
            .andExpect(jsonPath("$.data.length()").value("2"));
        // 내가 쓴 댓글 목록은 생성시간 오래된 순으로 반환
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createPost() throws Exception {
        String requestBody = """
            {
                "content": "테스트 내용1",
                "postId": "1"
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("댓글이 생성되었습니다"))
            .andExpect(jsonPath("$.data.id").value("5"))
            .andExpect(jsonPath("$.data.postId").value("1"))
            .andExpect(jsonPath("$.data.content").value("테스트 내용1"))
            .andExpect(jsonPath("$.data.nickname").value("테스트1"));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 내용이 없는 댓글")
    void createPostNoContent() throws Exception {
        String requestBody = """
            {
                "content": "",
                "postId": "1"
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.resultCode").value("CMN002"))
            .andExpect(jsonPath("$.msg").value("서버 내부 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 게시글")
    void createPostNoPost() throws Exception {
        String requestBody = """
            {
                "content": "테스트 내용1",
                "postId": "3"
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("CMN008"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deletePostComment() throws Exception {

        ResultActions result = mvc.perform(
            delete("/api/posts/comments/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("1번 댓글 삭제"));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void deletePostCommentNotExists() throws Exception {

        ResultActions result = mvc.perform(
            delete("/api/posts/comments/5")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("CMN008"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 타인의 댓글")
    void deletePostCommentOthers() throws Exception {

        ResultActions result = mvc.perform(
            delete("/api/posts/comments/4")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("CMN007"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void modifyPostComment() throws Exception {
        String requestBody = """
            {
                "content": "테스트 내용 수정"
            }
            """;

        ResultActions result = mvc.perform(
            patch("/api/posts/comments/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());


        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("1번 댓글 수정"))
            .andExpect(jsonPath("$.data.id").value("1"))
            .andExpect(jsonPath("$.data.postId").value("1"))
            .andExpect(jsonPath("$.data.content").value("테스트 내용 수정"))
            .andExpect(jsonPath("$.data.nickname").value("테스트1"));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
    void modifyPostCommentNotExists() throws Exception {
        String requestBody = """
            {
                "content": "테스트 내용 수정"
            }
            """;

        ResultActions result = mvc.perform(
            patch("/api/posts/comments/5")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("CMN008"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 타인의 댓글")
    void modifyPostCommentOthers() throws Exception {
        String requestBody = """
            {
                "content": "테스트 내용 수정"
            }
            """;

        ResultActions result = mvc.perform(
            patch("/api/posts/comments/4")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("CMN007"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }


}
