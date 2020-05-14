package ogorkiewicz.jakub.my_blog.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static ogorkiewicz.jakub.my_blog.resource.CommentResource.COMMENT_PATH;
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

import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.CommentDto;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.service.CommentService;

@Path(COMMENT_PATH)
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CommentResource {

    public final static String COMMENT_PATH = POSTS_PATH + "/{postId}/comments";
    private CommentService commentService;

    @GET
    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_JSON)
    public Response getComments(@PathParam("postId") Long postId) {
        return Response.ok().entity(commentService.getCommentsByPostId(postId)).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Transactional
    public Response addNewComment(@PathParam("postId") Long postId, @Valid CommentDto newComment) throws MyBlogException {
        commentService.addNewComment(postId, newComment);
        return Response.ok().build();
    }

}