package ogorkiewicz.jakub.my_blog.integration;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;
import static ogorkiewicz.jakub.my_blog.resource.ImageResource.IMAGE_PATH;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import lombok.extern.jbosslog.JBossLog;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.model.ImageFit;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.repository.PostImageRepository;
import ogorkiewicz.jakub.my_blog.repository.PostRepository;

@QuarkusTest
@JBossLog
@Tag("integration")
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ImageResourceTest {

    @ConfigProperty(name = "my-blog.directory")
    String homeDirectory;

    @ConfigProperty(name = "my-blog.server")
    private String server;

    @Inject
    PostRepository postRepository;

    @Inject
    PostImageRepository postImageRepository;

    private final String testFileName = "testImage.png";
    private final int imageSize = 10 * 1024 * 1024;
    private Long postId;
    private Path imagePath;

    @BeforeEach
    public void setUpTest() {
        RestAssured.basePath = IMAGE_PATH;
    }

    @AfterEach
    @Transactional
    public void cleanUp() throws IOException {
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        if(Files.exists(Paths.get(homeDirectory))){
            Files.walk(Paths.get(homeDirectory))
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    @Test
    public void shouldGetPostImage() throws IOException {
        // given 
        createTestFile();
        createPost();

        // when
        InputStream image = given()
            .pathParam("postId", postId)
            .when()
            .get()
            .then()
            .log().all()
            .statusCode(OK.getStatusCode())
            .extract().asInputStream();

        // then
        byte[] imageBytes1 = image.readAllBytes();
        byte[] testImageBytes2 = Files.newInputStream(imagePath).readAllBytes();

        assertTrue(Arrays.equals(imageBytes1, testImageBytes2));
    }

    @Test
    public void shouldNotGetNotExistingPostImage() {
        // when // then
        given()
            .pathParam("postId", 999)
            .when()
            .get()
            .then()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostImage.class.getSimpleName() + "." +  ErrorCode.NOT_EXIST.getTitle()));   
    }

    @Test
    public void shouldNotGetNotExistingImageFile() {
        // given 
        createPost();

        // when // then
        given()
            .pathParam("postId", postId)
            .when()
            .get()
            .then()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(PostImage.class.getSimpleName() + "." +  ErrorCode.NOT_EXIST.getTitle()));   
    }

    private void createTestFile() {
        Path dirPath = Paths.get(homeDirectory);
        imagePath = Paths.get(homeDirectory, testFileName);
        try {
            Files.createDirectories(dirPath);
            Files.createFile(imagePath);
            RandomAccessFile testImage = new RandomAccessFile(imagePath.toFile(), "rw");
            testImage.setLength(imageSize);
            testImage.close();
        } catch (IOException e) {
            log.error("Unable to create test file. " + e.getMessage());
        }
    }

    @Transactional
    public void createPost() {
        Post post = PostResourceTest.createPost();
        postRepository.persist(post);
        createPostImage(post);
        postId = post.getId();
    }

    private void createPostImage(Post post) {
        PostImage postImage = new PostImage();
        postImage.setPost(post);
        postImage.setFileName(testFileName);
        postImage.setImageFit(ImageFit.WIDTH);
        postImage.setImageOffset(0.0);
        postImage.setLocalUri(imagePath != null ? imagePath.toString() : "test");
        try {
            postImage.setImageUrl(UriBuilder.fromPath(server + "/posts/{id}/image").build(post.getId()).toURL());
        } catch (MalformedURLException e) {
            log.error("Unable to create image url. " + e.getMessage());
        }
        postImageRepository.persist(postImage);
    }
}