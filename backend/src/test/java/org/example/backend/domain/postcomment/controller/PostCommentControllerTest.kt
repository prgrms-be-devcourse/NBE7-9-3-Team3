package org.example.backend.domain.postcomment.controller

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.post.dto.PostWriteRequestDto
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.entity.PostImage
import org.example.backend.domain.post.repository.PostRepository
import org.example.backend.domain.postcomment.entity.PostComment
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
class PostCommentControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var accessToken: String
    private lateinit var testMember1: Member
    private lateinit var testMember2: Member

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var postCommentRepository: PostCommentRepository

    @BeforeEach
    fun setUp() {

        //id 초기화
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
        testMember1 = loginUtil.getMemberByEmail("test1@test.com")
        testMember2 = loginUtil.createMember(
            "test2@test.com",
            "test1234",
            "테스트2",
            "https://example.com/img1.jpg"
        )


        // 게시글 2개 생성
        val imageUrls = listOf(
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"
        )

        val postDto = PostWriteRequestDto(
            "제목1",
            "내용1",
            Post.BoardType.SHOWOFF,
            imageUrls,
            null
        )
        val post = Post(postDto, testMember1)

        val postDto2 = PostWriteRequestDto(
            "제목2",
            "내용2",
            Post.BoardType.SHOWOFF,
            imageUrls,
            null
        )
        val post2 = Post(postDto2, testMember1)

        postDto.imageUrls.forEach { url ->
            post.addImage(PostImage(url, post))
        }

        postDto2.imageUrls.forEach { url ->
            post2.addImage(PostImage(url, post2))
        }

        postRepository.save(post)
        postRepository.save(post2)

        // 댓글 생성
        postCommentRepository.save(PostComment("댓글1-1", post, testMember1))
        postCommentRepository.save(PostComment("댓글1-2", post, testMember1))
        postCommentRepository.save(PostComment("댓글2-1", post, testMember2))
        postCommentRepository.save(PostComment("댓글2-2", post, testMember2))
    }

    @Test
    @DisplayName("댓글 목록 다건 조회 성공")
    @Throws(Exception::class)
    fun getMyPostComments() {

        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/comments")
                .param("postId", "1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("댓글 목록 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value("4"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].content").value("댓글2-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].nickname").value("테스트2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].isMine").value("false"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value("4"))

        // 다건조회는 생성시간순으로 반환
    }

    @Test
    @DisplayName("내가 쓴 댓글 다건 조회 성공")
    @Throws(Exception::class)
    fun getMyPosts() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/comments/my")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("내가 쓴 댓글 목록 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].postId").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].postTitle").value("제목1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].content").value("댓글1-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].boardType").value("SHOWOFF"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value("2"))
        // 내가 쓴 댓글 목록은 생성시간 오래된 순으로 반환
    }

    @Test
    @DisplayName("댓글 생성 성공")
    @Throws(Exception::class)
    fun createPost() {
        val requestBody = """
            {
                "content": "테스트 내용1",
                "postId": "1"
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("댓글이 생성되었습니다"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value("5"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("테스트 내용1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value("테스트1"))
    }

    @Test
    @DisplayName("댓글 생성 실패 - 내용이 없는 댓글")
    @Throws(Exception::class)
    fun createPostNoContent() {
        val requestBody = """
            {
                "content": "",
                "postId": "1"
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("서버 내부 오류가 발생했습니다."))
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 게시글")
    @Throws(Exception::class)
    fun createPostNoPost() {

        val requestBody = """
            {
                "content": "테스트 내용1",
                "postId": "3"
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts/comments")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN008"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 데이터입니다."))
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @Throws(Exception::class)
    fun deletePostComment() {

        val result = mvc.perform(
            MockMvcRequestBuilders.delete("/api/posts/comments/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 댓글 삭제"))
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    @Throws(Exception::class)
    fun deletePostCommentNotExists() {

        val result = mvc.perform(
            MockMvcRequestBuilders.delete("/api/posts/comments/5")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN008"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 데이터입니다."))
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 타인의 댓글")
    @Throws(Exception::class)
    fun deletePostCommentOthers() {
        val result = mvc.perform(
            MockMvcRequestBuilders.delete("/api/posts/comments/4")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @Throws(Exception::class)
    fun modifyPostComment() {
        val requestBody = """
            {
                "content": "테스트 내용 수정"
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.patch("/api/posts/comments/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())


        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 댓글 수정"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("테스트 내용 수정"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value("테스트1"))
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
    @Throws(Exception::class)
    fun modifyPostCommentNotExists() {
        val requestBody = """
            {
                "content": "테스트 내용 수정"
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.patch("/api/posts/comments/5")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN008"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 데이터입니다."))
    }

    @Test
    @DisplayName("댓글 수정 실패 - 타인의 댓글")
    @Throws(Exception::class)
    fun modifyPostCommentOthers() {
        val requestBody = """
            {
                "content": "테스트 내용 수정"
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.patch("/api/posts/comments/4")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }
}
