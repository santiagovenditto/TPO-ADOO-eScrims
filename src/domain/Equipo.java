package domain;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa un "lado" del scrim (ej. Equipo A, Equipo B).
 * Contiene una lista de jugadores.
 */
public class Equipo {

    // --- Atributos Privados ---
    private String lado; // Ej: "A" o "B", "Atacantes" o "Defensores"
    private List<Usuario> jugadores;

    // --- Constructor Vacío ---
    public Equipo() {
        this.jugadores = new ArrayList<>(); // Inicializamos la lista
    }

    // --- Getters y Setters ---

    public String getLado() {
        return lado;
    }

    public void setLado(String lado) {
        this.lado = lado;
    }

    public List<Usuario> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<Usuario> jugadores) {
        this.jugadores = jugadores;
    }

    // Método útil
    public void agregarJugador(Usuario jugador) {
        this.jugadores.add(jugador);
    }
}