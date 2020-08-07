package ogorkiewicz.jakub.my_blog.dto;

import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ogorkiewicz.jakub.my_blog.constraint.FileName;

@Getter
@NoArgsConstructor
public class MultipartFile {

    @Valid
    @FormParam("post")
    @PartType(MediaType.APPLICATION_JSON)
    private PostDto postDto;

    @NotNull
    @FileName
    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    private String fileName;

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    private InputStream file;

}