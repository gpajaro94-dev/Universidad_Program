public final class ConvertidorBinario {
    private ConvertidorBinario() {
    }

    public static String convertirABinario(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder binario = new StringBuilder(input.length() * 8);
        for (int i = 0; i < input.length(); i++) {
            String bits = Integer.toBinaryString(input.charAt(i));
            binario.append("0".repeat(Math.max(0, 8 - bits.length())));
            binario.append(bits);
        }
        return binario.toString();
    }
}
