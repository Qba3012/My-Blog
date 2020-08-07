package ogorkiewicz.jakub.my_blog.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import ogorkiewicz.jakub.my_blog.model.PostImage;

@ApplicationScoped
public class PostImageRepository implements PanacheRepository<PostImage> {

    public List<PostImage> findByIdList(List<Long> postIds) {
        return find("id in (:postIds)", Parameters.with("postIds", postIds)).list();
    }
}