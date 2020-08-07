package ogorkiewicz.jakub.my_blog.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String content;
    @NotNull
    @Size(min = 1,max = 50)
    @Column(length = 50)
    private String email;
    @JsonIgnore
    @Column(name = "is_confirmed",nullable = false)
    private boolean isConfirmed;
    @Column(name = "create_date",nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createDate;
    @NotNull
    @Column(name = "post_id")
    private Long postId;

    @PrePersist
    private void setInitialData(){
        this.createDate = OffsetDateTime.now();
        this.isConfirmed = false;
    }

}