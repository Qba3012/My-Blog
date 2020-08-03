package ogorkiewicz.jakub.my_blog.dto;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ogorkiewicz.jakub.my_blog.model.Comment;

@Getter
@AllArgsConstructor
public class CommentDto {

    private final Long id;
    @Size(min = 1, max = 20)
    private final String content;
    @Size(min = 1, max = 50)
    private final String email;
    private final String createDate;

    public CommentDto(Comment comment) {
        this.id = comment.id;
        this.content = comment.content;
        this.email = comment.email;
        this.createDate = comment.createDate.toString();
    }

    public Comment toEntity(Long postId){
        Comment entity = new Comment();
        entity.content = this.content;
        entity.email = this.email;
        entity.postId = postId;
        return entity;
    }
}