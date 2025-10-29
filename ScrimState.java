// Esto define una plantilla para todos los estados.
// Cualquier clase que sea un "Estado" DEBE tener estos 5 métodos.
public interface ScrimState {
    
    // Qué pasa cuando alguien intenta postularse
    // Le pasamos el "scrim" (el contexto) para que el estado pueda
    // ver info (ej: ¿está lleno?) o cambiar el estado del scrim.
    void postular(Scrim scrim); 

    // Qué pasa cuando un jugador confirma asistencia
    void confirmar(Scrim scrim);

    // Qué pasa cuando se intenta iniciar la partida (ej: por tiempo)
    void iniciar(Scrim scrim);

    // Qué pasa cuando se intenta finalizar la partida
    void finalizar(Scrim scrim);

    // Qué pasa si el creador cancela
    void cancelar(Scrim scrim);
}