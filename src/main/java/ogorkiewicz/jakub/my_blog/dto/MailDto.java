package ogorkiewicz.jakub.my_blog.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class MailDto {

    private String subject;
    @NotNull
    @Size(min = 1,max = 1000)
    private String message;
    @NotNull
    @Size(min = 1,max = 50)
    private String email;

    public void setSubject(String subject){
        this.subject = subject;
    }
}
