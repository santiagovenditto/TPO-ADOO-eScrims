package auth;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final AuthRepository repo;
    private final ConcurrentHashMap<String, String> sessionsByToken = new ConcurrentHashMap<>();

    public AuthService(AuthRepository repo) {
        this.repo = repo;
    }

    public RegisterResult register(String username, String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.length() < 6) {
            return RegisterResult.fail("Email inválido o contraseña muy corta (mín. 6).");
        }
        if (repo.findByEmail(email).isPresent()) {
            return RegisterResult.fail("Ya existe un usuario con ese email.");
        }
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash(rawPassword, salt);
        UserCredentials user = new UserCredentials(username, email, salt, hash, "USER");
        try {
            repo.save(user);
            return RegisterResult.ok("Usuario registrado.");
        } catch (Exception e) {
            return RegisterResult.fail("No se pudo registrar: " + e.getMessage());
        }
    }

    public LoginResult login(String email, String rawPassword) {
        Optional<UserCredentials> opt = repo.findByEmail(email);
        if (opt.isEmpty()) return LoginResult.fail("Credenciales inválidas.");
        UserCredentials u = opt.get();
        String calc = PasswordUtil.hash(rawPassword, u.getSalt());
        if (!calc.equals(u.getPassHash())) return LoginResult.fail("Credenciales inválidas.");
        String token = UUID.randomUUID().toString();
        sessionsByToken.put(token, u.getEmail());
        return LoginResult.ok(token, u.getUsername(), u.getRole());
    }

    public boolean isAuthenticated(String token) {
        return token != null && sessionsByToken.containsKey(token);
    }

    public void logout(String token) {
        if (token != null) sessionsByToken.remove(token);
    }

    // --- DTOs de resultado ---
    public static class RegisterResult {
        public final boolean ok; public final String message;
        private RegisterResult(boolean ok, String message){ this.ok = ok; this.message = message; }
        public static RegisterResult ok(String m){ return new RegisterResult(true, m); }
        public static RegisterResult fail(String m){ return new RegisterResult(false, m); }
    }

    public static class LoginResult {
        public final boolean ok; public final String message;
        public final String token; public final String username; public final String role;
        private LoginResult(boolean ok, String message, String token, String username, String role){
            this.ok = ok; this.message = message; this.token = token; this.username = username; this.role = role;
        }
        public static LoginResult ok(String token, String username, String role){
            return new LoginResult(true, "Login correcto", token, username, role);
        }
        public static LoginResult fail(String m){ return new LoginResult(false, m, null, null, null); }
    }
}
