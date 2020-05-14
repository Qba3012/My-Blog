package ogorkiewicz.jakub.my_blog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ogorkiewicz.jakub.my_blog.model.Comment;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.model.PostLike;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class PostDto {

    Long id;
    @Size(min = 1, max = 20)
    String title;
    @Size(min = 1, max = 50)
    String email;
    @Size(min = 1, max = 1000)
    String content;
    @NotNull
    String imageFit;
    @NotNull
    Double imageOffset;
    Long likes;
    Boolean isConfirmed;
    String createDate;
    String imageUrl;
    long commentsNumber;

    public PostDto(Post post, PostImage postImage) {
        this.id = post.id;
        this.title = post.title;
        this.content = post.content;
        this.email = post.email;
        this.imageOffset = postImage != null ? postImage.imageOffset : null;
        this.imageFit = postImage != null ? postImage.imageFit.name() : null;
        this.createDate = post.createDate.toString();
        this.imageUrl = postImage != null ? postImage.imageUrl.toString() : null;
        this.commentsNumber = Comment.find("post_id=?1 and is_confirmed=true",post.id).count();
        this.likes = PostLike.count("post_id", post.id);
    }

    public static Post toEntity(MultipartFile multipartFile) {
        PostDto postDto = multipartFile.postDto;
        Post post = new Post();
        post.title = postDto.getTitle();
        post.content = postDto.getContent();
        post.email = postDto.getEmail();
        return post;
    }
}
