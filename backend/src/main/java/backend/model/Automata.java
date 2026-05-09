package backend.model;

import java.util.*;

/**
 * Representa un Autómata Finito (AFD o AFND) mediante su quíntupla formal:
 * M = (Q, Σ, δ, q0, F)
 */
public class Automata {

    /** Conjunto de estados Q */
    private List<String> estados;

    /** Alfabeto Σ */
    private List<String> alfabeto;

    /**
     * Función de transición δ.
     * Clave: "estado|simbolo"  →  Valor: lista de estados destino
     * (lista de uno para AFD, puede ser varios para AFND)
     */
    private Map<String, List<String>> transiciones;

    /** Estado inicial q0 */
    private String estadoInicial;

    /** Conjunto de estados de aceptación F */
    private List<String> estadosAceptacion;

    /** Tipo descriptivo: "AFND", "AFD", "AFD-MIN" */
    private String tipo;

    public Automata() {
        this.estados         = new ArrayList<>();
        this.alfabeto        = new ArrayList<>();
        this.transiciones    = new LinkedHashMap<>();
        this.estadosAceptacion = new ArrayList<>();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Agrega una transición δ(estado, simbolo) = destino.
     * En un AFND puede llamarse varias veces con el mismo par (estado, simbolo).
     */
    public void agregarTransicion(String estado, String simbolo, String destino) {
        String clave = estado + "|" + simbolo;
        transiciones.computeIfAbsent(clave, k -> new ArrayList<>()).add(destino);
    }

    /**
     * Devuelve los estados destino de δ(estado, simbolo).
     * Retorna lista vacía si no existe transición.
     */
    public List<String> obtenerDestinos(String estado, String simbolo) {
        return transiciones.getOrDefault(estado + "|" + simbolo, Collections.emptyList());
    }

    // ── Getters y Setters ────────────────────────────────────────────────────

    public List<String> getEstados()             { return estados; }
    public void setEstados(List<String> estados) { this.estados = estados; }

    public List<String> getAlfabeto()               { return alfabeto; }
    public void setAlfabeto(List<String> alfabeto)  { this.alfabeto = alfabeto; }

    public Map<String, List<String>> getTransiciones()                    { return transiciones; }
    public void setTransiciones(Map<String, List<String>> transiciones)   { this.transiciones = transiciones; }

    public String getEstadoInicial()                    { return estadoInicial; }
    public void setEstadoInicial(String estadoInicial)  { this.estadoInicial = estadoInicial; }

    public List<String> getEstadosAceptacion()                        { return estadosAceptacion; }
    public void setEstadosAceptacion(List<String> estadosAceptacion)  { this.estadosAceptacion = estadosAceptacion; }

    public String getTipo()              { return tipo; }
    public void setTipo(String tipo)     { this.tipo = tipo; }
}