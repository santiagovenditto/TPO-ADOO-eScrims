package notification;

import event.DomainEvent;
import event.Subscriber;
import event.ScrimStateChanged;

/**
 * Este es el Observer concreto.
 * Se suscribe al Bus de Eventos y reacciona
 * a los eventos que le interesan.
 */
public class NotificationSubscriber implements Subscriber {

    // Este método es el que nos obliga la interfaz Subscriber
    @Override
    public void onEvent(DomainEvent e) {
        
        // 1. Chequeamos si el evento que nos llegó es el que nos interesa
        if (e instanceof ScrimStateChanged) {
            
            // 2. Si lo es, casteamos el evento para poder leer sus datos
            ScrimStateChanged evento = (ScrimStateChanged) e;

            // 3. ¡Aquí ocurre la lógica de notificación!
            System.out.println("--- NOTIFICADOR ---");
            System.out.println("¡Evento recibido! El Scrim " + evento.scrimId());
            System.out.println("Cambió al estado: " + evento.nuevoEstado());
            
            // 4. Llamar a la Abstract Factory
            //    dependiendo del nuevo estado.
            if (evento.nuevoEstado().equals("LobbyArmadoState")) {
                System.out.println("Acción: Enviar notificaciones de 'Lobby Lleno'...");
                // Aquí es donde llamaremos a la Factory para enviar
                // el Email, Push, Discord, etc.
            }
            System.out.println("-------------------");
        }
        
        // (Si el evento fuera de otro tipo, ej: "UsuarioCreado",
        //  simplemente lo ignoramos)
    }
}