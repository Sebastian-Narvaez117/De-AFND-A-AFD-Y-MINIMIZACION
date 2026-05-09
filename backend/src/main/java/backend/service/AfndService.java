package backend.service;

import backend.model.Automata;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que construye el AFND del protocolo de telemetría IoT.
 *
 * Lenguaje reconocido: HDR (H | T)* CRC
 * Quíntupla:
 *   Q  = {q0, q1, q2, q3}
 *   Σ  = {HDR, H, T, CRC}
 *   q0 = q0
 *   F  = {q3}
 *   δ  = tabla de transiciones con transición epsilon q1 → q2
 */
@Service
public class AfndService {

    /**
     * Construye y devuelve el AFND del problema IoT.
     */
    public Automata construirAfnd() {
        Automata afnd = new Automata();
        afnd.setTipo("AFND");

        // Q — estados
        afnd.setEstados(List.of("q0", "q1", "q2", "q3"));

        // Σ — alfabeto (epsilon se representa como "ε" solo en la tabla, no es símbolo real)
        afnd.setAlfabeto(List.of("HDR", "H", "T", "CRC"));

        // q0 — estado inicial
        afnd.setEstadoInicial("q0");

        // F — estados de aceptación
        afnd.setEstadosAceptacion(List.of("q3"));

        // δ — función de transición
        afnd.agregarTransicion("q0", "HDR", "q1");   // q0 --HDR--> q1
        afnd.agregarTransicion("q1", "ε",   "q2");   // q1 --ε--> q2  (transición epsilon)
        afnd.agregarTransicion("q2", "H",   "q2");   // q2 --H-->  q2 (loop sensores)
        afnd.agregarTransicion("q2", "T",   "q2");   // q2 --T-->  q2 (loop sensores)
        afnd.agregarTransicion("q2", "CRC", "q3");   // q2 --CRC-> q3

        return afnd;
    }
}