package domain;
/**
 * Guarda el resultado de un jugador en un Scrim finalizado.
 */
public class Estadistica {

    // --- Atributos Privados ---
    private Scrim scrim;
    private Usuario usuario;
    private boolean mvp;
    private String kda; // Kills/Deaths/Assists, ej: "10/2/5"
    private String observaciones;

    // --- Constructor Vacío ---
    public Estadistica() {
    }

    // --- Getters y Setters ---
    
    public Scrim getScrim() {
        return scrim;
    }

    public void setScrim(Scrim scrim) {
        this.scrim = scrim;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isMvp() {
        return mvp;
    }

    public void setMvp(boolean mvp) {
        this.mvp = mvp;
    }

    public String getKda() {
        return kda;
    }

    public void setKda(String kda) {
        this.kda = kda;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}