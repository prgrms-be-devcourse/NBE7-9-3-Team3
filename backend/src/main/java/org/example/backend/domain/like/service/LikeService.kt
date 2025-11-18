package org.example.backend.domain.like.service

import org.example.backend.domain.like.dto.PostLikeResponseDto
import org.example.backend.domain.like.entity.Like
import org.example.backend.domain.like.repository.LikeRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.repository.PostRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun toggleLike(postId: Long, memberId: Long): Map<String, Any> {

        val member = memberRepository.findByIdOrNull(memberId)
            ?:throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        val post = postRepository.findByIdOrNull(postId)
            ?:throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        val existingLike = likeRepository.findByMemberAndPost(member, post)

        val liked = if (existingLike !=null) {
            likeRepository.delete(existingLike)
            post.decreaseLikeCount()
            false
        } else {
            likeRepository.save(Like(member, post))
            post.increaseLikeCount()
            true
        }

        return mapOf("liked" to liked, "likeCount" to post.likeCount)
    }


    fun getLikedPosts(memberId: Long): List<PostLikeResponseDto> {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)

        return likeRepository.findAllByMember(member).map{ like ->
                val post = postRepository.findByIdOrNull(like.post.id)
                    ?:throw BusinessException(ErrorCode.NOT_FOUND_DATA)
                PostLikeResponseDto(post.id, post.title)
            }
    }

    @Transactional(readOnly = true)
    fun existsByMemberAndPost(member: Member, post: Post): Boolean {
        return likeRepository.existsByMemberAndPost(member, post)
    }

    @Transactional(readOnly = true)
    fun findPostIdsByMember(member: Member): List<Long> {
        return likeRepository.findPostIdsByMember(member)
    }
}
