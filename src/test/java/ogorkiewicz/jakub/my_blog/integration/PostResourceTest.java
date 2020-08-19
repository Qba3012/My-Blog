package ogorkiewicz.jakub.my_blog.integration;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static ogorkiewicz.jakub.my_blog.dto.PostDto.CONTENT_MAX_LENGTH;
import static ogorkiewicz.jakub.my_blog.dto.PostDto.EMAIL_MAX_LENGTH;
import static ogorkiewicz.jakub.my_blog.dto.PostDto.TITLE_MAX_LENGTH;
import static ogorkiewicz.jakub.my_blog.resource.PostResource.POSTS_PATH;
import static ogorkiewicz.jakub.my_blog.service.MailService.LOGO;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import ogorkiewicz.jakub.my_blog.dto.MultipartFile;
import ogorkiewicz.jakub.my_blog.dto.PageDto;
import ogorkiewicz.jakub.my_blog.dto.PostDto;
import ogorkiewicz.jakub.my_blog.dto.PostLikeDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.model.ImageFit;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.model.PostLike;
import ogorkiewicz.jakub.my_blog.repository.ConfirmationTokenRepository;
import ogorkiewicz.jakub.my_blog.repository.PostImageRepository;
import ogorkiewicz.jakub.my_blog.repository.PostLikeRepository;
import ogorkiewicz.jakub.my_blog.repository.PostRepository;
import ogorkiewicz.jakub.my_blog.service.MailService;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(H2DatabaseTestResource.class)
public class PostResourceTest {
    
    @Inject
    PostRepository postRepository;

    @Inject
    PostImageRepository postImageRepository;

    @Inject
    ConfirmationTokenRepository tokenRepository;

    @Inject
    PostLikeRepository postLikeRepository;

    protected final static String TEST_CONTENT = "Test Content";
    protected final static String TEST_EMAIL = "test@mail.com";
    private final static String TEST_TITLE = "Test Title";
    private final String testFileName= "testImage.jpg";
    private final InputStream testFile= MailService.class.getResourceAsStream(LOGO);

    private final int postsNumber = 5;
    

    @BeforeEach
    public void setUpTest() {
        RestAssured.basePath = POSTS_PATH;
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        tokenRepository.deleteAll();
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        postLikeRepository.deleteAll();
    }

    @Test
    public void shouldGetPostsPage() {
        // given
        addTestPosts();

        // when
        PageDto pageDto = given()
            .pathParam("pageNumber", 1)
            .when()
            .get("/page/{pageNumber}")
            .then()
            .statusCode(OK.getStatusCode())
            .extract().as(PageDto.class);

        // then
        assertThat(pageDto)
            .isNotNull()
            .extracting(PageDto::getPage,
                        PageDto::getTotalPages,
                        p -> p.getPosts().size())
            .containsOnly(1, 1, 5);
        pageDto.getPosts().forEach(p -> {
            assertThat(p)
                .extracting(PostDto::getTitle,
                            PostDto::getContent,
                            PostDto::getEmail)
                .containsOnly(TEST_TITLE.toUpperCase(), TEST_CONTENT, TEST_EMAIL);
        });        
    }

    @Test
    public void shouldNotGetPostsIfPageNumberToBig() {
        // given
        addTestPosts();

        // when
        PageDto pageDto = given()
            .pathParam("pageNumber", 2)
            .when()
            .get("/page/{pageNumber}")
            .then()
            .statusCode(OK.getStatusCode())
            .extract().as(PageDto.class);
        
        // then
        assertThat(pageDto)
            .isNotNull()
            .extracting(PageDto::getPage,
                    PageDto::getTotalPages,
                    p -> p.getPosts().size())
            .containsOnly(2, 1, 0);
    }
    
