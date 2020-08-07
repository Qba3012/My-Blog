package ogorkiewicz.jakub.my_blog.repository;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import ogorkiewicz.jakub.my_blog.model.Captcha;

@ApplicationScoped
public class CaptchaRepository implements PanacheRepository<Captcha> {

    public Captcha getCaptchaByKey(String captchaKey) {
        return find("captcha_key", captchaKey).firstResult();
    }
}