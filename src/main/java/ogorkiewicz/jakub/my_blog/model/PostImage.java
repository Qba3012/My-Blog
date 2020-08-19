package ogorkiewicz.jakub.my_blog.model;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "post_image")
public class PostImage {

    @Id
    private Long id;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "image_url", nullable = false)
    private URL imageUrl;
    @Column(name = "local_uri", nullable = false)
    private String localUri;
    @Column(name = "image_offset", nullable = false)
    private double imageOffset;
    @Column(name = "image_fit", nullable  = false)
    @Enumerated(EnumType.STRING)
    private ImageFit imageFit;
    @OneToOne
    @MapsId
    private Post post;

}