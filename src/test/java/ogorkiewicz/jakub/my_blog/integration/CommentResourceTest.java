package ogorkiewicz.jakub.my_blog.integration;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static ogorkiewicz.jakub.my_blog.resource.CommentResource.COMMENT_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import ogorkiewicz.jakub.my_blog.dto.CommentDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.model.Comment;
import ogorkiewicz.jakub.my_blog.model.ConfirmationToken;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.repository.CommentRepository;
import ogorkiewicz.jakub.my_blog.repository.ConfirmationTokenRepository;
import ogorkiewicz.jakub.my_blog.repository.PostRepository;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(H2DatabaseTestResource.class)
public class CommentResourceTest {

    @Inject
    PostRepository postRepository;

    @Inject
    CommentRepository commentRepository;

    @Inject
    ConfirmationTokenRepository tokenRepository;

    @Inject
    MockMailbox mailbox;

    private Long postId;
    private final String testContent = "Test Content";
    private final String testEmail = "test@mail.com";
    
    @BeforeEach
    @Transactional
    public void setUpTest() {
        RestAssured.basePath = COMMENT_PATH;
        Post post = new Post();
        post.setTitle("Test");
        post.setContent(testContent);
        post.setEmail(testEmail);
        postRepository.persist(post);
        postId = post.getId();
    }

    @AfterEach
    @Transactional
    public void deletePost() {
        postRepository.deleteAll();
        tokenRepository.deleteAll();
        commentRepository.deleteAll();
        mailbox.clear();
    }

    @Test
    public void shouldAddCommentAndSendConfirmationEmail() {
        // given // when
        createComment();
        
        //then
        Comment commentDb = commentRepository.find("post_id", postId).firstResult();
        ConfirmationToken token = tokenRepository.listAll().get(0);
        Mail mail = mailbox.getMessagesSentTo(testEmail).get(0);

        assertThat(commentDb.isConfirmed())
            .isFalse();
        assertThat(mailbox.getTotalMessagesSent())
            .isEqualTo(1);
        assertThat(mail.getHtml())
            .contains(token.getToken());
    }

    @Test
    public void shouldNotAddCommentToNotExistingPost() {
        // given
        Comment comment = new Comment();
        comment.setContent(testContent); 
        comment.setEmail(testEmail);
        comment.setCreateDate(OffsetDateTime.now());
        CommentDto commentDto = new CommentDto(comment);

        // when 
        given()
            .pathParam("postId", "0")
            .body(commentDto)
            .contentType(APPLICATION_JSON)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(Post.class.getSimpleName() + "." +  ErrorCode.NOT_EXIST.getTitle()));   
    }

    @Test
    public void shouldNotGetNotConfirmedComments() {
        // given
        createComment();

        // when 
        List<CommentDto> commentDtos = given()
                .pathParam("postId", postId)
                .contentType(TEXT_PLAIN)
                .when()
                .get()
                .then()
                .log().all()
                .statusCode(OK.getStatusCode())
                .extract().body().as(new TypeRef<List<CommentDto>>(){});

        // then
        assertThat(commentDtos.size())
            .isEqualTo(0);
    }

    @Test
    public void shouldGetConfirmedComments() {
        // given
        createComment();
        confirmComments();

        // when 
        List<CommentDto> commentDtos = given()
                .pathParam("postId", postId)
                .contentType(TEXT_PLAIN)
                .when()
                .get()
                .then()
                .log().all()
                .statusCode(OK.getStatusCode())
                .extract().body().as(new TypeRef<List<CommentDto>>(){});

        // then
        assertThat(commentDtos.size())
            .isEqualTo(1);
        assertThat(commentDtos.get(0))
            .extracting(
                CommentDto::getContent,
                CommentDto::getEmail)
            .containsOnly(testContent, testEmail);
    }

    private void createComment() {
        // given
        Comment comment = new Comment();
        comment.setContent(testContent); 
        comment.setEmail(testEmail);
        comment.setCreateDate(OffsetDateTime.now());
        CommentDto commentDto = new CommentDto(comment);

        // when 
        given()
            .pathParam("postId", postId)
            .body(commentDto)
            .contentType(APPLICATION_JSON)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(OK.getStatusCode());    
    }

    @Transactional
    public void confirmComments() {
        commentRepository.update("is_confirmed = ?1", true);
    }

}