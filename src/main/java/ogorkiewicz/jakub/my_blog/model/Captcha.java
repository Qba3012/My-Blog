package ogorkiewicz.jakub.my_blog.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Captcha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String captcha;
    @Column(name = "captcha_key")
    private String captchaKey;

    public Captcha(String captcha, String captchaKey) {
        this.captcha = captcha;
        this.captchaKey = captchaKey;
    }

}