package state;

import domain.Scrim;

public class EnJuegoState implements ScrimState {

    @Override
    public void postular(Scrim scrim) {
        System.out.println("ERROR: No se puede postular, la partida ya está en juego.");
    }

    @Override
    public void confirmar(Scrim scrim) {
        System.out.println("ERROR: No se puede confirmar, la partida ya está en juego.");
    }

    @Override
    public void iniciar(Scrim scrim) {
        System.out.println("ERROR: No se puede iniciar, la partida YA está en juego.");
    }

    @Override
public void finalizar(Scrim scrim) {
    System.out.println("La partida ha terminado. Pasando a 'Finalizado'.");
    
    // ¡Transición al estado final!
    scrim.setState(new FinalizadoState()); 
}

    @Override
    public void cancelar(Scrim scrim) {
        // Una partida en juego no se "cancela", se "finaliza"
        System.out.println("ERROR: No se puede cancelar una partida en juego. Debes finalizarla.");
    }
}