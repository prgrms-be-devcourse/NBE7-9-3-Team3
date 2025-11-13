package org.example.backend.domain.post.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.like.entity.Like;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.example.backend.global.jpa.entity.BaseEntity;

@NoArgsConstructor
@Getter
@Entity
public class Post extends BaseEntity {

    @Column(nullable = false)
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "게시판 종류는 필수입니다.")
    private BoardType boardType;

    public enum BoardType {
        SHOWOFF, QUESTION
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "공개 여부는 필수입니다.")
    private Displaying displaying;

    public enum Displaying {
        PUBLIC, PRIVATE
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    private int likeCount = 0;

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Category category;

    public enum Category {
        ALL, FISH, AQUARIUM
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    public Post(PostWriteRequestDto reqBody, Member member) {
        this.title = reqBody.title();
        this.content = reqBody.content();
        this.author = member;
        this.boardType = reqBody.boardType();
        this.displaying = Post.Displaying.PUBLIC;
        this.images = new ArrayList<>();
        this.category = reqBody.category();
    }

    public void addImage(PostImage image) {
        images.add(image);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void deleteImageUrls() {
        this.images.clear();

    }
}