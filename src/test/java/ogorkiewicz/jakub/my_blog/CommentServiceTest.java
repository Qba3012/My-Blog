package ogorkiewicz.jakub.my_blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import ogorkiewicz.jakub.my_blog.service.CommentService;
import ogorkiewicz.jakub.my_blog.service.MailService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    CommentService commentService;
    @Mock
    CommentRepository commentRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    MailService mailService;
    @Mock
    ConfirmationTokenRepository tokenRepository;

    final private String commentEmail = "test@mail.com";
    final private String commentContent = "Test comment";
    final private OffsetDateTime commentCreateDate = OffsetDateTime.now();
    final private Long postId = 2L;
    final private Long commentId = 1L;
    final private Comment comment = createComment();
    final private String testToken = "testToken";

    @Test
    public void shouldGetListOfComments() {
        // given
        List<Comment> comments = new ArrayList<>(List.of(comment));

        given(commentRepository.getCommentsByPostId(postId)).willReturn(comments);

        // when
        List<CommentDto> commentDtoList = commentService.getCommentsByPostId(postId);

        // then
        assertThat(commentDtoList.size())
            .isEqualTo(1);
        assertThat(commentDtoList.get(0))
            .isInstanceOf(CommentDto.class)
            .extracting(CommentDto::getId, CommentDto::getContent, CommentDto::getEmail, CommentDto::getCreateDate)
            .containsOnly(commentId, commentContent, commentEmail, commentCreateDate.toString());
    }

    @Test
    public void shouldAddNewComment() throws MyBlogException {
        // given
        Post post = new Post();
        post.setTitle("Post title");
        given(postRepository.findById(postId)).willReturn(post);

        // when
        commentService.addNewComment(postId, new CommentDto(comment));

        // then
        then(commentRepository)
            .should()
            .persist(any(Comment.class));
        then(tokenRepository)
            .should()
            .persist(any(ConfirmationToken.class));
        then(mailService)
            .should()
            .sendCommentConfirmationEmail(eq(comment.getEmail()), eq(comment.getContent()), eq(post.getTitle()), anyString());
    }

    @Test
    public void shouldNotAddCommentToNonExistingPost() {
        // given
        given(postRepository.findById(postId)).willReturn(null);

        // when//then
        MyBlogException myBlogException = new MyBlogException(ErrorCode.NOT_EXIST, Post.class);

        assertThatThrownBy(() -> commentService.addNewComment(postId, new CommentDto(comment)))
            .isInstanceOf(MyBlogException.class)
            .usingRecursiveComparison()
            .isEqualTo(myBlogException);
        then(commentRepository)
            .should(never())
            .persist(any(Comment.class));
        then(tokenRepository)
            .should(never())
            .persist(any(ConfirmationToken.class));
        then(mailService)
            .should(never())
            .sendCommentConfirmationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldConfirmComment() throws MyBlogException {
        // given
        ConfirmationToken token = new CommentToken(testToken, comment);
        given(tokenRepository.findByToken(testToken, CommentToken.class)).willReturn(token);

        // when
        commentService.confirmComment(testToken);

        // then
        then(tokenRepository)
            .should()
            .deleteToken(testToken, CommentToken.class);
        then(commentRepository)
            .should()
            .confirmComment(null);
        ;
    }

    @Test
    public void shouldThrowExceptionIfTokenIsIncorrect() throws MyBlogException {
        // given
        given(tokenRepository.findByToken(testToken, CommentToken.class)).willReturn(null);

        // when // then
        MyBlogException myBlogException = new MyBlogException(ErrorCode.NOT_EXIST, CommentToken.class);

        assertThatThrownBy(() -> commentService.confirmComment(testToken))
            .isInstanceOf(MyBlogException.class)
            .usingRecursiveComparison()
            .isEqualTo(myBlogException);
        then(commentRepository)
            .should(never())
            .confirmComment(anyLong());
        then(tokenRepository)
            .should(never())
            .deleteToken(anyString(), any(Class.class));
    }

    private Comment createComment() {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setEmail(commentEmail);
        comment.setConfirmed(false);
        comment.setContent(commentContent);
        comment.setCreateDate(commentCreateDate);
        comment.setPostId(postId);
        return comment;
    }
}