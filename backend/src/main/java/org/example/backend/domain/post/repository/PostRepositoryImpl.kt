package org.example.backend.domain.post.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.example.backend.domain.member.entity.QMember
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.entity.QPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

class PostRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositoryCustom {

    override fun searchPosts(
        boardType: Post.BoardType,
        displaying: Post.Displaying,
        keyword: String?,
        category: Post.Category?,
        authorIds: List<Long>?,
        pageable: Pageable
    ): Page<Post> {
        val qPost = QPost.post
        val qAuthor = QMember.member


        val query = queryFactory.selectFrom(qPost)
            .join(qPost.author, qAuthor).fetchJoin()
            .where(
                qPost.boardType.eq(boardType),
                qPost.displaying.eq(displaying),
                keyword?.let {
                    qPost.title.containsIgnoreCase(it)
                        .or(qPost.content.containsIgnoreCase(it))
                        .or(qAuthor.nickname.containsIgnoreCase(it))
                },
                category?.takeIf { it != Post.Category.ALL }?.let { qPost.category.eq(it) },
                authorIds?.takeIf { it.isNotEmpty() }?.let { qPost.author.memberId.`in`(it) }
            )
            .orderBy(
                qPost.createDate.desc(),
                qPost.id.desc()
            )

        val content = query.offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            queryFactory.select(qPost.count())
                .from(qPost)
                .join(qPost.author, qAuthor)
                .where(
                    qPost.boardType.eq(boardType),
                    qPost.displaying.eq(displaying),
                    keyword?.let {
                        qPost.title.containsIgnoreCase(it)
                            .or(qPost.content.containsIgnoreCase(it))
                            .or(qAuthor.nickname.containsIgnoreCase(it))
                    },
                    category?.takeIf { it != Post.Category.ALL }?.let { qPost.category.eq(it) },
                    authorIds?.takeIf { it.isNotEmpty() }?.let { qPost.author.memberId.`in`(it) }
                )
                .fetchOne() ?: 0L
        }
    }
}