package ogorkiewicz.jakub.my_blog.service;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
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

@ApplicationScoped
@JBossLog
public class PostImageService {

    @ConfigProperty(name = "my-blog.server")
    private String server;
    private FileService fileService;

    public PostImageService(FileService fileService) {
        this.fileService = fileService;
    }

    public InputStream getImage(Long postId) throws MyBlogException{
        PostImage postImage = PostImage.findById(postId);

        if(postImage == null){
            throw new MyBlogException(ErrorCode.NOT_EXIST,PostImage.class);
        }

        InputStream data = fileService.readFile(postImage.localUri);

        if(data == null){
            throw new MyBlogException(ErrorCode.NOT_EXIST,PostImage.class);
        }

        return data;
    }

    public Path addPostImage(MultipartFile multipartRequest, Post post) throws MyBlogException{
        PostImage postImage = new PostImage();
        PostDto postDto = multipartRequest.postDto;
        postImage.fileName = multipartRequest.fileName;
        postImage.imageOffset = postDto.getImageOffset();
        postImage.imageFit = ImageFit.valueOf(postDto.getImageFit());
        postImage.post = post;
        try {
            postImage.imageUrl = UriBuilder.fromPath(server + "/posts/{id}/image").build(post.id).toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Path path = fileService.saveImage(postImage.fileName,post.id,multipartRequest.file);
        postImage.localUri = path.toString();
        PostImage.persist(postImage);
        return path;
    }

}
