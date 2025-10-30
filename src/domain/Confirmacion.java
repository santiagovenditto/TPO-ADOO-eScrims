package domain;
/**
 * Registra si un usuario confirmó su participación
 * en el estado "LobbyArmado".
 */
public class Confirmacion {

    // --- Atributos Privados ---
    private Usuario usuario;
    private Scrim scrim;
    private boolean confirmado; // true o false 

    // --- Constructor Vacío ---
    public Confirmacion() {
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

    public boolean isConfirmado() { // Para 'boolean' es 'is' en vez de 'get'
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }
}