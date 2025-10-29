import java.util.List;
import java.util.ArrayList;


// Clase principal para probar los Patrones State y Strategy.

public class Main {

    public static void main(String[] args) {

        System.out.println("========= INICIO DE LA SIMULACIÓN 1: PROBANDO STRATEGY =========");

        // 1. Creamos las ESTRATEGIAS que vamos a usar
        MatchmakingStrategy estrategiaMMR = new ByMMRStrategy();
        MatchmakingStrategy estrategiaLatencia = new ByLatencyStrategy();

        // 2. Creamos un Scrim NUEVO, pasándole la estrategia de MMR.
        // (Nota: el constructor de Scrim fue modificado)
        System.out.println("\nCreando Scrim con estrategia MMR...");
        Scrim scrimConMMR = new Scrim(estrategiaMMR);

        // 3. Usamos la estrategia para buscar jugadores.
        // El Scrim usará la estrategia que tiene guardada (MMR).
        scrimConMMR.buscarJugadores();

        // 4. CAMBIAMOS la estrategia del Scrim "en caliente".
        System.out.println("\n... El matchmaking por MMR falló, cambiamos a Latencia...");
        scrimConMMR.setStrategy(estrategiaLatencia);

        // 5. Volvemos a buscar jugadores.
        // Ahora el Scrim usará la NUEVA estrategia (Latencia).
        scrimConMMR.buscarJugadores();


        System.out.println("\n========= FIN DE LA SIMULACIÓN 1 =========");


        System.out.println("\n\n========= INICIO DE LA SIMULACIÓN 2: FLUJO DE ESTADOS (STATE) =========");
        
        // 1. Creamos un Scrim para probar el Patrón State.
        // (Le pasamos una estrategia cualquiera, ej: MMR, para que el constructor funcione)
        Scrim scrimDeEstados = new Scrim(new ByMMRStrategy());
        
        // 2. Hacemos la acción válida: postularse.
        System.out.println("\nIntentando 'postular' (debería funcionar y cambiar a LobbyArmado):");
        scrimDeEstados.postularse();

        // 3. Hacemos la acción válida: confirmar.
        System.out.println("\nIntentando 'confirmar' (debería funcionar y cambiar a Confirmado):");
        scrimDeEstados.confirmarAsistencia();

        // 4. Hacemos la acción válida: iniciar.
        System.out.println("\nIntentando 'iniciar' (debería funcionar y cambiar a EnJuego):");
        scrimDeEstados.iniciarPartida();

        // 5. Hacemos la acción válida: finalizar.
        System.out.println("\nIntentando 'finalizar' (debería funcionar y cambiar a Finalizado):");
        scrimDeEstados.finalizarPartida();

        System.out.println("\n========= FIN DE LA SIMULACIÓN 2 =========");
    }
}