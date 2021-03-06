package ogorkiewicz.jakub.my_blog.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ogorkiewicz.jakub.my_blog.model.PostLike;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeDto {
    
    @NotNull
    @Size(min = 1, max = 50)
    private String email;
    @NotNull
    private Long postId;

    public PostLike toEntity(){
        PostLike blogLike = new PostLike();
        blogLike.setPostId(this.postId);
        blogLike.setEmail(this.email);
        return blogLike;
    }
}