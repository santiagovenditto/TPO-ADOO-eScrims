// Esta es la implementación del primer estado
// "implements ScrimState" significa que "firma el contrato" y
// está OBLIGADO a tener los 5 métodos.
public class BuscandoState implements ScrimState {

    @Override
public void postular(Scrim scrim) {
    System.out.println("Un jugador se ha postulado.");
    
    boolean simulacion_lobby_lleno = true;

    if (simulacion_lobby_lleno) {
        // Ahora Java sí encuentra la clase LobbyArmadoState que acabamos de crear.
        scrim.setState(new LobbyArmadoState()); 
    }
}

    @Override
    public void confirmar(Scrim scrim) {
        // NO podés confirmar un lobby que todavía se está armando.
        System.out.println("ERROR: No se puede confirmar, aún estamos buscando jugadores.");
    }

    @Override
    public void iniciar(Scrim scrim) {
        // NO podés iniciar un lobby que ni siquiera está armado.
        System.out.println("ERROR: No se puede iniciar, aún estamos buscando jugadores.");
    }

    @Override
    public void finalizar(Scrim scrim) {
        // No tiene sentido finalizar algo que no empezó.
        System.out.println("ERROR: No se puede finalizar, la partida no ha comenzado.");
    }

    @Override
public void cancelar(Scrim scrim) {
    System.out.println("El scrim ha sido CANCELADO por el organizador.");
    scrim.setState(new CanceladoState());
}
}