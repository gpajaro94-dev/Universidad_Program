import java.util.HashMap;
import java.util.Map;

public class Cinta { // Cinta de la máquina de Turing
    public static final char VACIO = 'B';

    private final Map<Integer, Character> celdas = new HashMap<>();
    private int posicionCabezal = 0;

    public Cinta() { // Cinta vacía
    }

    public Cinta(String bitsIniciales) { // Cinta con bits iniciales
        cargar(bitsIniciales);
    }

    public void cargar(String bitsIniciales) {
        celdas.clear();
        posicionCabezal = 0;
        if (bitsIniciales == null) {
            return;
        }
        for (int i = 0; i < bitsIniciales.length(); i++) {
            char bit = bitsIniciales.charAt(i);
            verificarSimbolo(bit);
            if (bit != VACIO) {
                celdas.put(i, bit);
            }
        }
    } // Carga los bits en la cinta, validando cada símbolo

    public char leer() {
        return celdas.getOrDefault(posicionCabezal, VACIO);
    } // Lee el símbolo en la posición actual del cabezal

    public void escribir(char simbolo) {
        verificarSimbolo(simbolo);
        if (simbolo == VACIO) {
            celdas.remove(posicionCabezal);
        } else {
            celdas.put(posicionCabezal, simbolo);
        }
    } // Escribe un símbolo en la posición actual del cabezal

    public void mover(String direccion) {
        if ("L".equalsIgnoreCase(direccion)) {
            posicionCabezal--;
        } else if ("R".equalsIgnoreCase(direccion)) {
            posicionCabezal++;
        } else if (!"N".equalsIgnoreCase(direccion)) {
            throw new IllegalArgumentException("Dirección inválida: " + direccion);
        }
    } // Mueve el cabezal en la dirección especificada (L, R, N)

    public int getPosicionCabezal() {
        return posicionCabezal;
    } // Devuelve la posición actual del cabezal

    public Map<Integer, Character> getCeldas() {
        return new HashMap<>(celdas);
    } // Devuelve una copia de las celdas de la cinta

    private void verificarSimbolo(char simbolo) {
        if (simbolo != '0' && simbolo != '1' && simbolo != VACIO) {
            throw new IllegalArgumentException("Símbolo inválido: " + simbolo);
        }
    } // Valida que el símbolo sea '0', '1' o VACIO
}
