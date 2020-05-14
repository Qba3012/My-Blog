package ogorkiewicz.jakub.my_blog.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.InputStream;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.service.CaptchaService;

@Path("/captcha")
@AllArgsConstructor(onConstructor = @__(@Inject))
@Produces(APPLICATION_JSON)
public class CaptchaResource {

    private CaptchaService captchaService;
   
    @POST
    @Path("/validate")
    @Transactional
    @Consumes(TEXT_PLAIN)
    public Response confirmCaptcha(@QueryParam("captcha") String captcha, 
                                    @QueryParam("captchaKey") String captchaKey) throws MyBlogException{
            InputStream inputStream = captchaService.confirmCaptcha(captcha, captchaKey);
            if(inputStream == null){
                return Response.ok().build();
            }else{
                return Response.status(Status.BAD_REQUEST).entity(inputStream).build();
            }
    }

    @GET
    @Path("/new")
    @Transactional
    @Consumes(TEXT_PLAIN)
    public Response getNewCaptcha(@QueryParam("captchaKey") String captchaKey) throws MyBlogException{
            return Response.ok(captchaService.createCaptcha(captchaKey)).build();
    }
}