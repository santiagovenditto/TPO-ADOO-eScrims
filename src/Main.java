import domain.Scrim;
import domain.Usuario;

import strategy.ByMMRStrategy;
import strategy.ByLatencyStrategy;
import strategy.MatchmakingStrategy;

import event.DomainEventBus;

import notification.NotificationSubscriber;

import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        // PATRÓN OBSERVER

        System.out.println("... Inicializando el sistema de Notificaciones ...");
        
        // 1. Creamos nuestro "oyente" de notificaciones
        NotificationSubscriber notificador = new NotificationSubscriber();

        // 2. Lo suscribimos al "colectivo" (Bus) de eventos
        DomainEventBus.getInstance().subscribe(notificador);
        
        System.out.println("... Sistema listo. Comenzando simulación ...");

        // PATRÓN STRATEGY

        System.out.println("\n\n========= INICIO DE LA SIMULACIÓN 1: PROBANDO STRATEGY =========");

        MatchmakingStrategy estrategiaMMR = new ByMMRStrategy();
        MatchmakingStrategy estrategiaLatencia = new ByLatencyStrategy();

        System.out.println("\nCreando Scrim con estrategia MMR...");
        Scrim scrimConMMR = new Scrim(estrategiaMMR);

        scrimConMMR.buscarJugadores();

        System.out.println("\n... El matchmaking por MMR falló, cambiamos a Latencia...");
        scrimConMMR.setStrategy(estrategiaLatencia);

        scrimConMMR.buscarJugadores();

        System.out.println("\n========= FIN DE LA SIMULACIÓN 1 =========");

        // PATRÓN STATE + OBSERVER + STRATEGY

        System.out.println("\n\n========= INICIO DE LA SIMULACIÓN 2: FLUJO DE ESTADOS =========");
        
        System.out.println("\nCreando Scrim para probar el flujo de estados...");
        Scrim scrimDeEstados = new Scrim(new ByMMRStrategy());
        
        System.out.println("\nIntentando 'postular' (debería cambiar a LobbyArmado y disparar evento):");
        scrimDeEstados.postularse();

        System.out.println("\nIntentando 'confirmar' (debería cambiar a Confirmado y disparar evento):");
        scrimDeEstados.confirmarAsistencia();

        System.out.println("\nIntentando 'iniciar' (debería cambiar a EnJuego y disparar evento):");
        scrimDeEstados.iniciarPartida();

        System.out.println("\nIntentando 'finalizar' (debería cambiar a Finalizado y disparar evento):");
        scrimDeEstados.finalizarPartida();

        System.out.println("\n========= FIN DE LA SIMULACIÓN 2 =========");
    }
}