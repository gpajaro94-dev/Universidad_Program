import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaquinaTuring {
    public static final class ResultadoPaso {
        private final int numeroPaso;
        private final char bitLeido;
        private final String estadoAnterior;
        private final String siguienteEstado;
        private final Transicion reglaAplicada;
        private final int posicionCabezal;
        private final boolean detenido;

        public ResultadoPaso(int numeroPaso, char bitLeido, String estadoAnterior, String siguienteEstado,
                             Transicion reglaAplicada, int posicionCabezal, boolean detenido) {
            this.numeroPaso = numeroPaso;
            this.bitLeido = bitLeido;
            this.estadoAnterior = estadoAnterior;
            this.siguienteEstado = siguienteEstado;
            this.reglaAplicada = reglaAplicada;
            this.posicionCabezal = posicionCabezal;
            this.detenido = detenido;
        }

        public int getNumeroPaso() { return numeroPaso; }
        public char getBitLeido() { return bitLeido; }
        public String getEstadoAnterior() { return estadoAnterior; }
        public String getSiguienteEstado() { return siguienteEstado; }
        public Transicion getReglaAplicada() { return reglaAplicada; }
        public int getPosicionCabezal() { return posicionCabezal; }
        public boolean estaDetenida() { return detenido; }
    }

    private final Cinta cinta;
    private final List<Transicion> transiciones = new ArrayList<>();
    private final String estadoInicial;
    private final String estadoAceptacion;
    private final String contenidoInicial;
    private String estadoActual;
    private boolean ejecutando;
    private int conteoPasos;

    public MaquinaTuring(String contenidoInicial, String estadoInicial, String estadoAceptacion) {
        this.contenidoInicial = contenidoInicial == null ? "" : contenidoInicial;
        this.cinta = new Cinta(this.contenidoInicial);
        this.estadoInicial = estadoInicial;
        this.estadoAceptacion = estadoAceptacion;
        this.estadoActual = estadoInicial;
        this.ejecutando = true;
        this.conteoPasos = 0;
    }

    public void agregarTransicion(Transicion transicion) {
        transiciones.add(transicion);
    }

    public Optional<ResultadoPaso> avanzar() {
        if (!ejecutando) {
            return Optional.empty();
        }

        char bitLeido = cinta.leer();
        Transicion reglaAplicada = buscarRegla(estadoActual, bitLeido);
        if (reglaAplicada == null) {
            ejecutando = false;
            return Optional.of(new ResultadoPaso(
                conteoPasos + 1,
                bitLeido,
                estadoActual,
                estadoActual,
                null,
                cinta.getPosicionCabezal(),
                true
            ));
        }

        String estadoAnterior = estadoActual;
        cinta.escribir(reglaAplicada.getSimboloEscrito());
        cinta.mover(reglaAplicada.getDireccion());
        estadoActual = reglaAplicada.getSiguienteEstado();
        conteoPasos++;
        if (estadoAceptacion.equals(estadoActual)) {
            ejecutando = false;
        }

        return Optional.of(new ResultadoPaso(
            conteoPasos,
            bitLeido,
            estadoAnterior,
            estadoActual,
            reglaAplicada,
            cinta.getPosicionCabezal(),
            !ejecutando
        ));
    }

    private Transicion buscarRegla(String estado, char bitLeido) {
        for (Transicion transicion : transiciones) {
            if (transicion.esValidaPara(estado, bitLeido)) {
                return transicion;
            }
        }
        return null;
    }

    public void reiniciar() {
        cinta.cargar(contenidoInicial);
        estadoActual = estadoInicial;
        ejecutando = true;
        conteoPasos = 0;
    }

    public Cinta getCinta() {
        return cinta;
    }

    public List<Transicion> getTransiciones() {
        return new ArrayList<>(transiciones);
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public boolean estaEjecutando() {
        return ejecutando;
    }

    public int getConteoPasos() {
        return conteoPasos;
    }
}
