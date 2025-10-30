package event;

import java.util.UUID; // Lo usamos para el ID

/**
 * Un evento de dominio CONCRETO.
 * Se dispara cuando el estado de un Scrim ha cambiado.
 * (Usamos un 'record' de Java moderno, como sugiere el PDF )
 */
public record ScrimStateChanged(UUID scrimId, String nuevoEstado) implements DomainEvent {
    // Un 'record' automáticamente crea los atributos privados,
    // el constructor, los getters y los métodos toString/equals.
    // Es un POJO súper compacto.
}