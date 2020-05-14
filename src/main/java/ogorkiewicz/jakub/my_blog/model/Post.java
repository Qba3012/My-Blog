package ogorkiewicz.jakub.my_blog.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Post extends PanacheEntity {

    @Column(length = 20,nullable = false)
    public String title;
    @Column(length = 1000,nullable = false)
    public String content;
    @Column(length = 50,nullable = false)
    public String email;
    @Column(name = "is_confirmed")
    public boolean isConfirmed;
    @Column(name = "create_date",nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    public OffsetDateTime createDate;

    @PrePersist
    private void setInitialData(){
        this.createDate = OffsetDateTime.now();
        this.isConfirmed = false;
    }

    public void setTitle(String title) {
        this.title = title.toUpperCase();
    }
}
