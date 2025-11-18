package org.example.backend.domain.post.service

import org.example.backend.domain.follow.service.FollowService
import org.example.backend.domain.like.service.LikeService
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.post.dto.*
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.entity.PostImage
import org.example.backend.domain.post.repository.PostRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.image.ImageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository,
    private val imageService: ImageService,
    private val followService: FollowService,
    private val likeService: LikeService
) {

    fun findById(id: Long): Post {
        return postRepository.findByIdOrNull(id)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)
    }

    @Transactional
    fun delete(id: Long, member: Member) {
        val post = postRepository.findByIdWithAuthorAndImages(id)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        if (post.author.memberId != member.memberId) {
            throw BusinessException(ErrorCode.FORBIDDEN_ACCESS)
        }

        if (!post.images.isEmpty()) {
            val oldImageUrls = post.images.map { it.imageUrl }
            imageService.deleteFiles(oldImageUrls)
            post.deleteImageUrls() // 이미지 리스트 초기화
        }

        postRepository.delete(post)
    }

    @Transactional
    fun write(reqBody: PostWriteRequestDto, member: Member): PostResponseDto {
        val post = Post(reqBody, member)

        if (reqBody.boardType == Post.BoardType.SHOWOFF
            && reqBody.imageUrls.isEmpty()
        ) {
            throw BusinessException(ErrorCode.IMAGE_FILE_EMPTY)
        }

        reqBody.imageUrls.forEach { url ->
            post.addImage(PostImage(url, post))
        }

        postRepository.save(post)

        return PostResponseDto(post)
    }

    @Transactional
    fun modify(id: Long, reqBody: PostModifyRequestDto, member: Member): PostResponseDto {
        val post = postRepository.findByIdWithAuthorAndImages(id)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        // 작성자 검증
        if (post.author.memberId != member.memberId) {
            throw BusinessException(ErrorCode.FORBIDDEN_ACCESS)
        }

        post.updateTitle(reqBody.title)
        post.updateContent(reqBody.content)

        // 새 이미지 URL이 있고, 기존과 다를 때만 교체
        val newImageUrls = reqBody.imageUrls

        val oldImageUrls = post.images.map { it.imageUrl }
        val toDelete = oldImageUrls.filter { it !in newImageUrls }

        if (toDelete.isNotEmpty()) {
            imageService.deleteFiles(toDelete)
        }

        post.deleteImageUrls()
        newImageUrls.forEach { url ->
            post.addImage(PostImage(url, post))
        }


        postRepository.save(post)
        return PostResponseDto(post)
    }

    @Transactional(readOnly = true)
    fun getPosts(
        boardType: Post.BoardType,
        filterType: FilterType,
        member: Member,
        keyword: String?,
        category: Post.Category,
        pageable: Pageable
    ): PostListResponseDto {

        // 로그인 사용자가 좋아요한 postId를 한번에 가져오기
        val likedPostIds = likeService.findPostIdsByMember(member)

        // 로그인 사용자가 팔로우하는 회원 ID 리스트 미리 가져오기
        val followingIds = followService.findFolloweeIdsByFollower(member)

        // 팔로잉 대상이 없으면 바로 빈 결과 반환
        if (filterType == FilterType.FOLLOWING && followingIds.isEmpty()) {
            return PostListResponseDto(emptyList(), 0)
        }

        val postPage: Page<Post> = when {

            filterType == FilterType.FOLLOWING -> postRepository.findByBoardTypeAndDisplayingWithAuthorAndImagesAndIds(
                boardType, Post.Displaying.PUBLIC, followingIds, pageable
            )

            keyword.isNullOrBlank() && (category == null || category == Post.Category.ALL) ->
                postRepository.findByBoardTypeAndDisplayingWithAuthorAndImages(
                    boardType, Post.Displaying.PUBLIC, pageable
                )

            else ->
                postRepository.searchByBoardTypeAndDisplayingAndKeywordAndCategoryWithAuthorAndImages(
                    boardType, Post.Displaying.PUBLIC, keyword, category, pageable
                )
        }

        val postDtos = postPage.content
            .map { post ->

                val liked = likedPostIds.contains(post.id)
                val following = followingIds.contains(post.author.memberId)
                val isMine = (post.author.memberId
                        == member.memberId)

                PostReadResponseDto(
                    post.id,
                    post.title,
                    post.content,
                    post.author.nickname,
                    post.createDate,
                    post.images.map { it.imageUrl },
                    post.likeCount,
                    liked,
                    following,
                    post.author.memberId,
                    post.category,
                    isMine
                )
            }

        return PostListResponseDto(postDtos, postPage.totalElements.toInt())
    }

    @Transactional(readOnly = true)
    fun getPostById(id: Long, member: Member): PostReadResponseDto {
        val post = postRepository.findByIdWithAuthorAndImages(id)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        if (post.displaying == Post.Displaying.PRIVATE && post.author.memberId != member.memberId) {
            throw BusinessException(ErrorCode.POST_FORBIDDEN_ACCESS) // 비공개글
        }

        val liked = likeService.existsByMemberAndPost(member, post)
        val following = followService.existsByFollowerAndFollowee(
            member,  // 로그인 사용자
            post.author // 게시글 작성자
        )
        val isMine = post.author.memberId == member.memberId

        return PostReadResponseDto(
            post.id,
            post.title,
            post.content,
            post.author.nickname,
            post.createDate,
            post.images.map { it.imageUrl },
            post.likeCount,
            liked,
            following,
            post.author.memberId,
            post.category,
            isMine
        )
    }

    @Transactional(readOnly = true)
    fun getMyPosts(boardType: Post.BoardType, id: Long): List<MyPostReadResponseDto> {
        val posts = postRepository.findMyPostsWithAuthor(boardType, id)

        return posts.map { post ->
            MyPostReadResponseDto(
                post.id,
                post.title,
                post.displaying
            )
        }

    }
}