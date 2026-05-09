package backend.service;

import backend.model.Automata;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que construye el AFND para deteccion de Patrones de Ataque en Ciberseguridad (IDS) 
 *
 * Lenguaje reconocido: S A R
 * Quíntupla:
 *   Q  = {q0, q1, q2, q3, q4}
 *   Σ  = {S, A, R}
 *   q0 = q0
 *   F  = {q4}
 *   δ  = tabla de transiciones con transición epsilon q1 → q2
 */


@Service
public class AfndServiceids {

    /**
     * Construye y devuelve el AFND del problema IDS.
     */

    public Automata construirAfnd() {
        Automata afnd = new Automata();
        afnd.setTipo("AFND");

        // Q — estados
        afnd.setEstados(List.of("q0", "q1", "q2", "q3", "q4"));

        // Σ — alfabeto (epsilon se representa como "ε" solo en la tabla, no es símbolo real)
        afnd.setAlfabeto(List.of("S", "A", "R"));

        // q0 — estado inicial
        afnd.setEstadoInicial("q0");

        // F — estados de aceptación
        afnd.setEstadosAceptacion(List.of("q4"));

        // δ — función de transición
        afnd.agregarTransicion("q0", "S", "q1");   // q0 --S--> q1
        afnd.agregarTransicion("q1", "ε", "q2");   // q1 --ε--> q2  (transición epsilon)
        afnd.agregarTransicion("q2", "A", "q3");   // q2 --A-->  q3 (loop ataque)
        afnd.agregarTransicion("q3", "A", "q3");   // q3 --A-->  q3 (respuesta)
        afnd.agregarTransicion("q3", "R", "q4");   // q3 --R-->  q4 (respuesta final)

        return afnd;
    }

    
}
