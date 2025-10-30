package state;

import domain.Scrim;

public class FinalizadoState implements ScrimState {

    @Override
    public void postular(Scrim scrim) {
        System.out.println("ERROR: No se puede postular, el scrim ya finalizó.");
    }

    @Override
    public void confirmar(Scrim scrim) {
        System.out.println("ERROR: No se puede confirmar, el scrim ya finalizó.");
    }

    @Override
    public void iniciar(Scrim scrim) {
        System.out.println("ERROR: No se puede iniciar, el scrim ya finalizó.");
    }

    @Override
    public void finalizar(Scrim scrim) {
        // Ya no hace nada, ya está en este estado.
        System.out.println("El scrim YA se encuentra finalizado.");
    }

    @Override
    public void cancelar(Scrim scrim) {
        System.out.println("ERROR: No se puede cancelar un scrim que ya finalizó.");
    }
    
    // Aquí es donde podrías agregar lógica para cargar estadísticas,
    // pero esa lógica no es parte de la interfaz ScrimState.
}