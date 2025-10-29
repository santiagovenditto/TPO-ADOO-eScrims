import java.util.List;
import java.util.ArrayList;

public class ByLatencyStrategy implements MatchmakingStrategy {

    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        System.out.println("--- LOGICA DE MATCHMAKING: Seleccionando jugadores por Latencia... ---");
        return new ArrayList<>();
    }
}