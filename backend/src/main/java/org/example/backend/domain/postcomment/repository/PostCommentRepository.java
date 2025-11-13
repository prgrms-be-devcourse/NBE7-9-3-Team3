package org.example.backend.domain.postcomment.repository;

import java.util.List;
import java.util.Optional;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("SELECT c FROM PostComment c JOIN FETCH c.post WHERE c.author.memberId = :memberId")
    List<PostComment> findByAuthor_MemberIdWithPost(@Param("memberId") Long memberId);

    List<PostComment> findByAuthor_MemberIdAndPost_BoardType(Long memberId, BoardType boardType);

    @Query("SELECT c FROM PostComment c JOIN FETCH c.author WHERE c.id = :id")
    Optional<PostComment> findByIdWithAuthor(@Param("id") Long id);

    @Query("SELECT c FROM PostComment c " +
        "JOIN FETCH c.author " +
        "WHERE c.post.id = :postId " +
        "ORDER BY c.createDate DESC")
    List<PostComment> findByPostIdWithAuthor(@Param("postId") Long postId);
}
