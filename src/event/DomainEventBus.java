package event;

import java.util.ArrayList;
import java.util.List;

/**
 * El "Subject" del patrón Observer.
 * Es un "Singleton" para que haya un único bus de eventos en la app.
 * (Basado en el esqueleto del PDF )
 */
public class DomainEventBus {

    // --- Inicio del patrón Singleton ---
    private static final DomainEventBus INSTANCE = new DomainEventBus();

    // Constructor privado para que nadie más pueda crearlo
    private DomainEventBus() {} 

    public static DomainEventBus getInstance() {
        return INSTANCE;
    }
    // --- Fin del patrón Singleton ---

    
    // Lista de todos los "oyentes"
    private final List<Subscriber> subscribers = new ArrayList<>();

    /**
     * Permite que un nuevo "oyente" se suscriba al bus.
     */
    public void subscribe(Subscriber subscriber) {
        this.subscribers.add(subscriber);
        System.out.println("EVENT_BUS: Nuevo suscriptor registrado: " + subscriber.getClass().getSimpleName());
    }

    /**
     * Publica un evento a TODOS los suscriptores registrados.
     */
    public void publish(DomainEvent event) {
        System.out.println("EVENT_BUS: Publicando evento: " + event.getClass().getSimpleName());
        
        // Avisa a cada suscriptor
        for (Subscriber sub : subscribers) {
            sub.onEvent(event);
        }
    }
}