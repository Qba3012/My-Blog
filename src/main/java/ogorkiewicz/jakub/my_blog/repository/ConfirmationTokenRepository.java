package ogorkiewicz.jakub.my_blog.repository;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import ogorkiewicz.jakub.my_blog.model.ConfirmationToken;

@ApplicationScoped
public class ConfirmationTokenRepository implements PanacheRepository<ConfirmationToken> {

    public ConfirmationToken findByToken(String token, Class<?> type) {
        String query = String.format("from %s where token = ?1", type.getSimpleName());
        return find(query, token).firstResult();
    }

    public void deleteToken(String token, Class<?> type) {
        String query = String.format("from %s where token = ?1", type.getSimpleName());
        delete(query, token);
    }
}