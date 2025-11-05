package domain;
import java.util.List;
import java.util.Map;

/**
 * Clase del Modelo de Dominio (POJO / Entity) que representa al Usuario.
 * Basado en el Punto 6 del PDF.
 */
public class Usuario {
    private String username;
    private String email;
    private String passwordHash;
    private String region;
    private Map<String, String> rangoPorJuego; // Ej: <"Valorant", "Diamante">
    private List<String> rolesPreferidos;  // Ej: ["Duelist", "Support"]
    private Integer ranking; // valor numérico para emparejamiento (antes llamado MMR)

    
    public Usuario() {
        // El constructor vacío es requerido por JPA/ORM
    }

    
    // --- Getters y Setters Públicos ---
    // Hacé clic derecho -> Source Action... -> Generate Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Map<String, String> getRangoPorJuego() {
        return rangoPorJuego;
    }

    public void setRangoPorJuego(Map<String, String> rangoPorJuego) {
        this.rangoPorJuego = rangoPorJuego;
    }

    public List<String> getRolesPreferidos() {
        return rolesPreferidos;
    }

    public void setRolesPreferidos(List<String> rolesPreferidos) {
        this.rolesPreferidos = rolesPreferidos;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    @Override
    public String toString() {
        return "Usuario: " + this.username;
    }
}