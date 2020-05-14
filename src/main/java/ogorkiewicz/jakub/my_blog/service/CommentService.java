package ogorkiewicz.jakub.my_blog.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


import io.quarkus.panache.common.Sort;
import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.CommentDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Comment;
import ogorkiewicz.jakub.my_blog.model.CommentToken;
import ogorkiewicz.jakub.my_blog.model.Post;

@ApplicationScoped
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CommentService {

    private MailService mailService;

    public List<CommentDto> getCommentsByPostId(Long postId){
        List<Comment> comments = Comment.find("is_confirmed = true and post_id = ?1", Sort.descending("create_date"),postId).list();
        return comments.stream().map(CommentDto::new).collect(Collectors.toList());
    }

    public void addNewComment(Long postId, CommentDto commentDto) throws MyBlogException{
        Post post = Post.findById(postId);
        if(post == null){
            throw new MyBlogException(ErrorCode.NOT_EXIST,Post.class);
        }else{
            Comment comment = commentDto.toEntity(postId);
            Comment.persist(comment);
            
            String token = UUID.randomUUID().toString();
            CommentToken.persist(new CommentToken(token, comment));

            mailService.sendCommentConfirmationEmail(comment.email,comment.content,post.title, token);
        }
    }
    
    public void confirmComment(String token) throws MyBlogException{
        CommentToken commentToken = CommentToken.find("token",token).firstResult();
        if(commentToken != null){
            Comment.update("is_confirmed = true where id =?1", commentToken.id);
            CommentToken.deleteById(commentToken.id);
        }else{
            throw new MyBlogException(ErrorCode.NOT_EXIST, CommentToken.class);
        }
    }

    // @Scheduled(cron="0 0 0 * * ?")
    // @Transactional
    // public void purgeNotConfirmedComments(){
    //     CommentConfirmationToken.deleteAll();
    //     Comment.delete("is_confirmed",false);
    // }
}
