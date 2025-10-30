package domain;
/**
 * Clase que "conecta" a un Usuario con un Scrim.
 * Representa una solicitud para unirse.
 */
public class Postulacion {

    // --- Atributos Privados ---
    private Usuario usuario; // Quién se postula
    private Scrim scrim;     // A qué scrim
    private String rolDeseado;
    private String estado;   // "Pendiente", "Aceptada", "Rechazada" 

    // --- Constructor Vacío ---
    public Postulacion() {
    }

    // --- Getters y Setters ---

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Scrim getScrim() {
        return scrim;
    }

    public void setScrim(Scrim scrim) {
        this.scrim = scrim;
    }

    public String getRolDeseado() {
        return rolDeseado;
    }

    public void setRolDeseado(String rolDeseado) {
        this.rolDeseado = rolDeseado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}