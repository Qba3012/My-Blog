package ogorkiewicz.jakub.my_blog.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    NOT_EXIST("Requested resource does not exist", "NotExist"),
    READ_WRITE_ERROR("File read/write operation failed", "ReadWriteError"),
    CAPTCHA_MISMATCH("Captcha does not match the image", "CaptchaMismatch"),
    QUERY_PARAMETERS("Wrong query parameters", "QueryParameters"),
    ALREADY_EXIST("Given data already exist", "AlreadyExist"),
    OUT_OF_INDEX("Selected page number is out of index", "OutOfIndex");

    private String message;
    private String title;

    ErrorCode(String message, String title) {
        this.message = message;
        this.title = title;
    }
}

