package domain;
/**
 * Representa un mensaje a ser enviado a un usuario.
 */
public class Notificacion {

    // --- Atributos Privados ---
    private String tipo;   // Ej: "LOBBY_LLENO", "PARTIDA_INICIA"
    private String canal;  // Ej: "EMAIL", "PUSH", "DISCORD" 
    private String payload; // El contenido del mensaje
    private String estado; // Ej: "PENDIENTE", "ENVIADO", "FALLIDO"

    // --- Constructor Vacío ---
    public Notificacion() {
    }

    // --- Getters y Setters ---
    
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}