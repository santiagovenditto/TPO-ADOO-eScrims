
// Segundo estado del ciclo de vida.
// El cupo está lleno, esperando confirmación de los jugadores.

public class LobbyArmadoState implements ScrimState {

    @Override
    public void postular(Scrim scrim) {
        // En este estado, el lobby YA ESTÁ LLENO.
        System.out.println("ERROR: No se puede postular, el lobby está completo.");
    }

    @Override
public void confirmar(Scrim scrim) {
    System.out.println("Un jugador ha CONFIRMADO su asistencia.");
    
    boolean simulacion_todos_confirmaron = true;

    if (simulacion_todos_confirmaron) {
        scrim.setState(new ConfirmadoState());
    }
}
    @Override
    public void iniciar(Scrim scrim) {
        // Aún no se puede iniciar, faltan confirmaciones.
        System.out.println("ERROR: No se puede iniciar, faltan confirmaciones de jugadores.");
    }

    @Override
    public void finalizar(Scrim scrim) {
        // No tiene sentido finalizar algo que no empezó.
        System.out.println("ERROR: No se puede finalizar, la partida no ha comenzado.");
    }

    @Override
public void cancelar(Scrim scrim) {
    System.out.println("El scrim ha sido CANCELADO (desde el lobby).");
    
    // Transición al estado cancelado.
    scrim.setState(new CanceladoState()); 
}
}