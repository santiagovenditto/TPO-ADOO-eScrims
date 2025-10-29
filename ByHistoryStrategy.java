import java.util.List;
import java.util.ArrayList;

public class ByHistoryStrategy implements MatchmakingStrategy {

    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        System.out.println("--- LOGICA DE MATCHMAKING: Seleccionando jugadores por Historial/Compatibilidad... ---");
        return new ArrayList<>();
    }
}