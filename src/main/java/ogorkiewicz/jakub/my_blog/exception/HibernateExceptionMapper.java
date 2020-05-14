package ogorkiewicz.jakub.my_blog.exception;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class HibernateExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {

        return Response.status(BAD_REQUEST).header("Content-Type","application/json").entity(new ExceptionResponse(e)).build();
    }
}
