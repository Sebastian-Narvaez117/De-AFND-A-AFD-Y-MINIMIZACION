package backend.service;

import backend.model.Automata;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que minimiza un AFD usando el algoritmo de partición por
 * clases de equivalencia (algoritmo de Moore / tabla de relleno).
 *
 * Algoritmo:
 *  1. Partición inicial: separar estados de aceptación de los que no aceptan.
 *  2. Refinamiento: dividir grupos cuando dos estados del mismo grupo,
 *     ante algún símbolo, van a grupos diferentes.
 *  3. Repetir hasta que ningún grupo se divida.
 *  4. Cada grupo se convierte en un único estado del AFD mínimo.
 */
@Service
public class MinimizacionService {

    /**
     * Minimiza el AFD recibido y devuelve el AFD mínimo equivalente.
     *
     * @param afd AFD de entrada (ya determinista).
     * @return    AFD minimizado.
     */
    public Automata minimizar(Automata afd) {

        List<String> estados   = afd.getEstados();
        List<String> alfabeto  = afd.getAlfabeto();
        List<String> aceptacion = afd.getEstadosAceptacion();

        // Eliminar estados inaccesibles antes de minimizar
        Set<String> accesibles = calcularAccesibles(afd);
        estados = estados.stream().filter(accesibles::contains).collect(Collectors.toList());

        // ── Paso 1: partición inicial ────────────────────────────────────────
        List<Set<String>> particion = new ArrayList<>();

        Set<String> grupoAceptacion  = new LinkedHashSet<>(aceptacion);
        grupoAceptacion.retainAll(accesibles);

        Set<String> grupoNoAceptacion = new LinkedHashSet<>(estados);
        grupoNoAceptacion.removeAll(grupoAceptacion);

        if (!grupoAceptacion.isEmpty())   particion.add(grupoAceptacion);
        if (!grupoNoAceptacion.isEmpty()) particion.add(grupoNoAceptacion);

        // ── Pasos 2 y 3: refinar la partición ───────────────────────────────
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            List<Set<String>> nuevaParticion = new ArrayList<>();

            for (Set<String> grupo : particion) {
                List<Set<String>> subgrupos = dividirGrupo(grupo, particion, alfabeto, afd);
                nuevaParticion.addAll(subgrupos);
                if (subgrupos.size() > 1) cambio = true;
            }
            particion = nuevaParticion;
        }

