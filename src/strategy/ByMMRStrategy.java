package strategy;
import java.util.List;

import domain.Scrim;
import domain.Usuario;

import java.util.ArrayList; // Usamos ArrayList como una implementación de List
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Random;

public class ByMMRStrategy implements MatchmakingStrategy {

    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        System.out.println("--- LOGICA DE MATCHMAKING: Seleccionando jugadores por MMR... ---");
        if (candidatos == null || candidatos.isEmpty()) return new ArrayList<>();
        // Simulamos un MMR por Usuario: si no hay campo real, generamos determinísticamente a partir del username
        List<Usuario> copy = new ArrayList<>(candidatos);
        Random rnd = new Random(0); // semilla fija para reproducibilidad en demo
        // Map each usuario to a simulated mmr value using username hash (stable)
        List<Usuario> ordered = copy.stream().sorted(Comparator.comparingInt(u -> -simulateMmr(u, rnd))).collect(Collectors.toList());
        // determine how many to pick: prefer scrim.cupos if available, else pick up to candidatos size
        int pick = candidatos.size();
        try{
            // attempt to infer capacity from scrim class (best-effort). If Scrim has getCupos(), use it.
            pick = Math.max(1, scrim.getCupos() > 0 ? scrim.getCupos() : candidatos.size());
        }catch(Exception e){ pick = Math.min(candidatos.size(), 10); }
        return ordered.subList(0, Math.min(pick, ordered.size()));
    }

    private int simulateMmr(Usuario u, Random rnd){
        try{
            if(u==null) return 1000 + rnd.nextInt(800);
            // prefer explicit ranking if set on Usuario
            try{ Integer r = u.getRanking(); if(r!=null) return r.intValue(); }catch(Exception ex){}
            String name = u.getUsername(); if(name==null || name.trim().isEmpty()) return 1000 + rnd.nextInt(800);
            int h = Math.abs(name.hashCode());
            return 800 + (h % 2000); // ranking in 800..2799
        }catch(Exception e){ return 1000 + rnd.nextInt(800); }
    }
}