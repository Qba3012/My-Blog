package ogorkiewicz.jakub.my_blog.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static ogorkiewicz.jakub.my_blog.resource.PostResource.POSTS_PATH;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.MultipartFile;
import ogorkiewicz.jakub.my_blog.dto.PostLikeDto;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.service.PostService;

@Path(POSTS_PATH)
@AllArgsConstructor(onConstructor = @__(@Inject))
@Consumes(TEXT_PLAIN)
@Produces(APPLICATION_JSON)
public class PostResource {

    public final static String POSTS_PATH = "/posts";
    private PostService postService;

    @GET
    @Path("/page/{pageNumber}")
    public Response getPageByNumber(@PathParam("pageNumber") int pageNumber) throws MyBlogException {
        return Response.ok().entity(postService.getPage(pageNumber)).build();
    }

    @POST
    @Transactional
    @Consumes(MULTIPART_FORM_DATA)
    public Response addNewPost(@Valid @MultipartForm MultipartFile multipartRequest) throws MyBlogException {
        postService.addPost(multipartRequest);
        return Response.ok().build();
    }

    @POST
    @Path("/likes")
    @Transactional
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response addLike(@Valid PostLikeDto postLikeDto) throws MyBlogException {
        Long likesNumber = postService.addLike(postLikeDto);
        return Response.ok(likesNumber).build();
    }

}