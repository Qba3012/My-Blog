package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.MappedSuperclass;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor
public class ConfirmationToken extends PanacheEntity {
    
    public String token;

    public ConfirmationToken(String token) {
        this.token = token;
    }
    

}