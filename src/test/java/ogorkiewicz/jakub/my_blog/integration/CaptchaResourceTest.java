package ogorkiewicz.jakub.my_blog.integration;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static ogorkiewicz.jakub.my_blog.resource.CaptchaResource.CAPTCHA_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.model.Captcha;
import ogorkiewicz.jakub.my_blog.repository.CaptchaRepository;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(H2DatabaseTestResource.class)
public class CaptchaResourceTest {

    @Inject
    CaptchaRepository captchaRepository;
    
    private final String testCaptchaKey = UUID.randomUUID().toString();
    final char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPRSTUVWXYZ0123456789".toCharArray();

    @BeforeEach
    public void setUpTest() {
        RestAssured.basePath = CAPTCHA_PATH;
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        captchaRepository.deleteAll();
    }

    @Test
    public void shouldGetNewCaptcha() throws IOException {
        // given // when
        InputStream captcha =createCaptcha();
        
        // then
        BufferedImage bufferedImage = ImageIO.read(captcha);
        String captchaText = captchaRepository.getCaptchaByKey(testCaptchaKey).getCaptcha();

        assertThat(bufferedImage)
            .isNotNull()
            .extracting(BufferedImage::getHeight, BufferedImage::getWidth)
            .containsExactly(100, 300);
        assertThat(captchaRepository.count())
            .isEqualTo(1);
        assertThat(alphabet)
            .contains(captchaText.toCharArray());
    }

    @Test
    public void shouldOverwriteCaptcha() throws IOException {
        // given 
        InputStream captchaImage1 = createCaptcha();
        
        // when
        InputStream captchaImage2 = createCaptcha();
    
        // then
        BufferedImage bufferedImage = ImageIO.read(captchaImage2);
        byte[] captchaByte1 = captchaImage1.readAllBytes();
        byte[] captchaByte2 = captchaImage2.readAllBytes();

        assertThat(bufferedImage)
            .isNotNull()
            .extracting(BufferedImage::getHeight, BufferedImage::getWidth)
            .containsExactly(100, 300);
        assertFalse(Arrays.equals(captchaByte1, captchaByte2));
        assertThat(captchaRepository.count())
            .isEqualTo(1);
    }

    @Test
    public void shouldNotGetCaptchaWithNoKey() {
        // when // then
        given()
            .contentType(TEXT_PLAIN)
            .when()
            .get("/new")
            .then()
            .log().all()
            .body("errorCode",
                Is.is(Captcha.class.getSimpleName() + "." +  ErrorCode.CAPTCHA_KEY_NULL.getTitle()));
    }

    @Test
    public void shouldValidateCaptcha() {
        // given
        createCaptcha();
        String captcha = captchaRepository.listAll().get(0).getCaptcha();

        // when
        given()
            .queryParam("captcha", captcha)
            .queryParam("captchaKey", testCaptchaKey)
            .contentType(TEXT_PLAIN)
            .when()
            .post("/validate")
            .then()
            .log().all()
            .statusCode(OK.getStatusCode());
        
        assertThat(captchaRepository.count())
            .isZero();
    }

    @Test
    public void shouldCreateNewCaptchaIfValidationFails() throws IOException {
        // given 
        InputStream captchaImage1 = createCaptcha();
        
        // when
        InputStream captchaImage2 = given()
            .queryParam("captcha", "****")
            .queryParam("captchaKey", testCaptchaKey)
            .contentType(TEXT_PLAIN)
            .when()
            .post("/validate")
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .extract().body().asInputStream();
        
        // then
        BufferedImage bufferedImage = ImageIO.read(captchaImage2);
        byte[] captchaByte1 = captchaImage1.readAllBytes();
        byte[] captchaByte2 = captchaImage2.readAllBytes();
        String captchaText = captchaRepository.getCaptchaByKey(testCaptchaKey).getCaptcha();

        assertThat(bufferedImage)
            .isNotNull()
            .extracting(BufferedImage::getHeight, BufferedImage::getWidth)
            .containsExactly(100, 300);
        assertFalse(Arrays.equals(captchaByte1, captchaByte2));
        assertThat(captchaRepository.count())
            .isEqualTo(1);
        assertThat(alphabet)
            .contains(captchaText.toCharArray());
    }

    @Test
    public void shouldReturnExceptionIfCaptchaKeyNotExist() {
        // when // then
        given()
            .queryParam("captcha", "TEST")
            .queryParam("captchaKey", "****")
            .contentType(TEXT_PLAIN)
            .when()
            .post("/validate")
            .then()
            .log().all()
            .statusCode(BAD_REQUEST.getStatusCode())
            .body("errorCode",
                Is.is(Captcha.class.getSimpleName() + "." +  ErrorCode.NOT_EXIST.getTitle()));
    }

    private InputStream createCaptcha() {
        return given()
            .queryParam("captchaKey", testCaptchaKey)
            .contentType(TEXT_PLAIN)
            .when()
            .get("/new")
            .then()
            .log().all()
            .statusCode(OK.getStatusCode())
            .extract().body().asInputStream();
    }

}