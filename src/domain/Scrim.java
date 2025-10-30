package domain;
import java.util.List;
import java.util.ArrayList;

import state.BuscandoState;
import state.ScrimState;

import strategy.MatchmakingStrategy;

import java.time.LocalDateTime;

import java.util.UUID;
import event.DomainEventBus;
import event.ScrimStateChanged;

/**
 * Clase principal del Dominio.
 * Funciona como Contexto para el Patrón State.
 * Funciona como Contexto para el Patrón Strategy.
 */
public class Scrim {

    private ScrimState state;

    private MatchmakingStrategy strategy;

    private UUID id;
    private String juego;
    private String formato; // Ej: "5v5", "1v1"
    private String region;
    private int rangoMin;
    private int rangoMax;
    private int latenciaMax;
    private LocalDateTime fechaHora; // Para esto importamos java.time.LocalDateTime
    private int duracionEstimada; // En minutos
    private int cupos; // Cantidad total de jugadores
    // private String reglasRoles; (lo agrego despues, más complejo)

    
    public Scrim() {
        this.id = UUID.randomUUID(); // ID único
    }

    
    // Constructor de Prueba
    public Scrim(MatchmakingStrategy strategyInicial) {
        this.id = UUID.randomUUID();
        this.state = new BuscandoState();
        this.strategy = strategyInicial;
        System.out.println("Nuevo Scrim Creado. Estado: Buscando. Estrategia: " + strategyInicial.getClass().getSimpleName());
    }

    // Métodos de Strategy y State

    public void setStrategy(MatchmakingStrategy nuevaStrategy) {
        this.strategy = nuevaStrategy;
        System.out.println("!!! Estrategia de Matchmaking cambiada a: " + nuevaStrategy.getClass().getSimpleName() + " !!!");
    }

    public void buscarJugadores() {
        System.out.println("\nIniciando búsqueda de jugadores...");
        List<Usuario> candidatos = new ArrayList<>();
        candidatos.add(new Usuario());
        
        List<Usuario> seleccionados = this.strategy.seleccionar(candidatos, this);
        System.out.println("Búsqueda finalizada. Jugadores seleccionados: " + seleccionados.size());
    }

    public void postularse() { state.postular(this); }
    public void confirmarAsistencia() { state.confirmar(this); }
    public void iniciarPartida() { state.iniciar(this); }
    public void finalizarPartida() { state.finalizar(this); }
    public void cancelarPartida() { state.cancelar(this); }

    public void setState(ScrimState newState) {
        this.state = newState;
        String nuevoEstadoNombre = newState.getClass().getSimpleName();
        System.out.println("--- El estado del Scrim cambió a: " + nuevoEstadoNombre + " ---");

        // Patrón Observer
        // 1. Creamos el evento con los datos
        ScrimStateChanged evento = new ScrimStateChanged(this.id, nuevoEstadoNombre);

        // 2. Obtenemos el Bus y publicamos el evento
        DomainEventBus.getInstance().publish(evento);
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public String getJuego() {
        return juego;
    }

    public void setJuego(String juego) {
        this.juego = juego;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getRangoMin() {
        return rangoMin;
    }

    public void setRangoMin(int rangoMin) {
        this.rangoMin = rangoMin;
    }

    public int getRangoMax() {
        return rangoMax;
    }

    public void setRangoMax(int rangoMax) {
        this.rangoMax = rangoMax;
    }

    public int getLatenciaMax() {
        return latenciaMax;
    }

    public void setLatenciaMax(int latenciaMax) {
        this.latenciaMax = latenciaMax;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(int duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public int getCupos() {
        return cupos;
    }

    public void setCupos(int cupos) {
        this.cupos = cupos;
    }
}