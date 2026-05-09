package backend.service;

import backend.model.Automata;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Servicio que construye el AFND para un proceso de compra en un sitio de comercio electrónico (e-commerce).
 *
 * Lenguaje reconocido: HOME SEARCH+ CART
 * Quíntupla:
 *   Q  = {q0, q1, q2, q3, q4}
 *   Σ  = {H, S, C}
 *   q0 = q0
 *   F  = {q4}
 *   δ  = tabla de transiciones con transición epsilon q2 → q1  y q2 → q3
 *  
 */
@Service
public class AfndServiceecomerce {

     /**
     * Construye y devuelve el AFND del problema E-Commerce.
     */
    public Automata construirAfnd() {
        Automata afnd = new Automata();
        afnd.setTipo("AFND");

        // Q — estados
        afnd.setEstados(List.of("q0", "q1", "q2", "q3", "q4"));

        // Σ — alfabeto (epsilon se representa como "ε" solo en la tabla, no es símbolo real)
        afnd.setAlfabeto(List.of("H", "S", "C"));

        // q0 — estado inicial
        afnd.setEstadoInicial("q0");

        // F — estados de aceptación
        afnd.setEstadosAceptacion(List.of("q4"));

        // δ — función de transición
        afnd.agregarTransicion("q0", "H", "q1");   // q0 --H--> q1
        afnd.agregarTransicion("q1", "S", "q2");   // q1 --S--> q2  (transición búsqueda)
        afnd.agregarTransicion("q2", "ε", "q1");   // q2 --ε--> q1  (transición epsilon a búsqueda)
        afnd.agregarTransicion("q2", "ε", "q3");   // q2 --ε--> q3  (transición epsilon a carrito)
        afnd.agregarTransicion("q3", "C", "q4");   // q3 --C-->  q4 (transición compra)
        return afnd;
    }
    
}
