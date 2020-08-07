package ogorkiewicz.jakub.my_blog.service;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.extern.jbosslog.JBossLog;
import ogorkiewicz.jakub.my_blog.dto.MultipartFile;
import ogorkiewicz.jakub.my_blog.dto.PostDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.ImageFit;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.repository.PostImageRepository;

@ApplicationScoped
@JBossLog
public class PostImageService {

    @ConfigProperty(name = "my-blog.server")
    private String server;
    private FileService fileService;
    private PostImageRepository postImageRepository;

    @Inject
    public PostImageService(FileService fileService, PostImageRepository postImageRepository) {
        this.fileService = fileService;
        this.postImageRepository = postImageRepository;
    }

    public InputStream getImage(Long postId) throws MyBlogException {
        PostImage postImage = postImageRepository.findById(postId);

        if (postImage == null) {
            throw new MyBlogException(ErrorCode.NOT_EXIST, PostImage.class);
        }

        InputStream data = fileService.readFile(postImage.getLocalUri());

        if (data == null) {
            throw new MyBlogException(ErrorCode.NOT_EXIST, PostImage.class);
        }

        return data;
    }

    public Path addPostImage(MultipartFile multipartRequest, Post post) throws MyBlogException {
        PostImage postImage = new PostImage();
        PostDto postDto = multipartRequest.getPostDto();
        postImage.setFileName(multipartRequest.getFileName());
        postImage.setImageOffset(postDto.getImageOffset());
        postImage.setImageFit(ImageFit.valueOf(postDto.getImageFit()));
        postImage.setPost(post);
        try {
            postImage.setImageUrl(UriBuilder.fromPath(server + "/posts/{id}/image").build(post.getId()).toURL());
        } catch (MalformedURLException e) {
            log.error("Unable to create Url. Wrong format. " + e.getMessage());
        }
        Path path = fileService.saveImage(postImage.getFileName(), post.getId(), multipartRequest.getFile());
        postImage.setLocalUri(path.toString());
        postImageRepository.persist(postImage);
        return path;
    }

}