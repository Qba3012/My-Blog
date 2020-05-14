package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;


@Entity
@Table(name = "post_like")
public class PostLike extends PanacheEntity{

    @Column(nullable=false)
    public String email;
    @Column(nullable=false,name = "post_id")
    public Long postId;
  
}