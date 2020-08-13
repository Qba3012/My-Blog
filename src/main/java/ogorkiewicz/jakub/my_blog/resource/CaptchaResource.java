package ogorkiewicz.jakub.my_blog.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static ogorkiewicz.jakub.my_blog.resource.CaptchaResource.CAPTCHA_PATH;

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

import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Captcha;
import ogorkiewicz.jakub.my_blog.service.CaptchaService;

@Path(CAPTCHA_PATH)
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CaptchaResource {

    private CaptchaService captchaService;
    public final static String CAPTCHA_PATH = "/captcha";
   
    @POST
    @Path("/validate")
    @Transactional
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public Response confirmCaptcha(@QueryParam("captcha") String captcha, 
                                    @QueryParam("captchaKey") String captchaKey) throws MyBlogException {
        InputStream inputStream = captchaService.confirmCaptcha(captcha, captchaKey);
        if(inputStream == null){
            return Response.ok().build();
        }else{
            return Response.status(BAD_REQUEST).entity(inputStream).build();
        }    
    }

    @GET
    @Path("/new")
    @Transactional
    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response getNewCaptcha(@QueryParam("captchaKey") String captchaKey) throws MyBlogException{
        if(captchaKey == null) {
            throw new MyBlogException(ErrorCode.CAPTCHA_KEY_NULL, Captcha.class);
        }
        return Response.ok(captchaService.createCaptcha(captchaKey)).build();
    }
}