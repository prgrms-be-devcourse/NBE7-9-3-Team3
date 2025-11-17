package org.example.backend.domain.postcomment.service

import lombok.RequiredArgsConstructor
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.post.service.PostService
import org.example.backend.domain.postcomment.dto.*
import org.example.backend.domain.postcomment.entity.PostComment
import org.example.backend.domain.postcomment.repository.PostCommentRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
class PostCommentService(
    private val postCommentRepository: PostCommentRepository,
    private val postService: PostService
) {

    @Transactional
    fun modifyPostComment(
        commentId: Long,
        reqBody: PostCommentModifyRequestDto,
        member: Member
    ): PostCommentResponseDto {

        val postComment = postCommentRepository.findByIdWithAuthor(commentId)
            .orElseThrow{ BusinessException(ErrorCode.NOT_FOUND_DATA) }

        // 작성자 검증
        if (postComment.author.memberId != member.memberId) {
            throw BusinessException(ErrorCode.FORBIDDEN_ACCESS)
        }

        postComment.modifyContent(reqBody.content)
        return PostCommentResponseDto(postComment)
    }

    @Transactional
    fun deletePostComment(commentId: Long, member: Member) {

        val postComment = postCommentRepository.findByIdWithAuthor(commentId)
            .orElseThrow { BusinessException(ErrorCode.NOT_FOUND_DATA) }

        // 작성자 검증
        if (postComment.author.memberId != member.memberId) {
            throw BusinessException(ErrorCode.FORBIDDEN_ACCESS)
        }

        postCommentRepository.delete(postComment)
    }

    @Transactional
    fun createPostComment(
        reqBody: PostCommentCreateRequestDto,
        member: Member
    ): PostCommentResponseDto {

        val post = postService.findById(reqBody.postId)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        val postComment = PostComment(
            reqBody.content,
            post,
            member
        )

        postCommentRepository.save(postComment)
        return PostCommentResponseDto(postComment)
    }

    @Transactional(readOnly = true)
    fun findMyComments(member: Member): List<MyPostCommentReadResponseDto> {
        val postComments =
            postCommentRepository.findByAuthor_MemberIdWithPost(member.memberId!!)

        return postComments
            .sortedWith(compareBy<PostComment> { it.createDate }.thenBy { it.id })
            .map{
                MyPostCommentReadResponseDto(
                it.id,
                it.post.id,
                it.post.title,
                it.content,
                it.post.boardType,
                it.post.category
                )
            }
    }

    @Transactional(readOnly = true)
    fun getPostComments(postId: Long, member: Member): List<PostCommentReadResponseDto> {
        val comments =
            postCommentRepository.findByPostIdWithAuthor(postId)

       return comments
            .map{
                PostCommentReadResponseDto(
                    it.id,
                    it.content,
                    it.author.nickname,
                    it.author.memberId == member.memberId
                )
            }
    }
}
