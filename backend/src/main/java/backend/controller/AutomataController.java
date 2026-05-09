package backend.controller;

import backend.model.Automata;
import backend.model.AutomataResponse;
import backend.service.AfndService;
import backend.service.AfndServiceecomerce;
import backend.service.AfndServiceids;
import backend.service.MinimizacionService;
import backend.service.SubconjuntosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador REST que expone los endpoints del módulo de autómatas.
 *
 * Endpoints:
 *   GET  /api/automatas          → devuelve los tres autómatas (AFND, AFD, AFD-MIN)
 *   POST /api/automatas/validar  → valida una cadena contra los tres autómatas
 */
@RestController
@RequestMapping("/api/automatas")
@CrossOrigin(origins = "*")   // permite peticiones desde el frontend Flask
public class AutomataController {

    private final AfndService         afndService;
    private final AfndServiceids      afndServiceids;
    private final AfndServiceecomerce afndServiceecomerce;
    private final SubconjuntosService subconjuntosService;
    private final MinimizacionService minimizacionService;

    public AutomataController(
            AfndService afndService,
            AfndServiceids afndServiceids,
            AfndServiceecomerce afndServiceecomerce,
            SubconjuntosService subconjuntosService,
            MinimizacionService minimizacionService) {
        this.afndService         = afndService;
        this.afndServiceids      = afndServiceids;
        this.afndServiceecomerce = afndServiceecomerce;
        this.subconjuntosService = subconjuntosService;
        this.minimizacionService = minimizacionService;
    }

    // ── GET /api/automatas ───────────────────────────────────────────────────

    /**
     * Construye y devuelve los tres autómatas en formato JSON.
     */
    @GetMapping
    public ResponseEntity<AutomataResponse> obtenerAutomatas() {
        Automata afnd          = construirAfndPorTipo("iot");
        Automata afd           = subconjuntosService.convertirAFD(afnd);
        Automata afdMinimizado = minimizacionService.minimizar(afd);

        return ResponseEntity.ok(new AutomataResponse(afnd, afd, afdMinimizado));
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<AutomataResponse> obtenerAutomatasPorTipo(@PathVariable String tipo) {
        Automata afnd          = construirAfndPorTipo(tipo);
        Automata afd           = subconjuntosService.convertirAFD(afnd);
        Automata afdMinimizado = minimizacionService.minimizar(afd);

        return ResponseEntity.ok(new AutomataResponse(afnd, afd, afdMinimizado));
    }

    // ── POST /api/automatas/validar ──────────────────────────────────────────

    /**
     * Valida una cadena de símbolos contra los tres autómatas.
     *
     * Body JSON esperado:
     * {
     *   "cadena": ["HDR", "T", "H", "CRC"]
     * }
     *
     * Respuesta:
     * {
     *   "cadena": ["HDR", "T", "H", "CRC"],
     *   "afnd":   true,
     *   "afd":    true,
     *   "afdMin": true
     * }
     */
    @PostMapping("/validar")
    public ResponseEntity<Map<String, Object>> validarCadena(
            @RequestBody Map<String, List<String>> body) {

        return validarCadenaPorTipo("iot", body);
    }

    @PostMapping("/{tipo}/validar")
    public ResponseEntity<Map<String, Object>> validarCadenaPorTipoEndpoint(
            @PathVariable String tipo,
            @RequestBody Map<String, List<String>> body) {

        return validarCadenaPorTipo(tipo, body);
    }

    private ResponseEntity<Map<String, Object>> validarCadenaPorTipo(
            String tipo,
            Map<String, List<String>> body) {

        List<String> cadena = body.get("cadena");
        if (cadena == null) {
            cadena = Collections.emptyList();
        }

        Automata afnd          = construirAfndPorTipo(tipo);
        Automata afd           = subconjuntosService.convertirAFD(afnd);
        Automata afdMinimizado = minimizacionService.minimizar(afd);

        // Obtener resultados con pasos
        ResultadoValidacion resultAfnd   = validarConPasos(afnd, cadena, true);
        ResultadoValidacion resultAfd    = validarConPasos(afd, cadena, false);
        ResultadoValidacion resultAfdMin = validarConPasos(afdMinimizado, cadena, false);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cadena", cadena);
        response.put("afnd", resultAfnd.aceptada);
        response.put("afd", resultAfd.aceptada);
        response.put("afdMin", resultAfdMin.aceptada);

        Map<String, Object> pasos = new LinkedHashMap<>();
        pasos.put("afnd", resultAfnd.pasos);
        pasos.put("afd", resultAfd.pasos);
        pasos.put("afdMin", resultAfdMin.pasos);
        response.put("pasos", pasos);

        return ResponseEntity.ok(response);
    }

    private Automata construirAfndPorTipo(String tipo) {
        if (tipo == null) {
            return afndService.construirAfnd();
        }

        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "iot" -> afndService.construirAfnd();
            case "ids" -> afndServiceids.construirAfnd();
            case "ecommerce", "e-comerce", "ecomerce" -> afndServiceecomerce.construirAfnd();
            default -> throw new IllegalArgumentException("Tipo de automata no soportado: " + tipo);
        };
    }

