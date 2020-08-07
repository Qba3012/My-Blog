package ogorkiewicz.jakub.my_blog.dto;

import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ogorkiewicz.jakub.my_blog.model.Comment;

@Getter
@NoArgsConstructor
public class CommentDto {

    private Long id;
    @Size(min = 1, max = 20)
    private String content;
    @Size(min = 1, max = 50)
    private String email;
    private String createDate;

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.email = comment.getEmail();
        this.createDate = comment.getCreateDate().toString();
    }

    public Comment toEntity(Long postId){
        Comment entity = new Comment();
        entity.setContent(this.content);
        entity.setEmail(this.email);
        entity.setPostId(postId);
        return entity;
    }
}