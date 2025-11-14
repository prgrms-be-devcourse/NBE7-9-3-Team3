package org.example.backend.domain.post.postcontroller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Category;
import org.example.backend.domain.post.repository.PostRepository;
import org.example.backend.domain.post.service.PostService;
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


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private PostService postService;

    @PersistenceContext
    private EntityManager em;

    private String accessToken;
    private Member testMember;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private S3Presigner s3Presigner;

    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void setUp() {
        // DB 초기화
        postRepository.deleteAll(); // 서비스 메서드로 초기화
        memberRepository.deleteAll();
        em.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();

        // 테스트 멤버 생성
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);
        testMember = memberRepository.save(
            new Member("test1@test.com", "test1234", "테스트1", "https://example.com/img1.jpg")
        );

        // 테스트용 JWT 발급 (서비스와 동일한 secret 사용)
        accessToken = authTokenService.genAccessToken(testMember);

        // 게시글 5개 생성
        List<String> imageUrls = List.of("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg");
        for (int i = 1; i <= 3; i++) {
            PostWriteRequestDto postDto = new PostWriteRequestDto(
                "제목%d".formatted(i),
                "내용%d".formatted(i),
                Post.BoardType.SHOWOFF,
                imageUrls,
                null
            );
            postService.write(postDto, testMember);
        }
    }

    @Test
    @DisplayName("내가 쓴 게시글 다건 조회")
    void getMyPosts() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/my")
                .param("boardType", "SHOWOFF")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("내가 쓴 게시글 다건 조회"))
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].title").value("제목1"));

    }

    @Test
    @DisplayName("내가 쓴 게시글 다건 조회 실패 - 유효하지 않은 게시판 타입")
    void getMyPostsInvalidBoardType() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/my")
                .param("boardType", "Invalid")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.resultCode").value("CMN002"))
            .andExpect(jsonPath("$.msg").value("서버 내부 오류가 발생했습니다."));

    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost() throws Exception {
        String requestBody = """
            {
                "title": "테스트 제목1",
                "content": "테스트 내용1",
                "boardType": "SHOWOFF",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("게시글 생성"))
            .andExpect(jsonPath("$.data.title").value("테스트 제목1"))
            .andExpect(jsonPath("$.data.content").value("테스트 내용1"))
            .andExpect(jsonPath("$.data.imageUrls[0]").value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 제목 없음")
    void createPostBlankTitle() throws Exception {
        String requestBody = """
            {
                "title": "",
                "content": "테스트 내용1",
                "boardType": "SHOWOFF",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts")
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
    @DisplayName("게시글 생성 실패 - 내용 없음")
    void createPostBlankContent() throws Exception {
        String requestBody = """
            {
                "title": "테스트 제목1",
                "content": "",
                "boardType": "SHOWOFF",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts")
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
    @DisplayName("게시글 생성 실패 - 게시판 타입 없음")
    void createPostBlankBoardType() throws Exception {
        String requestBody = """
            {
                "title": "테스트 제목1",
                "content": "테스트 내용1",
                "boardType": "",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("CMN009"))
            .andExpect(jsonPath("$.msg").value("잘못된 형식의 요청 데이터입니다."));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 이미지없는 자랑게시글")
    void createPostNoImagesShowOff() throws Exception {
        String requestBody = """
            {
                "title": "테스트 제목1",
                "content": "테스트 내용1",
                "boardType": "SHOWOFF",
                "imageUrls": []
            }
            """;

        ResultActions result = mvc.perform(
            post("/api/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("I003"))
            .andExpect(jsonPath("$.msg").value("파일이 비어있습니다."));
    }


    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getPost() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("1번 게시글 단건 조회"))
            .andExpect(jsonPath("$.data.title").value("제목1"))
            .andExpect(jsonPath("$.data.content").value("내용1"))
            .andExpect(jsonPath("$.data.images[0]").value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"));
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 존재하지 않는 게시글")
    void getPostNoId() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts/100")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("CMN008"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 타인의 비공개글 열람 시도")
    void getPostPrivatePost() throws Exception {

        Post post = postRepository.findById(1L).get();
        post.setDisplayingPrivate();

        testMember = memberRepository.save(
            new Member("test2@test.com", "test1234", "테스트2", "https://example.com/img1.jpg")
        );
        accessToken = authTokenService.genAccessToken(testMember);

        ResultActions result = mvc.perform(
            get("/api/posts/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("PS001"))
            .andExpect(jsonPath("$.msg").value("비공개 글입니다."));
    }

    @Test
    @DisplayName("게시글 다건 조회 성공 - 필터 타입이 있는 경우")
    void getPostsFollowing() throws Exception {

        // 멤버 2로 로그인
        Member testMember2 = memberRepository.save(
            new Member("test2@test.com", "test1234", "테스트2", "https://example.com/img1.jpg")
        );

        // 멤버2가 멤버1을 팔로우
        followRepository.deleteAll();
        followRepository.save(new Follow(testMember2, testMember));

        accessToken = authTokenService.genAccessToken(testMember2);

        List<String> imageUrls = List.of("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg");
        PostWriteRequestDto postDto = new PostWriteRequestDto(
            "member2 제목",
            "member 2내용",
            Post.BoardType.SHOWOFF,
            imageUrls,
            null
        );
        postService.write(postDto, testMember2);

        ResultActions result = mvc.perform(
            get("/api/posts")
                .param("boardType", "SHOWOFF")
                .param("filterType", "FOLLOWING")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("게시글 다건 조회"))
            .andExpect(jsonPath("$.data.posts[0].title").value("제목3"))
            .andExpect(jsonPath("$.data.posts[0].content").value("내용3"))
            .andExpect(jsonPath("$.data.totalCount").value(3));
        // 다건 조회는 역순으로 반환해서 3번째 글이 0번 인덱스
    }

    @Test
    @DisplayName("게시글 다건 조회 성공 - 키워드가 있는 경우")
    void getPostsKeyword() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts")
                .param("boardType", "SHOWOFF")
                .param("keyword", "2")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("게시글 다건 조회"))
            .andExpect(jsonPath("$.data.posts[0].title").value("제목2"))
            .andExpect(jsonPath("$.data.posts[0].content").value("내용2"))
            .andExpect(jsonPath("$.data.totalCount").value(1));
        // 키워드는 제목 내용 작성자에 키워드 포함 필터링
    }

    @Test
    @DisplayName("게시글 다건 조회 성공 - 카테고리가 있는 경우")
    void getPosts() throws Exception {

        List<String> imageUrls = new ArrayList<>();

        PostWriteRequestDto postDto1 = new PostWriteRequestDto(
            "제목 카테고리 물고기",
            "내용 카테고리 물고기",
            BoardType.QUESTION,
            imageUrls,
            Category.FISH);
        postService.write(postDto1, testMember);

        PostWriteRequestDto postDto2 = new PostWriteRequestDto(
            "제목 카테고리 수조",
            "내용 카테고리 수조",
            BoardType.QUESTION,
            imageUrls,
            Category.AQUARIUM);
        postService.write(postDto2, testMember);

        ResultActions result = mvc.perform(
            get("/api/posts")
                .param("boardType", "QUESTION")
                .param("category", "FISH")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("게시글 다건 조회"))
            .andExpect(jsonPath("$.data.posts[0].title").value("제목 카테고리 물고기"))
            .andExpect(jsonPath("$.data.posts[0].content").value("내용 카테고리 물고기"))
            .andExpect(jsonPath("$.data.totalCount").value(1));
        // 카테고리는 질문 게시판에서만 작동
    }

    @Test
    @DisplayName("게시글 다건 조회 실패 - 유효하지 않은 게시판 타입")
    void getPostsInvalidBoardType() throws Exception {

        ResultActions result = mvc.perform(
            get("/api/posts")
                .param("boardType", "INVALID")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.resultCode").value("CMN002"))
            .andExpect(jsonPath("$.msg").value("서버 내부 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void modifyPost() throws Exception {
        String requestBody = """
            {
                "title": "수정 테스트 제목1",
                "content": "수정 테스트 내용1",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"]
            }
            """;

        ResultActions result = mvc.perform(
            patch("/api/posts/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("1번 게시글 수정"))
            .andExpect(jsonPath("$.data.title").value("수정 테스트 제목1"))
            .andExpect(jsonPath("$.data.content").value("수정 테스트 내용1"))
            .andExpect(jsonPath("$.data.imageUrls[0]").value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    void modifyPostNoId() throws Exception {
        String requestBody = """
            {
                "title": "수정 테스트 제목1",
                "content": "수정 테스트 내용1",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"]
            }
            """;

        ResultActions result = mvc.perform(
            patch("/api/posts/100")
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
    @DisplayName("게시글 수정 실패 - 타인의 게시글")
    void modifyPostOhters() throws Exception {
        String requestBody = """
            {
                "title": "수정 테스트 제목1",
                "content": "수정 테스트 내용1",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"]
            }
            """;

        testMember = memberRepository.save(
            new Member("test2@test.com", "test1234", "테스트2", "https://example.com/img1.jpg")
        );
        accessToken = authTokenService.genAccessToken(testMember);

        ResultActions result = mvc.perform(
            patch("/api/posts/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(print());

        result
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("CMN007"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }



    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost() throws Exception {

        ResultActions result = mvc.perform(
            delete("/api/posts/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("1번 게시글 삭제"));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    void deletePostNoId() throws Exception {

        ResultActions result = mvc.perform(
            delete("/api/posts/100")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("CMN008"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 타인의 게시글")
    void deletePostOhters() throws Exception {

        testMember = memberRepository.save(
            new Member("test2@test.com", "test1234", "테스트2", "https://example.com/img1.jpg")
        );
        accessToken = authTokenService.genAccessToken(testMember);

        ResultActions result = mvc.perform(
            delete("/api/posts/1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        result
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("CMN007"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

}

