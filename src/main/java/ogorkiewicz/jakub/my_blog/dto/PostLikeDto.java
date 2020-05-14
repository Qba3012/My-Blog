package ogorkiewicz.jakub.my_blog.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import ogorkiewicz.jakub.my_blog.model.PostLike;

@Getter
public class PostLikeDto {
    
    @Size(min = 1, max = 50)
    private String email;
    @NotNull
    private Long postId;

    public PostLike toEntity(){
        PostLike blogLike = new PostLike();
        blogLike.postId = this.postId;
        blogLike.email = this.email;
        return blogLike;
    }
}