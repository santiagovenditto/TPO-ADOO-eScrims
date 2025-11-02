package auth;

import java.util.Objects;
import java.util.UUID;

public class UserCredentials {
    private final String id = UUID.randomUUID().toString();
    private String username;
    private String email;
    private String salt;
    private String passHash;
    private String role;

    public UserCredentials(String username, String email, String salt, String passHash, String role) {
        this.username = username;
        this.email = email.toLowerCase();
        this.salt = salt;
        this.passHash = passHash;
        this.role = role;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getSalt() { return salt; }
    public String getPassHash() { return passHash; }
    public String getRole() { return role; }

    public void setPassHash(String passHash) { this.passHash = passHash; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setRole(String role) { this.role = role; }

    // CSV
    public String toCsv() {
        return String.join(";", escape(id), escape(username), escape(email),
                escape(salt), escape(passHash), escape(role));
    }

    public static UserCredentials fromCsv(String line) {
        String[] p = line.split(";", -1);
        if (p.length < 6) throw new IllegalArgumentException("Bad CSV line");
        return new UserCredentials(unescape(p[1]), unescape(p[2]), unescape(p[3]), unescape(p[4]), unescape(p[5]));
    }

    private static String escape(String s) { return s == null ? "" : s.replace(";", "\\;"); }
    private static String unescape(String s) { return s.replace("\\;", ";"); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCredentials)) return false;
        UserCredentials that = (UserCredentials) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() { return Objects.hash(email); }
}
