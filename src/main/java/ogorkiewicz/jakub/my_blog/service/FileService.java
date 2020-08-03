package ogorkiewicz.jakub.my_blog.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.extern.jbosslog.JBossLog;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;

@ApplicationScoped
@JBossLog
public class FileService {

    @ConfigProperty(name = "my-blog.directory")
    private String directory;

    public Path saveImage(String fileName,Long id, InputStream inputStream) throws MyBlogException{

        Path filePath = Paths.get(directory,"post" + id,fileName);
        if(Files.notExists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.copy(inputStream, filePath);
                return filePath;
            } catch (IOException e) {
                log.error("Unable to save file. " + e.getMessage());
                Post.deleteById(id);
                throw new MyBlogException(ErrorCode.READ_WRITE_ERROR, PostImage.class);
            }
        }
        return null;
    }

    public InputStream readFile(String localUri) throws MyBlogException{
        Path filePath = Paths.get(localUri);

        if (Files.exists(filePath)) {
            try{
                return Files.newInputStream(filePath);
            }catch (IOException e){
                log.error("Unable to read file. " + e.getMessage());
                throw new MyBlogException(ErrorCode.READ_WRITE_ERROR,PostImage.class);
            }
        } else {
            throw new MyBlogException(ErrorCode.NOT_EXIST,PostImage.class);
        }
    }

}
