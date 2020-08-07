package ogorkiewicz.jakub.my_blog.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20,nullable = false)
    private String title;
    @Column(length = 1000,nullable = false)
    private String content;
    @Column(length = 50,nullable = false)
    private String email;
    @Column(name = "is_confirmed")
    private boolean isConfirmed;
    @Column(name = "create_date",nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createDate;

    @PrePersist
    private void setInitialData(){
        this.createDate = OffsetDateTime.now();
        this.isConfirmed = false;
    }

    public void setTitle(String title) {
        this.title = title.toUpperCase();
    }
}