    @Test
    public void shouldGetErrorIfPageNumberNegative() {
        // given
        addTestPosts();

        // when // then
        given()
            .pathParam("pageNumber", -1)
            .when()
            .get("/page/{pageNumber}")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(Post.class.getSimpleName() + "." +  ErrorCode.OUT_OF_INDEX.getTitle()));   
        
    }

    @Test
    public void shouldAddPost() throws MalformedURLException, IOException {
        // given
        Post post = createPost();
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(OK.getStatusCode());
        
        // then
        Post postResult = postRepository.listAll().get(0);
        PostImage postImageResult = postImageRepository.findById(postResult.getId());

        assertThat(postResult)
            .extracting(Post::getTitle,
                        Post::getContent,
                        Post::getEmail,
                        Post::isConfirmed)
                .contains(TEST_TITLE.toUpperCase(),
                        TEST_CONTENT,
                        TEST_EMAIL,
                        false);
        assertThat(postResult.getId())
            .isNotNull();
        assertThat(postResult.getCreateDate())
            .isNotNull();
        assertThat(postImageResult)
            .extracting(PostImage::getImageOffset,
                        PostImage::getImageFit)
            .containsExactly(0.0, ImageFit.WIDTH);
    }

    @Test
    public void shouldNotAddPostWithWrongFileName() throws MalformedURLException, IOException {
        // given
        Post post = createPost();
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", "%$WRONGFILENAME$#@___")
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(MultipartFile.class.getSimpleName() + ".FileName"));   
    }
    
    @Test
    public void shouldNotAddPostWithMissingTitle() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setContent(TEST_CONTENT);
        post.setEmail(TEST_EMAIL);
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".NotNull"));  
    }

    @Test
    public void shouldNotAddPostWithToLongTitle() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setTitle(StringUtils.repeat("t", TITLE_MAX_LENGTH + 1));
        post.setContent(TEST_CONTENT);
        post.setEmail(TEST_EMAIL);
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".Size"));  
    }

    @Test
    public void shouldNotAddPostWithMissingContent() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setEmail(TEST_EMAIL);
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".NotNull"));  
    }

    @Test
    public void shouldNotAddPostWithToLongContent() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(StringUtils.repeat("t", CONTENT_MAX_LENGTH + 1));
        post.setEmail(TEST_EMAIL);
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".Size"));  
    }

    @Test
    public void shouldNotAddPostWithMissingEmail() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(TEST_CONTENT);
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".NotNull"));  
    }

    @Test
    public void shouldNotAddPostWithToLongEmail() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(TEST_CONTENT);
        post.setEmail(StringUtils.repeat("t", EMAIL_MAX_LENGTH + 1));
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageOffset(0.0);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".Size"));  
    }

    @Test
    public void shouldNotAddPostWithMissingImageOffset() throws MalformedURLException, IOException {
        // given
        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(TEST_CONTENT);
        post.setCreateDate(OffsetDateTime.now());
        PostImage postImage = new PostImage();
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageUrl(new URL("http://test"));
        PostDto postDto = new PostDto(post, postImage);

        // when // then
        given()
            .multiPart(new MultiPartSpecBuilder(postDto).controlName("post").charset("UTF-8").mimeType(APPLICATION_JSON).build())
            .multiPart("fileName", testFileName)
            .multiPart("file", testFile.readAllBytes())
            .contentType(MULTIPART_FORM_DATA)
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostDto.class.getSimpleName() + ".NotNull"));  
    }

    @Test
    public void shouldAddLike() {
        // given
        PostLikeDto postLikeDto = new PostLikeDto(TEST_EMAIL, 1L);

        // when
        String likes = given()
            .body(postLikeDto)
            .contentType(APPLICATION_JSON)
            .when()
            .post("/likes")
            .then()
            .contentType(TEXT_PLAIN)
            .log().all()
            .statusCode(OK.getStatusCode())
            .extract().asString();

        // then
        assertThat(Long.valueOf(likes))
            .isEqualTo(1L);
    }

    @Test
    public void shouldNotAddLikeIfAlreadyVoted() {
        // given
        PostLikeDto postLikeDto = new PostLikeDto(TEST_EMAIL, 1L);       
        addPostLike();

        // when // then
        given()
            .body(postLikeDto)
            .contentType(APPLICATION_JSON)
            .when()
            .post("/likes")
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostLike.class.getSimpleName() + "." +  ErrorCode.ALREADY_VOTED.getTitle()));  
    }

    @Transactional
    public void addPostLike() {
        PostLike postLike = new PostLike();
        postLike.setEmail(TEST_EMAIL);
        postLike.setPostId(1L);
        postLikeRepository.persistAndFlush(postLike);
    }

    @Transactional
    public void addTestPosts() {
        for(int i = 0; i < postsNumber; i++){
            Post post = createPost();
            postRepository.persist(post);
            postRepository.update("is_confirmed = ?1", true);
        }
    }

    public static Post createPost() {
        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(TEST_CONTENT);
        post.setEmail(TEST_EMAIL);
        post.setCreateDate(OffsetDateTime.now());
        return post;
    }
}