package ogorkiewicz.jakub.my_blog.validator;

import ogorkiewicz.jakub.my_blog.constraint.FileName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameValidator implements ConstraintValidator<FileName,String> {

    private final String validFileNamePattern = "^[\\w]+\\.[A-Za-z0-9]{3,4}$";

    @Override
    public boolean isValid(String fileName, ConstraintValidatorContext constraintValidatorContext) {
        Pattern pattern = Pattern.compile(validFileNamePattern);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }
}
