package ogorkiewicz.jakub.my_blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Captcha;
import ogorkiewicz.jakub.my_blog.repository.CaptchaRepository;
import ogorkiewicz.jakub.my_blog.service.CaptchaService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class CaptchaServiceTest {

    @InjectMocks
    CaptchaService captchaService;
    @Mock
    CaptchaRepository captchaRepository;

    private final String captchaKey = "testCaptchaKey";
    private final String testCaptcha = "TEST";

    @Test
    public void shouldCreateCaptcha() throws MyBlogException, IOException {
        // given
        given(captchaRepository.getCaptchaByKey(captchaKey)).willReturn(null);

        // when
        InputStream captchaImage = captchaService.createCaptcha(captchaKey);

        // then
        BufferedImage bufferedImage = ImageIO.read(captchaImage);

        then(captchaRepository)
            .should()
            .persist(any(Captcha.class));

        assertThat(bufferedImage)
            .isNotNull()
            .extracting(BufferedImage::getHeight, BufferedImage::getWidth)
            .containsExactly(100, 300);
    }

    @Test
    public void shouldOverWriteExistingCaptcha() throws MyBlogException, IOException {
        // given
        Captcha captcha = new Captcha(testCaptcha, captchaKey);
        given(captchaRepository.getCaptchaByKey(captchaKey)).willReturn(captcha);

        // when
        InputStream captchaImage = captchaService.createCaptcha(captchaKey);

        // then
        BufferedImage bufferedImage = ImageIO.read(captchaImage);

        then(captchaRepository)
            .should(never())
            .persist(any(Captcha.class));

        assertThat(bufferedImage)
            .isNotNull()
            .extracting(BufferedImage::getHeight, BufferedImage::getWidth)
            .containsExactly(100, 300);
        assertThat(captcha.getCaptcha())
            .isNotEqualTo(testCaptcha);
        assertThat(captcha.getCaptchaKey())
            .isEqualTo(captchaKey);
    }

    @Test
    public void shouldConfirmCaptcha() throws MyBlogException {
        // given
        Captcha captcha = new Captcha(testCaptcha, captchaKey);
        given(captchaRepository.getCaptchaByKey(captchaKey)).willReturn(captcha);

        // when
        InputStream captchaImage = captchaService.confirmCaptcha(testCaptcha, captchaKey);

        // then
        then(captchaRepository)
            .should()
            .deleteById(captcha.getId());

        assertThat(captchaImage).isNull();
    }

    @Test
    public void shouldCreateNewCaptchaIfNoMatch() throws MyBlogException, IOException {
        // given
        Captcha captcha = new Captcha(testCaptcha, captchaKey);
        given(captchaRepository.getCaptchaByKey(captchaKey)).willReturn(captcha);

        // when
        InputStream captchaImage = captchaService.confirmCaptcha("WRONG CAPTCHA", captchaKey);

        // then
        BufferedImage bufferedImage = ImageIO.read(captchaImage);

        assertThat(bufferedImage)
            .isNotNull()
            .extracting(BufferedImage::getHeight, BufferedImage::getWidth)
            .containsExactly(100, 300);
        assertThat(captcha.getCaptcha())
            .isNotEqualTo(testCaptcha);
        assertThat(captcha.getCaptchaKey())
            .isEqualTo(captchaKey);
    }

    @Test
    public void shouldThrowExceptionWhenNoCaptchaFound() throws MyBlogException, IOException {
        // given
        given(captchaRepository.getCaptchaByKey(captchaKey)).willReturn(null);

        // when// then
        MyBlogException myBlogException = new MyBlogException(ErrorCode.NOT_EXIST, Captcha.class);

        assertThatThrownBy(() -> captchaService.confirmCaptcha(testCaptcha, captchaKey))
            .isInstanceOf(MyBlogException.class)
            .usingRecursiveComparison()
            .isEqualTo(myBlogException);
        then(captchaRepository)
            .should(never())
            .deleteById(anyLong());
        then(captchaRepository)
            .should(never())
            .persist(any(Captcha.class));
    }

}