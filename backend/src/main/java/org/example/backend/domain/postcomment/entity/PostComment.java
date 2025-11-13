package org.example.backend.domain.postcomment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.global.jpa.entity.BaseEntity;

@Entity
@NoArgsConstructor
@Getter
public class PostComment extends BaseEntity {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    public PostComment(String content, Post post, Member member) {
        this.content = content;
        this.post = post;
        this.author = member;
    }

    public void modifyContent(String content) {
        this.content = content;
    }

}
