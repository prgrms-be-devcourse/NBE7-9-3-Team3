package org.example.backend.domain.post.controller

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.example.backend.domain.follow.entity.Follow
import org.example.backend.domain.follow.repository.FollowRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.domain.post.dto.PostWriteRequestDto
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.entity.PostImage
import org.example.backend.domain.post.repository.PostRepository
import org.example.backend.global.LoginUtil
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var accessToken: String
    private lateinit var testMember: Member

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockitoBean
    private lateinit var s3Client: S3Client

    @MockitoBean
    private lateinit var s3Presigner: S3Presigner

    @Autowired
    private lateinit var followRepository: FollowRepository

    @BeforeEach
    fun setUp() {

        clearDatabase()
        createTestMember()
        createPosts(3)
    }

    //id 초기화
    private fun clearDatabase() {
        em.createNativeQuery("DELETE FROM post").executeUpdate()
        em.createNativeQuery("ALTER TABLE post AUTO_INCREMENT = 1").executeUpdate()
    }

    // 테스트 멤버 생성
    private fun createTestMember(): Member {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        accessToken = loginUtil.createMemberAndGetToken(
            "test1@test.com", "test1234", "테스트1", "https://example.com/img1.jpg"
        )
        return loginUtil.getMemberByEmail("test1@test.com").also { testMember = it }
    }

    // 자랑게시판 게시글 3개 생성
    private fun createPosts(count: Int, member: Member = testMember) {
        val imageUrls = listOf("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg")
        repeat(count) { i ->
            val postDto = PostWriteRequestDto(
                title = "제목${i + 1}",
                content = "내용${i + 1}",
                boardType = Post.BoardType.SHOWOFF,
                imageUrls = imageUrls,
                category = null
            )
            Post(postDto, member).apply {
                imageUrls.forEach { addImage(PostImage(it, this)) }
            }.let { postRepository.save(it) }
        }
    }

    @Test
    @DisplayName("내가 쓴 게시글 다건 조회")
    @Throws(Exception::class)
    fun getMyPosts() {

        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/my")
                .param("boardType", "SHOWOFF")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("내가 쓴 게시글 다건 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].title").value("제목1"))
    }

    @Test
    @DisplayName("내가 쓴 게시글 다건 조회 실패 - 유효하지 않은 게시판 타입")
    @Throws(Exception::class)
    fun getMyPostsInvalidBoardType() {

        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/my")
                .param("boardType", "Invalid")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("서버 내부 오류가 발생했습니다."))
    }

    @Test
    @DisplayName("게시글 생성 성공")
    @Throws(Exception::class)
    fun createPost() {
        val requestBody = """
            {
                "title": "테스트 제목1",
                "content": "테스트 내용1",
                "boardType": "SHOWOFF",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("게시글 생성"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("테스트 제목1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("테스트 내용1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.imageUrls[0]")
                    .value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg")
            )
    }

    @Test
    @DisplayName("게시글 생성 실패 - 제목 없음")
    @Throws(Exception::class)
    fun createPostBlankTitle() {
        val requestBody = """
            {
                "title": "",
                "content": "테스트 내용1",
                "boardType": "SHOWOFF",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts")
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
    @DisplayName("게시글 생성 실패 - 내용 없음")
    @Throws(Exception::class)
    fun createPostBlankContent() {
        val requestBody = """
            {
                "title": "테스트 제목1",
                "content": "",
                "boardType": "SHOWOFF",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts")
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
    @DisplayName("게시글 생성 실패 - 게시판 타입 없음")
    @Throws(Exception::class)
    fun createPostBlankBoardType() {
        val requestBody = """
            {
                "title": "테스트 제목1",
                "content": "테스트 내용1",
                "boardType": "",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg"]
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN009"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("잘못된 형식의 요청 데이터입니다."))
    }

    @Test
    @DisplayName("게시글 생성 실패 - 이미지없는 자랑게시글")
    @Throws(Exception::class)
    fun createPostNoImagesShowOff() {
        val requestBody = """
            {
                "title": "테스트 제목1",
                "content": "테스트 내용1",
                "boardType": "SHOWOFF",
                "imageUrls": []
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/posts")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("I003"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("파일이 비어있습니다."))
    }


    @Test
    @DisplayName("게시글 단건 조회 성공")
    @Throws(Exception::class)
    fun getPost() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 게시글 단건 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("제목1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("내용1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.images[0]")
                    .value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg")
            )
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 존재하지 않는 게시글")
    @Throws(Exception::class)
    fun getPostNoId() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/100")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN008"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 데이터입니다."))
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 타인의 비공개글 열람 시도")
    @Throws(Exception::class)
    fun getPostPrivatePost() {

        val post = postRepository.findById(1L).orElseThrow{ BusinessException(ErrorCode.NOT_FOUND_DATA) }
        post.setDisplayingPrivate()

        testMember = memberRepository.save<Member>(
            Member("test2@test.com", "test1234", "테스트2", "https://example.com/img1.jpg")
        )
        accessToken = authTokenService.genAccessToken(testMember)

        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("PS001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("비공개 글입니다."))
    }

    @Test
    @DisplayName("게시글 다건 조회 성공 - 필터 타입이 있는 경우")
    @Throws(Exception::class)
    fun getPostsFollowing() {

        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        val testMember2 = loginUtil.createMember(
            "test2@test.com",
            "test1234",
            "테스트2",
            "https://example.com/img1.jpg"
        )

        // 멤버2가 멤버1을 팔로우
        followRepository.deleteAll()
        followRepository.save<Follow>(Follow(testMember2, testMember))

        // 멤버 2로 로그인
        accessToken = authTokenService.genAccessToken(testMember2)

        val imageUrls =
            listOf("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg")
        val postDto = PostWriteRequestDto(
            "member2 제목",
            "member 2내용",
            Post.BoardType.SHOWOFF,
            imageUrls,
            null
        )
        val post = Post(postDto, testMember2)

        postDto.imageUrls.forEach{ url ->
            post.addImage(PostImage(url, post))
        }

        postRepository.save(post)

        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts")
                .param("boardType", "SHOWOFF")
                .param("filterType", "FOLLOWING")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("게시글 다건 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].title").value("제목3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].content").value("내용3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalCount").value(3))
        // 다건 조회는 역순으로 반환해서 3번째 글이 0번 인덱스
    }

    @Test
    @DisplayName("게시글 다건 조회 성공 - 키워드가 있는 경우")
    @Throws(Exception::class)
    fun getPostsKeyword() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts")
                .param("boardType", "SHOWOFF")
                .param("keyword", "2")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("게시글 다건 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].title").value("제목2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].content").value("내용2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalCount").value(1))
        // 키워드는 제목 내용 작성자에 키워드 포함 필터링
    }

    @Test
    @DisplayName("게시글 다건 조회 성공 - 카테고리가 있는 경우")
    @Throws(Exception::class)
    fun getPosts() {
        val imageUrls: List<String> = listOf()

        val postDto1 = PostWriteRequestDto(
            "제목 카테고리 물고기",
            "내용 카테고리 물고기",
            Post.BoardType.QUESTION,
            imageUrls,
            Post.Category.FISH
        )
        postRepository.save(Post(postDto1, testMember))

        val postDto2 = PostWriteRequestDto(
            "제목 카테고리 수조",
            "내용 카테고리 수조",
            Post.BoardType.QUESTION,
            imageUrls,
            Post.Category.AQUARIUM
        )
        postRepository.save<Post?>(Post(postDto2, testMember))

        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts")
                .param("boardType", "QUESTION")
                .param("category", "FISH")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("게시글 다건 조회"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].title").value("제목 카테고리 물고기"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.posts[0].content").value("내용 카테고리 물고기")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalCount").value(1))
        // 카테고리는 질문 게시판에서만 작동
    }

    @Test
    @DisplayName("게시글 다건 조회 실패 - 유효하지 않은 게시판 타입")
    @Throws(Exception::class)
    fun getPostsInvalidBoardType() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get("/api/posts")
                .param("boardType", "INVALID")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("서버 내부 오류가 발생했습니다."))
    }

    @Test
    @DisplayName("게시글 수정 성공")
    @Throws(Exception::class)
    fun modifyPost() {
        val requestBody = """
            {
                "title": "수정 테스트 제목1",
                "content": "수정 테스트 내용1",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"]
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.patch("/api/posts/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 게시글 수정"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("수정 테스트 제목1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("수정 테스트 내용1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.imageUrls[0]")
                    .value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg")
            )
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    @Throws(Exception::class)
    fun modifyPostNoId() {
        val requestBody = """
            {
                "title": "수정 테스트 제목1",
                "content": "수정 테스트 내용1",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"]
            }
            
            """.trimIndent()

        val result = mvc.perform(
            MockMvcRequestBuilders.patch("/api/posts/100")
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
    @DisplayName("게시글 수정 실패 - 타인의 게시글")
    @Throws(Exception::class)
    fun modifyPostOhters() {
        val requestBody = """
            {
                "title": "수정 테스트 제목1",
                "content": "수정 테스트 내용1",
                "imageUrls": ["https://test-bucket.s3.ap-northeast-2.amazonaws.com/test2.jpg"]
            }
            
            """.trimIndent()

        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        accessToken = loginUtil.createMemberAndGetToken(
            "test2@test.com",
            "test1234",
            "테스트2",
            "https://example.com/img1.jpg"
        )

        val result = mvc.perform(
            MockMvcRequestBuilders.patch("/api/posts/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }


    @Test
    @DisplayName("게시글 삭제 성공")
    @Throws(Exception::class)
    fun deletePost() {
        val result = mvc.perform(
            MockMvcRequestBuilders.delete("/api/posts/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 게시글 삭제"))
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    @Throws(Exception::class)
    fun deletePostNoId() {
        val result = mvc.perform(
            MockMvcRequestBuilders.delete("/api/posts/100")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN008"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 데이터입니다."))
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 타인의 게시글")
    @Throws(Exception::class)
    fun deletePostOhters() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)
        accessToken = loginUtil.createMemberAndGetToken(
            "test2@test.com",
            "test1234",
            "테스트2",
            "https://example.com/img1.jpg"
        )

        val result = mvc.perform(
            MockMvcRequestBuilders.delete("/api/posts/1")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("CMN007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }
}

