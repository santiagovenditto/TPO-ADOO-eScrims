// Necesitamos 'List' (una colección de cosas)
import java.util.List;

/**
 * Esta es la interfaz "Strategy".
 * Define el contrato que CUALQUIER algoritmo de matchmaking debe cumplir.
 * (Basado en el esqueleto del PDF [cite: 160, 162])
 */
public interface MatchmakingStrategy {

    // El método principal del patrón.
    // Recibe una lista de candidatos y el scrim que busca jugadores,
    // y devuelve la lista de jugadores seleccionados que cumplen la estrategia.
    List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim);
}