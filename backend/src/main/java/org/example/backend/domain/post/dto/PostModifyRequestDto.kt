package org.example.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PostModifyRequestDto(

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    List<String> imageUrls
) {

}
