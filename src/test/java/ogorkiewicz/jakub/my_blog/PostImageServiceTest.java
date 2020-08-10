package ogorkiewicz.jakub.my_blog;

import static ogorkiewicz.jakub.my_blog.service.MailService.LOGO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ogorkiewicz.jakub.my_blog.dto.MultipartFile;
import ogorkiewicz.jakub.my_blog.dto.PostDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.ImageFit;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.repository.PostImageRepository;
import ogorkiewicz.jakub.my_blog.service.FileService;
import ogorkiewicz.jakub.my_blog.service.MailService;
import ogorkiewicz.jakub.my_blog.service.PostImageService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class PostImageServiceTest {

    @InjectMocks
    PostImageService postImageService;

    @Mock
    PostImageRepository postImageRepository;

    @Mock
    FileService fileService;

    private final Long postId = 1L;
    private final String fileName = "test.jpg";
    private final String fileUri = "/test/test.jpg";
    private final InputStream testImage = MailService.class.getResourceAsStream(LOGO);
    private final PostImage postImage = createPostImage();
    private final MyBlogException expectedException = new MyBlogException(ErrorCode.NOT_EXIST,PostImage.class);

    @Test
    public void shouldGetImage() throws MyBlogException {
        // given
        given(postImageRepository.findById(postId)).willReturn(postImage);
        given(fileService.readFile(fileUri)).willReturn(testImage);

        // when
        InputStream data = postImageService.getImage(postId);

        // then
        assertThat(data).isEqualTo(testImage);
    }

    @Test
    public void shouldThrowExceptionIfImageDoesNotExistInRepository() throws MyBlogException {
        // given
        given(postImageRepository.findById(postId)).willReturn(null);

        // when // then
        assertThatThrownBy(() -> postImageService.getImage(postId))
            .isInstanceOf(MyBlogException.class)
            .usingRecursiveComparison()
            .isEqualTo(expectedException);
        then(fileService)
            .should(never())
            .readFile(anyString());
    }

    @Test
    public void shouldAddImage()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            MyBlogException {
        
        // given
        Field field = postImageService.getClass().getDeclaredField("server");
        field.setAccessible(true);
        field.set(postImageService,"file:///C:/test");
        Post post = new Post();
        post.setId(postId);
        PostDto postDto = new PostDto(postId, "Test", "test@mail.com", "Test content", postImage.getImageFit().toString(), 
            Double.valueOf(0.0), 1L, OffsetDateTime.now().toString(), postImage.getImageUrl().toString(), 2L);
        MultipartFile multipartFile = new MultipartFile(postDto, fileName, testImage);

        // when
        postImageService.addPostImage(multipartFile, post);

        // then
        then(fileService)
            .should()
            .saveImage(fileName, postId, testImage);
        then(postImageRepository)
            .should()
            .persist(any(PostImage.class));
        
    }

    private PostImage createPostImage() {
        PostImage postImage = new PostImage();
        postImage.setId(postId);
        postImage.setFileName(fileName);
        postImage.setImageFit(ImageFit.HEIGHT);
        postImage.setImageOffset(0.0);
        postImage.setLocalUri(fileUri);
        postImage.setImageUrl(MailService.class.getResource(LOGO));
        return postImage;
    }

}