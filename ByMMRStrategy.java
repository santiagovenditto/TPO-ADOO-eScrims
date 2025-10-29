import java.util.List;
import java.util.ArrayList; // Usamos ArrayList como una implementación de List

public class ByMMRStrategy implements MatchmakingStrategy {

    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        System.out.println("--- LOGICA DE MATCHMAKING: Seleccionando jugadores por MMR... ---");
        return new ArrayList<>(); 
    }
}