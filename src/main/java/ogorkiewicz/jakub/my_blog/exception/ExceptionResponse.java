package ogorkiewicz.jakub.my_blog.exception;

import lombok.Value;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Value
public class ExceptionResponse{
    String errorCode;
    String message;

    public ExceptionResponse(MyBlogException e) {
        this.errorCode = e.getType().getSimpleName() + "." + e.getErrorCode().getTitle();
        this.message = e.getMessage();
    }

    public ExceptionResponse(ConstraintViolationException e){
        ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
        this.errorCode =
                violation.getLeafBean().getClass().getSimpleName() + "."
                        + violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        String[] pathElements = violation.getPropertyPath().toString().split("\\.");
        this.message = pathElements[pathElements.length-1] + " - " + violation.getMessage();
    }
}

