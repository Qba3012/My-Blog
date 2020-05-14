package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "comment_token")
public class CommentToken extends ConfirmationToken{

    @OneToOne
    @MapsId
    public Comment comment;

    public CommentToken(String token, Comment comment) {
        super(token);
        this.comment = comment;
    }

}
