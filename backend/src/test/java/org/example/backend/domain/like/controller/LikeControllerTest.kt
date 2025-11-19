package org.example.backend.domain.like.controller

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.example.backend.domain.like.entity.Like
import org.example.backend.domain.like.repository.LikeRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.post.dto.PostWriteRequestDto
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.entity.PostImage
import org.example.backend.domain.post.repository.PostRepository
import org.example.backend.domain.postcomment.repository.PostCommentRepository
import org.example.backend.global.LoginUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class LikeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var accessToken: String
    private lateinit var testMember: Member

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var postCommentRepository: PostCommentRepository

    @Autowired
    private lateinit var likeRepository: LikeRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    @BeforeEach
    fun setUp() {
        //id 초기화
        em.createNativeQuery("ALTER TABLE likes AUTO_INCREMENT = 1").executeUpdate()
        em.createNativeQuery("ALTER TABLE post AUTO_INCREMENT = 1").executeUpdate()
        em.createNativeQuery("ALTER TABLE post_comment AUTO_INCREMENT = 1").executeUpdate()

        // 테스트 멤버 생성
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        accessToken = loginUtil.createMemberAndGetToken(
            "test1@test.com",
            "test1234",
            "테스트1",
            "https://example.com/img1.jpg"
        )
        testMember = loginUtil.getMemberByEmail("test1@test.com")

        // 게시글 3개 생성
        val imageUrls = listOf("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg")
        for (i in 1..3) {
            val postDto = PostWriteRequestDto(
                "제목$i",
                "내용$i",
                Post.BoardType.SHOWOFF,
                imageUrls,
                null
            )
            val post = Post(postDto, testMember)

            imageUrls.forEach { url ->
                post.addImage(PostImage(url, post))
            }

            postRepository.save(post)
        }

        // 1번 2번에 좋아요 남기기
        val post1 = postRepository.findById(1L).orElseThrow()
        likeRepository.save(Like(testMember, post1))
        post1.increaseLikeCount()

        val post2 = postRepository.findById(2L).orElseThrow()
        likeRepository.save(Like(testMember, post2))
        post2.increaseLikeCount()
    }

    @Test
    @DisplayName("좋아요 남기기 성공")
    @Throws(Exception::class)
    fun makeLikes() {
        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts/3/likes")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("좋아요 토글 완료"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.likeCount").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.liked").value("true"))
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    @Throws(Exception::class)
    fun cancelLikes() {
        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts/1/likes")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("좋아요 토글 완료"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.likeCount").value("0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.liked").value("false"))
    }

    @Test
    @DisplayName("좋아요 실패 - 게시글이 없는 경우")
    @Throws(Exception::class)
    fun likesNoMember() {
        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts/4/likes")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN008"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 데이터입니다."))
    }

    @Test
    @DisplayName("좋아요 남긴 게시글 조회 성공")
    @Throws(Exception::class)
    fun getLikedPosts() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/likes/my")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("좋아요한 글 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].title").value("제목1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].id").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].title").value("제목2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value("2"))
    }
}
