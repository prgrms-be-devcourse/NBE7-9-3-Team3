package org.example.backend.domain.post.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.global.jpa.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
public class PostImage extends BaseEntity {

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public PostImage(String imageUrl, Post post) {
        this.imageUrl = imageUrl;
        this.post = post;
        post.getImages().add(this);
    }

    public void setPostForConstructor(Post post) {
        this.post = post;
        post.getImages().add(this);
    }
}
