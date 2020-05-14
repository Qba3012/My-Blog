package ogorkiewicz.jakub.my_blog.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class MyBlogException extends Exception {

        private ErrorCode errorCode;
        private Class type;

        public MyBlogException(ErrorCode errorCode,Class type) {
            super(errorCode.getMessage());
            this.errorCode = errorCode;
            this.type = type;
        }
}
