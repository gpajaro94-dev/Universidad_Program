public class Transicion {
    private final int idRegla;
    private final String estadoActual;
    private final char simboloLeido;
    private final char simboloEscrito;
    private final String direccion;
    private final String siguienteEstado;

    public Transicion(int idRegla, String estadoActual, char simboloLeido, char simboloEscrito,
                      String direccion, String siguienteEstado) {
        this.idRegla = idRegla;
        this.estadoActual = estadoActual;
        this.simboloLeido = simboloLeido;
        this.simboloEscrito = simboloEscrito;
        this.direccion = direccion;
        this.siguienteEstado = siguienteEstado;
    }

    public int getIdRegla() {
        return idRegla;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public char getSimboloLeido() {
        return simboloLeido;
    }

    public char getSimboloEscrito() {
        return simboloEscrito;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getSiguienteEstado() {
        return siguienteEstado;
    }

    public boolean esValidaPara(String estado, char simbolo) {
        return this.estadoActual.equals(estado) && this.simboloLeido == simbolo;
    }

    @Override
    public String toString() {
        return String.format("#%d %s + '%c' -> '%c', %s, %s",
                idRegla, estadoActual, simboloLeido, simboloEscrito, direccion, siguienteEstado);
    }
}