    // ── Simuladores ──────────────────────────────────────────────────────────

    /**
     * Clase interna para encapsular el resultado de una validación con sus pasos.
     */
    private static class ResultadoValidacion {
        boolean aceptada;
        List<Map<String, String>> pasos;

        ResultadoValidacion(boolean aceptada, List<Map<String, String>> pasos) {
            this.aceptada = aceptada;
            this.pasos = pasos;
        }
    }

    /**
     * Valida una cadena y retorna los pasos de las transiciones.
     */
    private ResultadoValidacion validarConPasos(Automata automata, List<String> cadena, boolean esAfnd) {
        List<Map<String, String>> pasos = new ArrayList<>();

        if (esAfnd) {
            boolean resultado = simularAfndConPasos(automata, automata.getEstadoInicial(), cadena, 0, pasos);
            return new ResultadoValidacion(resultado, pasos);
        } else {
            boolean resultado = simularAfdConPasos(automata, cadena, pasos);
            return new ResultadoValidacion(resultado, pasos);
        }
    }

    /**
     * Simula el AFND procesando la cadena y guardando los pasos.
     */
    private boolean simularAfndConPasos(Automata afnd, String estado, List<String> cadena, 
                                        int posicion, List<Map<String, String>> pasos) {
        // Seguir transiciones epsilon desde el estado actual
        for (String epsDest : afnd.obtenerDestinos(estado, "ε")) {
            if (simularAfndConPasos(afnd, epsDest, cadena, posicion, pasos)) return true;
        }

        if (posicion == cadena.size()) {
            boolean aceptada = afnd.getEstadosAceptacion().contains(estado);
            if (aceptada && pasos.isEmpty()) {
                // Estado inicial es de aceptación
                Map<String, String> paso = new LinkedHashMap<>();
                paso.put("estado", estado);
                paso.put("es_aceptacion", "true");
                pasos.add(paso);
            }
            return aceptada;
        }

        String simbolo = cadena.get(posicion);
        for (String destino : afnd.obtenerDestinos(estado, simbolo)) {
            List<Map<String, String>> pasosAux = new ArrayList<>(pasos);
            if (simularAfndConPasos(afnd, destino, cadena, posicion + 1, pasosAux)) {
                // Registrar este camino exitoso
                Map<String, String> paso = new LinkedHashMap<>();
                paso.put("estado", estado);
                paso.put("transicion", simbolo);
                paso.put("proximo_estado", destino);
                pasosAux.add(0, paso);
                pasos.clear();
                pasos.addAll(pasosAux);
                return true;
            }
        }
        return false;
    }

    /**
     * Simula el AFD (o AFD mínimo) procesando la cadena y guardando los pasos.
     */
    private boolean simularAfdConPasos(Automata afd, List<String> cadena, List<Map<String, String>> pasos) {
        String estadoActual = afd.getEstadoInicial();

        // Agregar estado inicial
        Map<String, String> pasoInicial = new LinkedHashMap<>();
        pasoInicial.put("estado", estadoActual);
        pasos.add(pasoInicial);

        for (String simbolo : cadena) {
            List<String> destinos = afd.obtenerDestinos(estadoActual, simbolo);
            if (destinos.isEmpty()) {
                // Transición muerta
                Map<String, String> pasoFinal = new LinkedHashMap<>();
                pasoFinal.put("estado", estadoActual);
                pasoFinal.put("es_aceptacion", "false");
                pasos.add(pasoFinal);
                return false;
            }

            estadoActual = destinos.get(0);

            Map<String, String> paso = new LinkedHashMap<>();
            paso.put("transicion", simbolo);
            paso.put("proximo_estado", estadoActual);
            pasos.add(paso);
        }

        boolean aceptada = afd.getEstadosAceptacion().contains(estadoActual);
        Map<String, String> pasoFinal = new LinkedHashMap<>();
        pasoFinal.put("estado", estadoActual);
        pasoFinal.put("es_aceptacion", String.valueOf(aceptada));
        pasos.add(pasoFinal);

        return aceptada;
    }



    
}