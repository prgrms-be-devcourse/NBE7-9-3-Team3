package org.example.backend.domain.post.dto;

import java.util.List;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.PostImage;

public record PostResponseDto (
    Long id,
    String title,
    String content,
    List<String> imageUrls
){
    public PostResponseDto(Post post) {
        this(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getImages()
                .stream()
                .map(PostImage::getImageUrl) // 이미지가 엔티티라면 URL로 변환
                .toList()
        );
    }
}
