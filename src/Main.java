import auth.AuthService;
import auth.FileAuthRepository;

import domain.Scrim;
import strategy.ByMMRStrategy;
import strategy.ByLatencyStrategy;
import strategy.MatchmakingStrategy;

import event.DomainEventBus;
import notification.NotificationSubscriber;

public class Main {

    public static void main(String[] args) {
        try {
            // start web server (serves web/ and exposes API)
            WebServer srv = new WebServer(9090, 0);
            srv.start();
        } catch (Exception e) {
            System.err.println("No pude iniciar el servidor web: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Extraído para que el WebServer pueda invocar la simulación y capturar su salida
    public static void runSimulation(AuthService auth) {
        // AUTH demo sequence
        System.out.println("=== AUTENTICACIÓN ===");

        String username = "fede";
        String email = "fede@example.com";
        String password = "secret123";

        AuthService.RegisterResult reg = auth.register(username, email, password);
        System.out.println("Register -> " + reg.message);

        AuthService.LoginResult login = auth.login(email, password);
        System.out.println("Login -> " + login.message);

        String sessionToken = null;
        if (login.ok) {
            sessionToken = login.token;
            System.out.println("Usuario: " + login.username + " | Rol: " + login.role);
            System.out.println("Token: " + sessionToken);
            System.out.println("isAuthenticated? " + auth.isAuthenticated(sessionToken));
        } else {
            System.out.println("No se pudo autenticar. Revisá email/contraseña.");
        }

        if (sessionToken != null) {
            auth.logout(sessionToken);
            System.out.println("Logout -> isAuthenticated? " + auth.isAuthenticated(sessionToken));
        }

        // OBSERVER
        System.out.println("\n... Inicializando el sistema de Notificaciones ...");
        NotificationSubscriber notificador = new NotificationSubscriber();
        DomainEventBus.getInstance().subscribe(notificador);
        System.out.println("... Sistema listo. Comenzando simulación ...");

        // STRATEGY
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

        // STATE + OBSERVER + STRATEGY
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
    // Nota: demos automáticos se exponen ahora vía /api/demo en WebServer
    }
}
