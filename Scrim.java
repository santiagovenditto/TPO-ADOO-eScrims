import java.util.List; // --- NUEVO ---: Necesario para manejar listas de usuarios
import java.util.ArrayList; // --- NUEVO ---: Lo usamos para crear listas

/**
 * Esta es la clase principal que "guarda" el estado actual (Patrón State)
 * y también "tiene" una estrategia de matchmaking (Patrón Strategy).
 */
public class Scrim {
    
    private ScrimState state;
    private MatchmakingStrategy strategy;

    /**
     * --- Constructor ---
     * Cuando creamos un Scrim, ahora no solo definimos su estado inicial,
     * sino que también le decimos qué estrategia de matchmaking debe usar.
     */
    public Scrim(MatchmakingStrategy strategyInicial) {
        this.state = new BuscandoState(); // Estado inicial
        this.strategy = strategyInicial; // Asignamos la estrategia
        System.out.println("Nuevo Scrim Creado. Estado: Buscando. Estrategia: " + strategyInicial.getClass().getSimpleName());
    }

    // --- NUEVO: Setter para cambiar la estrategia ---
    /**
     * Permite cambiar la estrategia de matchmaking "en caliente".
     * Por ejemplo, si una búsqueda por MMR falla, podemos cambiar a Latencia.
     */
    public void setStrategy(MatchmakingStrategy nuevaStrategy) {
        this.strategy = nuevaStrategy;
        System.out.println("!!! Estrategia de Matchmaking cambiada a: " + nuevaStrategy.getClass().getSimpleName() + " !!!");
    }

    // --- NUEVO: Método para USAR la estrategia ---
    /**
     * Este es el método que "dispara" el matchmaking.
     * Llama al método .seleccionar() de la ESTRATEGIA ACTUAL.
     */
    public void buscarJugadores() {
        System.out.println("\nIniciando búsqueda de jugadores...");
        
        // 1. Creamos una lista "dummy" (falsa) de candidatos para probar
        List<Usuario> candidatos = new ArrayList<>();
        candidatos.add(new Usuario());
        candidatos.add(new Usuario());
        
        // 2. ¡Aquí ocurre la magia del Patrón Strategy!
        // No sabemos QUÉ algoritmo se usa, solo lo llamamos.
        // Puede ser ByMMRStrategy, ByLatencyStrategy, etc.
        List<Usuario> seleccionados = this.strategy.seleccionar(candidatos, this);
        
        System.out.println("Búsqueda finalizada. Jugadores seleccionados: " + seleccionados.size());
    }


    // Métodos del Patrón STATE


    public void postularse() {
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

    public void setState(ScrimState newState) {
        this.state = newState;
        System.out.println("--- El estado del Scrim cambió a: " + newState.getClass().getSimpleName() + " ---");
    }
}