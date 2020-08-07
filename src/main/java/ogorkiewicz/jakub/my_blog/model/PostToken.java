package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post_token")
public class PostToken extends ConfirmationToken {

    @OneToOne
    @MapsId
    private Post post;

    public PostToken(String token, Post post) {
        super(token);
        this.post = post;
    }

}