public class CanceladoState implements ScrimState {

    @Override
    public void postular(Scrim scrim) {
        System.out.println("ERROR: No se puede postular, el scrim fue cancelado.");
    }

    @Override
    public void confirmar(Scrim scrim) {
        System.out.println("ERROR: No se puede confirmar, el scrim fue cancelado.");
    }

    @Override
    public void iniciar(Scrim scrim) {
        System.out.println("ERROR: No se puede iniciar, el scrim fue cancelado.");
    }

    @Override
    public void finalizar(Scrim scrim) {
        System.out.println("ERROR: No se puede finalizar, el scrim fue cancelado.");
    }

    @Override
    public void cancelar(Scrim scrim) {
        // Ya no hace nada, ya está en este estado.
        System.out.println("El scrim YA se encuentra cancelado.");
    }
}