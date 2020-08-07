package ogorkiewicz.jakub.my_blog.repository;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import ogorkiewicz.jakub.my_blog.model.PostLike;

@ApplicationScoped
public class PostLikeRepository implements PanacheRepository<PostLike> {

    public long countLikes(Long postId) {
        return count("post_id", postId);
    }

    public PostLike findLike(String email, Long postId) {
        return find("email=?1 and post_id=?2", email, postId).firstResult();
    }
}