        // ── Paso 4: construir el AFD mínimo ──────────────────────────────────
        return construirAfdMinimo(particion, afd);
    }

    // ── Métodos privados de apoyo ────────────────────────────────────────────

    /**
     * Calcula todos los estados accesibles desde el estado inicial del AFD.
     */
    private Set<String> calcularAccesibles(Automata afd) {
        Set<String> accesibles = new LinkedHashSet<>();
        Queue<String> cola = new LinkedList<>();
        cola.add(afd.getEstadoInicial());
        accesibles.add(afd.getEstadoInicial());

        while (!cola.isEmpty()) {
            String estado = cola.poll();
            for (String simbolo : afd.getAlfabeto()) {
                List<String> destinos = afd.obtenerDestinos(estado, simbolo);
                for (String d : destinos) {
                    if (accesibles.add(d)) cola.add(d);
                }
            }
        }
        return accesibles;
    }

    /**
     * Intenta dividir un grupo de estados en subgrupos más pequeños.
     * Dos estados del mismo grupo son distinguibles si ante algún símbolo
     * van a grupos diferentes de la partición actual.
     */
    private List<Set<String>> dividirGrupo(
            Set<String> grupo,
            List<Set<String>> particion,
            List<String> alfabeto,
            Automata afd) {

        List<String> miembros = new ArrayList<>(grupo);

        // Mapa: "firma" del estado → subgrupo al que pertenece
        Map<String, Set<String>> firmaASubgrupo = new LinkedHashMap<>();

        for (String estado : miembros) {
            String firma = calcularFirma(estado, particion, alfabeto, afd);
            firmaASubgrupo.computeIfAbsent(firma, k -> new LinkedHashSet<>()).add(estado);
        }

        return new ArrayList<>(firmaASubgrupo.values());
    }

    /**
     * Calcula la "firma" de un estado: para cada símbolo del alfabeto,
     * el índice del grupo al que va el estado destino.
     * Estados con la misma firma se comportan igual → son equivalentes.
     */
    private String calcularFirma(
            String estado,
            List<Set<String>> particion,
            List<String> alfabeto,
            Automata afd) {

        StringBuilder firma = new StringBuilder();
        for (String simbolo : alfabeto) {
            List<String> destinos = afd.obtenerDestinos(estado, simbolo);
            if (destinos.isEmpty()) {
                firma.append("∅");
            } else {
                String destino = destinos.get(0);
                firma.append(indiceGrupo(destino, particion));
            }
            firma.append("|");
        }
        return firma.toString();
    }

    /**
     * Retorna el índice del grupo al que pertenece un estado dentro de la partición.
     */
    private int indiceGrupo(String estado, List<Set<String>> particion) {
        for (int i = 0; i < particion.size(); i++) {
            if (particion.get(i).contains(estado)) return i;
        }
        return -1; // estado muerto / inaccesible
    }

    /**
     * Construye el AFD mínimo a partir de la partición final.
     * Cada grupo se convierte en un estado nombrado S0, S1, S2...
     */
    private Automata construirAfdMinimo(List<Set<String>> particion, Automata afdOriginal) {
        Automata minimo = new Automata();
        minimo.setTipo("AFD-MIN");
        minimo.setAlfabeto(new ArrayList<>(afdOriginal.getAlfabeto()));

        // Encontrar el indice del grupo que contiene el estado inicial
        int indicePrincipal = -1;
        for (int i = 0; i < particion.size(); i++) {
            if (particion.get(i).contains(afdOriginal.getEstadoInicial())) {
                indicePrincipal = i;
                break;
            }
        }

        if (indicePrincipal < 0 && !particion.isEmpty()) {
            indicePrincipal = 0;
        }

        Set<String> aceptacionOriginal = new HashSet<>(afdOriginal.getEstadosAceptacion());

        // Definir orden de nombrado:
        // 1) grupo inicial
        // 2) grupos no aceptadores
        // 3) grupos aceptadores (para que queden al final)
        List<Integer> ordenIndices = new ArrayList<>();
        if (indicePrincipal >= 0) {
            ordenIndices.add(indicePrincipal);
        }

        for (int i = 0; i < particion.size(); i++) {
            if (i == indicePrincipal) continue;
            boolean esAceptador = particion.get(i).stream().anyMatch(aceptacionOriginal::contains);
            if (!esAceptador) ordenIndices.add(i);
        }

        for (int i = 0; i < particion.size(); i++) {
            if (i == indicePrincipal) continue;
            boolean esAceptador = particion.get(i).stream().anyMatch(aceptacionOriginal::contains);
            if (esAceptador) ordenIndices.add(i);
        }

        // Crear mapeo: el grupo inicial siempre sera S0
        Map<Integer, String> indicePorNombre = new LinkedHashMap<>();
        List<String> estadosMinimo = new ArrayList<>();
        for (int i = 0; i < ordenIndices.size(); i++) {
            int indiceGrupo = ordenIndices.get(i);
            String nombre = "S" + i;
            indicePorNombre.put(indiceGrupo, nombre);
            estadosMinimo.add(nombre);
        }

        minimo.setEstados(estadosMinimo);
        minimo.setEstadoInicial(indicePorNombre.getOrDefault(indicePrincipal, "S0"));

        // Estados de aceptación: grupos que contienen algún estado de aceptación
        List<String> aceptacionMinimo = new ArrayList<>();
        for (int i = 0; i < particion.size(); i++) {
            Set<String> grupo = particion.get(i);
            boolean aceptador = grupo.stream()
                    .anyMatch(afdOriginal.getEstadosAceptacion()::contains);
            if (aceptador) aceptacionMinimo.add(indicePorNombre.get(i));
        }
        minimo.setEstadosAceptacion(aceptacionMinimo);

        // Transiciones: tomar un representante de cada grupo
        for (int i = 0; i < particion.size(); i++) {
            String representante = particion.get(i).iterator().next();
            String nombreOrigen  = indicePorNombre.get(i);

            for (String simbolo : afdOriginal.getAlfabeto()) {
                List<String> destinos = afdOriginal.obtenerDestinos(representante, simbolo);
                if (!destinos.isEmpty()) {
                    String estadoDestino = destinos.get(0);
                    // Encontrar a qué grupo pertenece el destino
                    int grupoDestino = indiceGrupo(estadoDestino, particion);
                    if (grupoDestino >= 0) {
                        minimo.agregarTransicion(nombreOrigen, simbolo, indicePorNombre.get(grupoDestino));
                    }
                }
            }
        }

        return minimo;
    }
}