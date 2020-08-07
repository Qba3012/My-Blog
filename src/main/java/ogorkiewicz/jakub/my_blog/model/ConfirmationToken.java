package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor
@Getter
public class ConfirmationToken {
    
    @Id
    private Long id;
    private String token;

    public ConfirmationToken(String token) {
        this.token = token;
    }
}