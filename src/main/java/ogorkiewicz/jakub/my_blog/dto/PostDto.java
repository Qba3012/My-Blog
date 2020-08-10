package ogorkiewicz.jakub.my_blog.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.repository.CommentRepository;
import ogorkiewicz.jakub.my_blog.repository.PostLikeRepository;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDto {

    private Long id;
    @Size(min = 1, max = 20)
    private String title;
    @Size(min = 1, max = 50)
    private String email;
    @Size(min = 1, max = 1000)
    private String content;
    @NotNull
    private String imageFit;
    @NotNull
    private Double imageOffset;
    private long likes;
    private String createDate;
    private String imageUrl;
    private long commentsNumber;

    public PostDto(Post post, PostImage postImage) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.email = post.getEmail();
        this.imageOffset = postImage != null ? postImage.getImageOffset() : null;
        this.imageFit = postImage != null ? postImage.getImageFit().name() : null;
        this.createDate = post.getCreateDate().toString();
        this.imageUrl = postImage != null ? postImage.getImageUrl().toString() : null;
        this.commentsNumber = new CommentRepository().getCommentCount(post.getId());
        this.likes = new PostLikeRepository().countLikes(post.getId());
    }

    public Post toEntity() {
        Post post = new Post();
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setEmail(this.email);
        return post;
    }
}