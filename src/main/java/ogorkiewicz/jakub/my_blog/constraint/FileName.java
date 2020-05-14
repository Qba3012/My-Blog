package ogorkiewicz.jakub.my_blog.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import ogorkiewicz.jakub.my_blog.validator.FileNameValidator;

@Constraint(validatedBy = FileNameValidator.class)
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileName {
    
    String message() default "File name must be valid name with extension and no special signs";
    Class<?>[] groups() default {};
    Class<? extends Payload> [] payload() default {};

}