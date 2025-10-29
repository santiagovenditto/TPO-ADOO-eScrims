// Esta es la clase principal que "guarda" el estado actual.
public class Scrim {

    // "state" es LA variable que dice en qué estado estamos
    private ScrimState state;

    // Esto se llama "Constructor".
    // Cuando creamos un Scrim NUEVO, su estado inicial SIEMPRE
    // será "BuscandoState". Aca definimos el inicio del ciclo.
    public Scrim() {
        this.state = new BuscandoState();
    }

    // --- Métodos de delegación ---
    // Scrim no decide qué hacer, le pregunta a su estado actual.

    public void postularse() {
        // Delega la lógica al estado actual
        state.postular(this);
    }

    public void confirmarAsistencia() {
        state.confirmar(this);
    }

    public void iniciarPartida() {
        state.iniciar(this);
    }

    public void finalizarPartida() {
        state.finalizar(this);
    }

    public void cancelarPartida() {
        state.cancelar(this);
    }

    // --- IMPORTANTE ---
    // Este método permite a los ESTADOS cambiar el estado del Scrim.
    // Así es como pasamos de "Buscando" a "LobbyArmado"
    public void setState(ScrimState newState) {
        this.state = newState;
        System.out.println("--- El estado del Scrim cambió a: " + newState.getClass().getSimpleName() + " ---");
    }
}