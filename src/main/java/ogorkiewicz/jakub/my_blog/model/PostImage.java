package ogorkiewicz.jakub.my_blog.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URL;
import java.util.List;

@Entity
@Table(name = "post_image")
public class PostImage extends PanacheEntity {

    @NotNull
    @Column(name="file_name")
    public String fileName;
    @Column(name = "image_url")
    public URL imageUrl;
    @Column(name = "local_uri")
    public URI localUri;
    @Column(name = "image_offset")
    public double imageOffset;
    @Column(name = "image_fit")
    @Enumerated(EnumType.STRING)
    public ImageFit imageFit;
    @OneToOne
    @MapsId
    public Post post;

    public static List<PostImage> findByIdList(List<Long> postIds){
        return find( "id in (:postIds)", Parameters.with("postIds",postIds)).list();
    }
}
