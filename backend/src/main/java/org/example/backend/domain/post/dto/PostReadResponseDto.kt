package org.example.backend.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.example.backend.domain.post.entity.Post.Category;

public record PostReadResponseDto(

    Long id,
    String title,
    String content,
    String nickname,
    LocalDateTime createDate,
    List<String> images,
    int likeCount,
    boolean liked,
    boolean following,
    Long authorId,
    Category category,
    boolean isMine

) {

}
