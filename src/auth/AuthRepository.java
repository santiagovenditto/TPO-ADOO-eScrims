package auth;

import java.util.Optional;

public interface AuthRepository {
    Optional<UserCredentials> findByEmail(String email);
    void save(UserCredentials user) throws Exception;
}
