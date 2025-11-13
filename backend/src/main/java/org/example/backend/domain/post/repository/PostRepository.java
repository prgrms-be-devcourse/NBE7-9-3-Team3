package org.example.backend.domain.post.repository;

import java.util.List;
import java.util.Optional;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Displaying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByBoardType(BoardType boardType);

    @Query(
        value = "SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.boardType = :boardType AND p.displaying = :displaying " +
            "AND p.author.memberId IN :authorIds",
        countQuery =
            "SELECT COUNT(p) FROM Post p WHERE p.boardType = :boardType AND p.displaying = :displaying "
                +
                "AND p.author.memberId IN :authorIds"
    )
    Page<Post> findByBoardTypeAndDisplayingWithAuthorAndImagesAndIds(
        @Param("boardType") BoardType boardType,
        @Param("displaying") Displaying displaying,
        @Param("authorIds") List<Long> authorIds,
        Pageable pageable
    );

    @Query(
        value = "SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.boardType = :boardType AND p.displaying = :displaying",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.boardType = :boardType AND p.displaying = :displaying"
    )
    Page<Post> findByBoardTypeAndDisplayingWithAuthorAndImages(
        @Param("boardType") BoardType boardType,
        @Param("displaying") Displaying displaying,
        Pageable pageable
    );

    @Query(
        value = "SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author a " +   // author alias 추가
            "LEFT JOIN FETCH p.images " +
            "WHERE p.boardType = :boardType " +
            "AND p.displaying = :displaying " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR :category = 'ALL' OR p.category = :category)",
        countQuery = "SELECT COUNT(p) FROM Post p " +
            "JOIN p.author a " +
            "WHERE p.boardType = :boardType " +
            "AND p.displaying = :displaying " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR :category = 'ALL' OR p.category = :category)"
    )
    Page<Post> searchByBoardTypeAndDisplayingAndKeywordAndCategoryWithAuthorAndImages(
        @Param("boardType") Post.BoardType boardType,
        @Param("displaying") Post.Displaying displaying,
        @Param("keyword") String keyword,
        @Param("category") Post.Category category,
        Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
        "JOIN FETCH p.author " +
        "LEFT JOIN FETCH p.images " +
        "WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndImages(@Param("id") Long id);

    @Query("SELECT p FROM Post p " +
        "JOIN FETCH p.author " +
        "WHERE p.boardType = :boardType AND p.author.memberId = :id")
    List<Post> findMyPostsWithAuthor(
        @Param("boardType") BoardType boardType,
        @Param("id") Long id);


}
