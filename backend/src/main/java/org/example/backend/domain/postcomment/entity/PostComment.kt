package org.example.backend.domain.postcomment.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotBlank
import lombok.Getter
import lombok.NoArgsConstructor
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.post.entity.Post
import org.example.backend.global.jpa.entity.BaseEntity

@Entity
@NoArgsConstructor
@Getter
class PostComment(

    @field:NotBlank(message = "댓글 내용은 필수입니다.")
    var content: String,

    @field:JoinColumn(name = "post_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    var post: Post,

    @field:JoinColumn(name = "member_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    var author: Member
) : BaseEntity() {

    fun modifyContent(content: String) {
        this.content = content

    }
}
