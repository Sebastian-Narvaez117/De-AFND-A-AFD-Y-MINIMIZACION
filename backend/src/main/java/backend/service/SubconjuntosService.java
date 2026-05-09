package backend.service;

import backend.model.Automata;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que aplica el algoritmo de construcción de subconjuntos
 * para convertir un AFND (con transiciones epsilon) en un AFD equivalente.
 *
 * Algoritmo:
 *  1. Calcular la ε-clausura del estado inicial → estado inicial del AFD.
 *  2. Para cada estado nuevo del AFD, calcular mover(estado, símbolo) para
 *     cada símbolo del alfabeto y luego aplicar ε-clausura al resultado.
 *  3. Repetir hasta que no aparezcan estados nuevos.
 *  4. Un estado del AFD acepta si contiene al menos un estado de aceptación del AFND.
 */
@Service
public class SubconjuntosService {

    /**
     * Convierte el AFND recibido en un AFD mediante construcción de subconjuntos.
     *
     * @param afnd Autómata finito no determinista de entrada.
     * @return     AFD equivalente.
     */
    public Automata convertirAFD(Automata afnd) {
        Automata afd = new Automata();
        afd.setTipo("AFD");
        afd.setAlfabeto(new ArrayList<>(afnd.getAlfabeto()));

        // Mapa: nombre del estado AFD → conjunto de estados AFND que representa
        Map<String, Set<String>> mapaEstados = new LinkedHashMap<>();

        // Cola de estados del AFD pendientes de procesar
        Queue<Set<String>> pendientes = new LinkedList<>();

        // Paso 1: estado inicial del AFD = ε-clausura(q0)
        Set<String> estadoInicial = epsilonClausura(Set.of(afnd.getEstadoInicial()), afnd);
        String nombreInicial = nombreEstado(estadoInicial);
        mapaEstados.put(nombreInicial, estadoInicial);
        pendientes.add(estadoInicial);

        afd.setEstadoInicial(nombreInicial);

        // Paso 2 y 3: procesar cada estado pendiente
        while (!pendientes.isEmpty()) {
            Set<String> estadoActual = pendientes.poll();
            String nombreActual = nombreEstado(estadoActual);

            for (String simbolo : afnd.getAlfabeto()) {
                // mover(estadoActual, simbolo) en el AFND
                Set<String> destinos = mover(estadoActual, simbolo, afnd);

                if (destinos.isEmpty()) {
                    continue; // sin transición para este símbolo
                }

                // ε-clausura del conjunto de destinos
                Set<String> clausura = epsilonClausura(destinos, afnd);
                String nombreDestino = nombreEstado(clausura);

                // Agregar transición al AFD
                afd.agregarTransicion(nombreActual, simbolo, nombreDestino);

                // Si es un estado nuevo, agregarlo a la cola
                if (!mapaEstados.containsKey(nombreDestino)) {
                    mapaEstados.put(nombreDestino, clausura);
                    pendientes.add(clausura);
                }
            }
        }

        // Paso 4: definir estados y estados de aceptación del AFD
        List<String> estadosAfd = new ArrayList<>(mapaEstados.keySet());
        afd.setEstados(estadosAfd);

        List<String> aceptacion = estadosAfd.stream()
                .filter(nombre -> {
                    Set<String> subconjunto = mapaEstados.get(nombre);
                    return subconjunto.stream().anyMatch(afnd.getEstadosAceptacion()::contains);
                })
                .collect(Collectors.toList());

        afd.setEstadosAceptacion(aceptacion);
        return afd;
    }

    // ── Métodos privados de apoyo ────────────────────────────────────────────

    /**
     * Calcula la ε-clausura de un conjunto de estados:
     * todos los estados alcanzables siguiendo solo transiciones epsilon.
     */
    private Set<String> epsilonClausura(Set<String> estados, Automata afnd) {
        Set<String> clausura = new LinkedHashSet<>(estados);
        Deque<String> pila = new ArrayDeque<>(estados);

        while (!pila.isEmpty()) {
            String estado = pila.pop();
            List<String> epsilonDestinos = afnd.obtenerDestinos(estado, "ε");
            for (String destino : epsilonDestinos) {
                if (clausura.add(destino)) {
                    pila.push(destino);
                }
            }
        }
        return clausura;
    }

    /**
     * Calcula mover(conjunto, simbolo):
     * todos los estados alcanzables desde cualquier estado del conjunto
     * leyendo exactamente el símbolo dado (sin epsilon).
     */
    private Set<String> mover(Set<String> estados, String simbolo, Automata afnd) {
        Set<String> resultado = new LinkedHashSet<>();
        for (String estado : estados) {
            resultado.addAll(afnd.obtenerDestinos(estado, simbolo));
        }
        return resultado;
    }

    /**
     * Genera un nombre legible para un estado del AFD a partir de su subconjunto de AFND.
     * Ejemplo: {q1, q2} → "{q1,q2}"
     */
    private String nombreEstado(Set<String> estados) {
        if (estados.isEmpty()) return "∅";
        List<String> ordenados = new ArrayList<>(estados);
        Collections.sort(ordenados);
        return "{" + String.join(",", ordenados) + "}";
    }
}