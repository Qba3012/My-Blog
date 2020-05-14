package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Captcha extends PanacheEntity {
    
    public String captcha;
    @Column(name = "captcha_key")
    public String captchaKey;

    public Captcha(String captcha, String captchaKey){
        this.captcha = captcha;
        this.captchaKey = captchaKey;
    }
}