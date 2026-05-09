package backend.model;

/**
 * DTO que agrupa los tres autómatas para enviarlos al frontend en una sola respuesta.
 */
public class AutomataResponse {

    private Automata afnd;
    private Automata afd;
    private Automata afdMinimizado;

    public AutomataResponse(Automata afnd, Automata afd, Automata afdMinimizado) {
        this.afnd          = afnd;
        this.afd           = afd;
        this.afdMinimizado = afdMinimizado;
    }

    public Automata getAfnd()                          { return afnd; }
    public void setAfnd(Automata afnd)                 { this.afnd = afnd; }

    public Automata getAfd()                           { return afd; }
    public void setAfd(Automata afd)                   { this.afd = afd; }

    public Automata getAfdMinimizado()                         { return afdMinimizado; }
    public void setAfdMinimizado(Automata afdMinimizado)       { this.afdMinimizado = afdMinimizado; }
}