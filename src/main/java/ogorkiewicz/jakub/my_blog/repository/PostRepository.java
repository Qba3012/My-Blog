package ogorkiewicz.jakub.my_blog.repository;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import ogorkiewicz.jakub.my_blog.model.Post;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {

    public PanacheQuery<Post> findConfirmed() {
        return find("is_confirmed", true);
    }

    public void confirmPost(Long tokenId) {
        update("is_confirmed = true where id =?1", tokenId);
    }
}