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
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "post_image")
public class PostImage {

    @Id
    private Long id;
    @NotNull
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "image_url")
    private URL imageUrl;
    @Column(name = "local_uri")
    private String localUri;
    @Column(name = "image_offset")
    private double imageOffset;
    @Column(name = "image_fit")
    @Enumerated(EnumType.STRING)
    private ImageFit imageFit;
    @OneToOne
    @MapsId
    private Post post;

}