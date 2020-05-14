package ogorkiewicz.jakub.my_blog.resource;

import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.MailDto;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.service.CommentService;
import ogorkiewicz.jakub.my_blog.service.MailService;
import ogorkiewicz.jakub.my_blog.service.PostService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static ogorkiewicz.jakub.my_blog.resource.MailResource.MAIL_PATH;

import java.net.URI;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path(MAIL_PATH)
public class MailResource {

    @ConfigProperty(name = "my-blog.front-uri")
    private String frontUri;

    private MailService mailService;
    private PostService postService;
    private CommentService commentService;

    @Inject
    MailResource(MailService mailService, PostService postService, CommentService commentService){
        this.mailService = mailService;
        this.postService = postService;
        this.commentService = commentService;
    }
    
    public final static String MAIL_PATH = "/mail";
    public final static String POST_CONFIRMATION_PATH = "/posts/confirmation";
    public final static String COMMENT_CONFIRMATION_PATH = "/comments/confirmation";

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response sendEmail(@Valid MailDto mail){
        mailService.sendEmail(mail);
        return Response.ok().build();
    }

    @GET
    @Path(COMMENT_CONFIRMATION_PATH)
    @Transactional
    public Response confirmComment(@QueryParam("token") String token) {
        try {
			commentService.confirmComment(token);
		} catch (MyBlogException e) {
            URI uri = UriBuilder.fromPath(frontUri + "/").fragment("/comments/error").build();
            return Response.seeOther(uri).build();
		}
        return Response.seeOther(UriBuilder.fromPath(frontUri).build()).build();
    }

    @GET
    @Path(POST_CONFIRMATION_PATH)
    @Transactional
    public Response confirmPost(@QueryParam("token") String token) {
        try {
			postService.confirmPost(token);
		} catch (MyBlogException e) {
            URI uri = UriBuilder.fromPath(frontUri + "/").fragment("/posts/error").build();
            return Response.seeOther(uri).build();
		}
        return Response.seeOther(UriBuilder.fromPath(frontUri).build()).build();
    }
}
