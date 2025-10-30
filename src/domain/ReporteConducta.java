package domain;

/**
 * Representa un reporte de un usuario contra otro.
 */
public class ReporteConducta {

    // --- Atributos Privados ---
    private Scrim scrim;
    private Usuario reportado;
    private Usuario reportador;
    private String motivo;
    private String estado; // "Pendiente", "Resuelto", "Descartado"
    private String sancion;

    // --- Constructor Vacío ---
    public ReporteConducta() {
    }

    // --- Getters y Setters ---

    public Scrim getScrim() {
        return scrim;
    }

    public void setScrim(Scrim scrim) {
        this.scrim = scrim;
    }

    public Usuario getReportado() {
        return reportado;
    }

    public void setReportado(Usuario reportado) {
        this.reportado = reportado;
    }

    public Usuario getReportador() {
        return reportador;
    }

    public void setReportador(Usuario reportador) {
        this.reportador = reportador;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSancion() {
        return sancion;
    }

    public void setSancion(String sancion) {
        this.sancion = sancion;
    }
}