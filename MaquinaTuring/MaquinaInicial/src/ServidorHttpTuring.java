import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class ServidorHttpTuring {
    private static final String ESTADO_INICIAL = "q0";
    private static final String ESTADO_ACEPTACION = "q_accept";

    private final Path webRoot;
    private MaquinaTuring maquina;
    private String entradaActual = "";
    private String binarioActual = "";

    public ServidorHttpTuring(Path webRoot) {
        this.webRoot = webRoot;
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8787;
        Path root = Paths.get(".").toAbsolutePath().normalize();
        ServidorHttpTuring app = new ServidorHttpTuring(root);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticHandler(app.webRoot));
        server.createContext("/api/init", app::handleInit);
        server.createContext("/api/step", app::handleStep);
        server.createContext("/api/reset", app::handleReset);
        server.start();
        System.out.println("Servidor en http://localhost:" + port + "/index.html");
    }

    private void handleInit(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Use POST\"}");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String input = parseField(body, "input");
        entradaActual = input == null ? "" : input;
        binarioActual = ConvertidorBinario.convertirABinario(entradaActual);
        maquina = construirMaquina(binarioActual);
        send(exchange, 200, instantaneaJson(null));
    }

    private void handleStep(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Use POST\"}");
            return;
        }
        if (maquina == null) {
            send(exchange, 400, "{\"error\":\"Inicializa primero la maquina\"}");
            return;
        }
        Optional<MaquinaTuring.ResultadoPaso> resultado = maquina.avanzar();
        send(exchange, 200, instantaneaJson(resultado.orElse(null)));
    }

    private void handleReset(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Use POST\"}");
            return;
        }
        maquina = construirMaquina(binarioActual);
        send(exchange, 200, instantaneaJson(null));
    }

    private MaquinaTuring construirMaquina(String binario) {
        MaquinaTuring tm = new MaquinaTuring(binario, ESTADO_INICIAL, ESTADO_ACEPTACION);

        // === ESTADO q0: LECTURA/INICIO ===
        tm.agregarTransicion(new Transicion(1, "q0", '0', '0', "N", "q1"));
        tm.agregarTransicion(new Transicion(2, "q0", '1', '1', "N", "q1"));
        tm.agregarTransicion(new Transicion(3, "q0", Cinta.VACIO, Cinta.VACIO, "N", "q_accept"));

        // === ESTADO q1: PROCESAMIENTO ===
        tm.agregarTransicion(new Transicion(4, "q1", '0', '0', "N", "q2"));
        tm.agregarTransicion(new Transicion(5, "q1", '1', '1', "N", "q2"));
        tm.agregarTransicion(new Transicion(6, "q1", Cinta.VACIO, Cinta.VACIO, "N", "q_accept"));

        // === ESTADO q2: ESCRITURA/CÁLCULO ===
        tm.agregarTransicion(new Transicion(7, "q2", '0', '0', "N", "q3"));
        tm.agregarTransicion(new Transicion(8, "q2", '1', '1', "N", "q3"));
        tm.agregarTransicion(new Transicion(9, "q2", Cinta.VACIO, Cinta.VACIO, "N", "q_accept"));

        // === ESTADO q3: VERIFICACIÓN/FINALIZACIÓN ===
        tm.agregarTransicion(new Transicion(10, "q3", '0', '0', "R", "q0"));
        tm.agregarTransicion(new Transicion(11, "q3", '1', '1', "R", "q0"));
        tm.agregarTransicion(new Transicion(12, "q3", Cinta.VACIO, Cinta.VACIO, "N", "q_accept"));

        return tm;
    }

    private String instantaneaJson(MaquinaTuring.ResultadoPaso resultado) {
        Map<Integer, Character> celdas = maquina.getCinta().getCeldas();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"input\":\"").append(escape(entradaActual)).append("\",");
        json.append("\"binary\":\"").append(escape(binarioActual)).append("\",");
        json.append("\"state\":\"").append(escape(maquina.getEstadoActual())).append("\",");
        json.append("\"stateInfo\":").append(obtenerInfoEstado(maquina.getEstadoActual())).append(",");
        json.append("\"running\":").append(maquina.estaEjecutando()).append(",");
        json.append("\"stepCount\":").append(maquina.getConteoPasos()).append(",");
        json.append("\"headPosition\":").append(maquina.getCinta().getPosicionCabezal()).append(",");
        json.append("\"highlightRuleId\":").append(calcularProximaReglaId()).append(",");
        json.append("\"rules\":").append(reglasJson()).append(",");
        json.append("\"cells\":{");
        boolean first = true;
        for (Map.Entry<Integer, Character> entry : celdas.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
        }
        json.append("},");
        if (resultado == null) {
            json.append("\"lastStep\":null");
        } else {
            json.append("\"lastStep\":{");
            json.append("\"stepNumber\":").append(resultado.getNumeroPaso()).append(",");
            json.append("\"bitRead\":\"").append(resultado.getBitLeido()).append("\",");
            json.append("\"currentState\":\"").append(escape(resultado.getEstadoAnterior())).append("\",");
            json.append("\"currentStateInfo\":").append(obtenerInfoEstado(resultado.getEstadoAnterior())).append(",");
            json.append("\"nextState\":\"").append(escape(resultado.getSiguienteEstado())).append("\",");
            json.append("\"nextStateInfo\":").append(obtenerInfoEstado(resultado.getSiguienteEstado())).append(",");
            json.append("\"headPosition\":").append(resultado.getPosicionCabezal()).append(",");
            json.append("\"halted\":").append(resultado.estaDetenida()).append(",");
            if (resultado.getReglaAplicada() == null) {
                json.append("\"ruleId\":null");
            } else {
                json.append("\"ruleId\":").append(resultado.getReglaAplicada().getIdRegla());
            }
            json.append("}");
        }
        json.append("}");
        return json.toString();
    }

    private String obtenerInfoEstado(String estado) {
        StringBuilder info = new StringBuilder("{");
        info.append("\"id\":\"").append(escape(estado)).append("\",");
        switch(estado.toLowerCase()) {
            case "q0":
                info.append("\"name\":\"Lectura\",");
                info.append("\"description\":\"Estado inicial: Lee el bit actual de la cinta\",");
                info.append("\"phase\":1");
                break;
            case "q1":
                info.append("\"name\":\"Procesamiento\",");
                info.append("\"description\":\"Procesa el bit leído y lo valida\",");
                info.append("\"phase\":2");
                break;
            case "q2":
                info.append("\"name\":\"Escritura/Cálculo\",");
                info.append("\"description\":\"Realiza el cálculo y escribe el resultado\",");
                info.append("\"phase\":3");
                break;
            case "q3":
                info.append("\"name\":\"Verificación\",");
                info.append("\"description\":\"Verifica el resultado y prepara siguiente ciclo\",");
                info.append("\"phase\":4");
                break;
            case "q_accept":
                info.append("\"name\":\"Aceptado\",");
                info.append("\"description\":\"Estado final: Máquina ha terminado exitosamente\",");
                info.append("\"phase\":5");
                break;
            default:
                info.append("\"name\":\"Desconocido\",");
                info.append("\"description\":\"Estado no reconocido\",");
                info.append("\"phase\":0");
        }
        info.append("}");
        return info.toString();
    }

    private int calcularProximaReglaId() {
        if (maquina == null || !maquina.estaEjecutando()) {
            return -1;
        }
        char nextBit = maquina.getCinta().leer();
        for (Transicion regla : maquina.getTransiciones()) {
            if (regla.esValidaPara(maquina.getEstadoActual(), nextBit)) {
                return regla.getIdRegla();
            }
        }
        return -1;
    }

    private String reglasJson() {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Transicion regla : maquina.getTransiciones()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("{");
            json.append("\"id\":").append(regla.getIdRegla()).append(",");
            json.append("\"state\":\"").append(escape(regla.getEstadoActual())).append("\",");
            json.append("\"read\":\"").append(regla.getSimboloLeido()).append("\",");
            json.append("\"write\":\"").append(regla.getSimboloEscrito()).append("\",");
            json.append("\"move\":\"").append(escape(regla.getDireccion())).append("\",");
            json.append("\"next\":\"").append(escape(regla.getSiguienteEstado())).append("\"");
            json.append("}");
        }
        json.append("]");
        return json.toString();
    }

    private String parseField(String json, String key) {
        String token = "\"" + key + "\"";
        int keyIndex = json.indexOf(token);
        if (keyIndex < 0) {
            return "";
        }
        int quoteStart = json.indexOf('"', json.indexOf(':', keyIndex) + 1);
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteStart < 0 || quoteEnd < 0) {
            return "";
        }
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static class StaticHandler implements HttpHandler {
        private final Path webRoot;

        private StaticHandler(Path webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if ("/".equals(requestPath)) {
                requestPath = "/index.html";
            }
            Path file = webRoot.resolve(requestPath.substring(1)).normalize();
            if (!file.startsWith(webRoot) || !Files.exists(file)) {
                byte[] bytes = "Not found".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
                return;
            }
            String contentType = file.toString().endsWith(".html")
                ? "text/html; charset=utf-8"
                : "text/plain; charset=utf-8";
            byte[] bytes = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }
}
