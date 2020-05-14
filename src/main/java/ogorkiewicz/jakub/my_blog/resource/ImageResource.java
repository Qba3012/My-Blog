package ogorkiewicz.jakub.my_blog.resource;

import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.service.PostImageService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static ogorkiewicz.jakub.my_blog.resource.ImageResource.IMAGE_PATH;

@Path(IMAGE_PATH)
@AllArgsConstructor(onConstructor = @__(@Inject))
public class ImageResource {

    protected static final String IMAGE_PATH = "/posts/{postId}/image";

    private PostImageService postImageService;

    @GET
    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response getImage(@PathParam("postId") Long postId) throws MyBlogException {
        InputStream data = postImageService.getImage(postId);
        return Response.ok(data).build();
    }

}
