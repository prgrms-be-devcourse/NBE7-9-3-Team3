package org.example.backend.domain.post.repository

import org.example.backend.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostRepository : JpaRepository<Post, Long>,PostRepositoryCustom  {

    fun findByBoardType(boardType: Post.BoardType): List<Post>

//    @Query(
//        value = ("SELECT DISTINCT p FROM Post p " +
//                "JOIN FETCH p.author " +
//                "LEFT JOIN FETCH p.images " +
//                "WHERE p.boardType = :boardType AND p.displaying = :displaying " +
//                "AND p.author.memberId IN :authorIds"),
//        countQuery = ("SELECT COUNT(p) FROM Post p WHERE p.boardType = :boardType AND p.displaying = :displaying "
//                +
//                "AND p.author.memberId IN :authorIds")
//    )
//    fun findByBoardTypeAndDisplayingWithAuthorAndImagesAndIds(
//        @Param("boardType") boardType: Post.BoardType,
//        @Param("displaying") displaying: Displaying,
//        @Param("authorIds") authorIds: List<Long>,
//        pageable: Pageable
//    ): Page<Post>
//
//    @Query(
//        value = ("SELECT DISTINCT p FROM Post p " +
//                "JOIN FETCH p.author " +
//                "LEFT JOIN FETCH p.images " +
//                "WHERE p.boardType = :boardType AND p.displaying = :displaying"),
//        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.boardType = :boardType AND p.displaying = :displaying"
//    )
//    fun findByBoardTypeAndDisplayingWithAuthorAndImages(
//        @Param("boardType") boardType: Post.BoardType,
//        @Param("displaying") displaying: Displaying,
//        pageable: Pageable
//    ): Page<Post>
//
//    @Query(
//        value = ("SELECT DISTINCT p FROM Post p " +
//                "JOIN FETCH p.author a " +  // author alias 추가
//                "LEFT JOIN FETCH p.images " +
//                "WHERE p.boardType = :boardType " +
//                "AND p.displaying = :displaying " +
//                "AND (:keyword IS NULL OR :keyword = '' OR " +
//                "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//                "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//                "LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//                "AND (:category IS NULL OR :category = 'ALL' OR p.category = :category)"),
//        countQuery = ("SELECT COUNT(p) FROM Post p " +
//                "JOIN p.author a " +
//                "WHERE p.boardType = :boardType " +
//                "AND p.displaying = :displaying " +
//                "AND (:keyword IS NULL OR :keyword = '' OR " +
//                "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//                "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//                "LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//                "AND (:category IS NULL OR :category = 'ALL' OR p.category = :category)")
//    )
//    fun searchByBoardTypeAndDisplayingAndKeywordAndCategoryWithAuthorAndImages(
//        @Param("boardType") boardType: Post.BoardType,
//        @Param("displaying") displaying: Displaying,
//        @Param("keyword") keyword: String?,
//        @Param("category") category: Post.Category?,
//        pageable: Pageable
//    ): Page<Post>

    @Query(
        ("SELECT p FROM Post p " +
                "JOIN FETCH p.author " +
                "LEFT JOIN FETCH p.images " +
                "WHERE p.id = :id")
    )
    fun findByIdWithAuthorAndImages(
        @Param("id") id: Long
    ): Post?

    @Query(
        ("SELECT p FROM Post p " +
                "JOIN FETCH p.author " +
                "WHERE p.boardType = :boardType AND p.author.memberId = :id")
    )
    fun findMyPostsWithAuthor(
        @Param("boardType") boardType: Post.BoardType,
        @Param("id") id: Long
    ): List<Post>
}
