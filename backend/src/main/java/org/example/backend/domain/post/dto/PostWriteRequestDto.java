package org.example.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.Category;

public record PostWriteRequestDto(

    @NotBlank(message = "제목은 필수입니다.")
    String title,
    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @NotBlank(message = "게시판 타입은 필수입니다.")
    Post.BoardType boardType,

    List<String> imageUrls,

    Category category

) {

}
