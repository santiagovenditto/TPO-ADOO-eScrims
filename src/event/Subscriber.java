package event;

/**
 * Interfaz para el "Observer" o "Suscriptor".
 * Define el método que el Bus llamará cuando ocurra un evento.
 */
public interface Subscriber {
    
    /**
     * Este método es llamado por el DomainEventBus
     * cuando se publica un evento al que está suscrito.
     * @param e El evento que ocurrió.
     */
    void onEvent(DomainEvent e);
}