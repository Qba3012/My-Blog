package ogorkiewicz.jakub.my_blog.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class MyBlogExceptionMapper implements ExceptionMapper<MyBlogException> {

    @Override
    public Response toResponse(MyBlogException e) {

        return Response.status(BAD_REQUEST).header("Content-Type",APPLICATION_JSON).entity(new ExceptionResponse(e)).build();
    }


}

