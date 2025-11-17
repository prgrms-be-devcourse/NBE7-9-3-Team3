package org.example.backend.domain.post.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import lombok.Getter
import lombok.NoArgsConstructor
import org.example.backend.global.jpa.entity.BaseEntity

@Entity
@Getter
@NoArgsConstructor
class PostImage(
    var imageUrl: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private var post: Post

) : BaseEntity() {

    init {
        post.images.add(this)
    }

}
