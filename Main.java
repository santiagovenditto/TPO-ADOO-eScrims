
// Clase principal para probar el Patrón State.
// Aquí simulamos las acciones sobre un objeto Scrim
// para ver cómo cambia de estado.

public class Main {

    public static void main(String[] args) {

        System.out.println("========= INICIO DE LA SIMULACIÓN 1: FLUJO FELIZ =========");
        
        // 1. Creamos un Scrim.
        // El constructor de Scrim automáticamente lo pone en "BuscandoState".
        Scrim scrimFeliz = new Scrim();
        
        // 2. Intentamos una acción inválida en este estado (confirmar)
        System.out.println("\nIntentando 'confirmar' (debería fallar):");
        scrimFeliz.confirmarAsistencia();

        // 3. Hacemos la acción válida: postularse.
        // (En BuscandoState, simulamos que esto llena el lobby)
        System.out.println("\nIntentando 'postular' (debería funcionar y cambiar a LobbyArmado):");
        scrimFeliz.postularse();

        // 4. Intentamos una acción inválida en el nuevo estado (postular)
        System.out.println("\nIntentando 'postular' de nuevo (debería fallar):");
        scrimFeliz.postularse();

        // 5. Hacemos la acción válida: confirmar.
        // (En LobbyArmadoState, simulamos que esto confirma a todos)
        System.out.println("\nIntentando 'confirmar' (debería funcionar y cambiar a Confirmado):");
        scrimFeliz.confirmarAsistencia();

        // 6. Hacemos la acción válida: iniciar.
        System.out.println("\nIntentando 'iniciar' (debería funcionar y cambiar a EnJuego):");
        scrimFeliz.iniciarPartida();

        // 7. Hacemos la acción válida: finalizar.
        System.out.println("\nIntentando 'finalizar' (debería funcionar y cambiar a Finalizado):");
        scrimFeliz.finalizarPartida();

        // 8. Intentamos hacer algo en un estado final
        System.out.println("\nIntentando 'iniciar' de nuevo (debería fallar):");
        scrimFeliz.iniciarPartida();

        System.out.println("\n========= FIN DE LA SIMULACIÓN 1 =========");


        System.out.println("\n\n========= INICIO DE LA SIMULACIÓN 2: FLUJO DE CANCELACIÓN =========");
        
        // 1. Creamos otro Scrim.
        Scrim scrimTriste = new Scrim();

        // 2. Pasa a Lobby Armado
        System.out.println("\nIntentando 'postular' (para llegar a LobbyArmado):");
        scrimTriste.postularse();

        // 3. El organizador lo cancela
        System.out.println("\nIntentando 'cancelar' (debería funcionar y cambiar a Cancelado):");
        scrimTriste.cancelarPartida();

        // 4. Intentamos postularnos a un scrim cancelado
        System.out.println("\nIntentando 'postular' (debería fallar):");
        scrimTriste.postularse();

        System.out.println("\n========= FIN DE LA SIMULACIÓN 2 =========");
    }
}