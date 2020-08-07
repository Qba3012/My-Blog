package ogorkiewicz.jakub.my_blog.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import ogorkiewicz.jakub.my_blog.model.Comment;

@ApplicationScoped
public class CommentRepository implements PanacheRepository<Comment> {

    public List<Comment> getCommentsByPostId(Long postId){
        return find("is_confirmed = true and post_id = ?1", Sort.descending("create_date"),postId).list();
    }

    public long getCommentCount(Long postId){
        return find("post_id=?1 and is_confirmed=true",postId).count();
    }

    public void confirmComment(Long tokenId){
        update("is_confirmed = true where id =?1", tokenId);
    }

    public void delteNotConfirmed(){
        delete("is_confirmed",false);
    }

    public void deleteByPostId(Long postId){
        delete("post_id", postId);
    }
}