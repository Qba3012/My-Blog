package ogorkiewicz.jakub.my_blog.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;
import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.CommentDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Comment;
import ogorkiewicz.jakub.my_blog.model.CommentToken;
import ogorkiewicz.jakub.my_blog.model.ConfirmationToken;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.repository.CommentRepository;
import ogorkiewicz.jakub.my_blog.repository.ConfirmationTokenRepository;
import ogorkiewicz.jakub.my_blog.repository.PostRepository;

@ApplicationScoped
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CommentService {

    private MailService mailService;
    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private ConfirmationTokenRepository tokenRepository;

    public List<CommentDto> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.getCommentsByPostId(postId);
        return comments.stream().map(CommentDto::new).collect(Collectors.toList());
    }

    public void addNewComment(Long postId, CommentDto commentDto) throws MyBlogException {
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new MyBlogException(ErrorCode.NOT_EXIST, Post.class);
        } else {
            Comment comment = commentDto.toEntity(postId);
            commentRepository.persist(comment);

            String token = UUID.randomUUID().toString();
            tokenRepository.persist(new CommentToken(token, comment));

            mailService.sendCommentConfirmationEmail(comment.getEmail(), comment.getContent(), post.getTitle(), token);
        }
    }

    public void confirmComment(String token) throws MyBlogException {
        ConfirmationToken commentToken = tokenRepository.findByToken(token, CommentToken.class);
        if (commentToken != null) {
            commentRepository.confirmComment(commentToken.getId());
            tokenRepository.deleteToken(token, CommentToken.class);
        } else {
            throw new MyBlogException(ErrorCode.NOT_EXIST, CommentToken.class);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void purgeNotConfirmedComments() {
        tokenRepository.deleteAll();
        commentRepository.delteNotConfirmed();
    }
}