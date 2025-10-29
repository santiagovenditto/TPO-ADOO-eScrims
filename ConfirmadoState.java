//Tercer estado del ciclo de vida.
//Todos los jugadores confirmaron. El scrim está "lockeado",
//y esperando la fecha/hora de inicio para pasar a "En Juego".
public class ConfirmadoState implements ScrimState {

    @Override
    public void postular(Scrim scrim) {
        System.out.println("ERROR: No se puede postular, el scrim ya está confirmado.");
    }

    @Override
    public void confirmar(Scrim scrim) {
        System.out.println("ERROR: No se puede confirmar, el scrim ya está confirmado por todos.");
    }

    @Override
public void iniciar(Scrim scrim) {
    System.out.println("¡El Scrim ha comenzado! Pasando a estado 'En Juego'.");
    
    // ¡Transición al nuevo estado!
    scrim.setState(new EnJuegoState());
}

    @Override
    public void finalizar(Scrim scrim) {
        System.out.println("ERROR: No se puede finalizar, la partida primero debe iniciarse.");
    }

    @Override
public void cancelar(Scrim scrim) {
    System.out.println("El scrim ha sido CANCELADO (estaba confirmado).");
    
    // ¡Transición al estado cancelado!
    scrim.setState(new CanceladoState()); 
}
}