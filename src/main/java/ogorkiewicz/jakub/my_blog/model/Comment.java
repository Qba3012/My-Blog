package ogorkiewicz.jakub.my_blog.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.ToString;

@Entity
@ToString
public class Comment extends PanacheEntity {

    @NotNull
    public String content;
    @NotNull
    @Size(min = 1,max = 50)
    @Column(length = 50)
    public String email;
    @JsonIgnore
    @Column(name = "is_confirmed",nullable = false)
    public boolean isConfirmed;
    @Column(name = "create_date",nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    public OffsetDateTime createDate;
    @NotNull
    @Column(name = "post_id")
    public Long postId;

    @PrePersist
    private void setInitialData(){
        this.createDate = OffsetDateTime.now();
        this.isConfirmed = false;
    }

}
