package org.example.backend.domain.post.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.example.backend.domain.like.entity.Like
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.post.dto.PostWriteRequestDto
import org.example.backend.domain.postcomment.entity.PostComment
import org.example.backend.global.jpa.entity.BaseEntity

@Entity
class Post(

    @Column(nullable = false)
    @field:NotBlank(message = "제목은 필수입니다.")
    var title: String,

    @Column(nullable = false)
    @field:NotBlank(message = "내용은 필수입니다.")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val author: Member,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @field:NotNull(message = "게시판 종류는 필수입니다.")
    val boardType: BoardType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @field:NotNull(message = "공개 여부는 필수입니다.")
    var displaying: Displaying,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var category: Category? = null,

) : BaseEntity() {

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val comments: MutableList<PostComment> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<PostImage> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val likes: MutableList<Like> = mutableListOf()

    var likeCount = 0

    enum class BoardType {
        SHOWOFF, QUESTION
    }

    enum class Displaying {
        PUBLIC, PRIVATE
    }

    enum class Category {
        ALL, FISH, AQUARIUM
    }

    constructor(reqBody: PostWriteRequestDto, author: Member) : this(
        reqBody.title,
        reqBody.content,
        author,
        reqBody.boardType,
        Displaying.PUBLIC,
        reqBody.category
    ) {
    }

    fun increaseLikeCount() {
        this.likeCount++
    }

    fun decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--
    }

    fun addImage(image: PostImage) {
        images.add(image)
    }

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateContent(content: String) {
        this.content = content
    }

    fun deleteImageUrls() {
        this.images.clear()
    }

    fun setDisplayingPrivate() {
        this.displaying = Displaying.PRIVATE
    }


}