import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Servidor Java para o cliente Python. */
public final class JavaServer {
    private static final int MAX_REQUEST_BYTES = 65536;

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/RPC2", JavaServer::handle);
        server.start();
        System.out.println("Servidor Java em http://" + host + ":" + server.getAddress().getPort() + "/RPC2");
    }

    private static void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        String response;
        try {
            byte[] body = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
            if (body.length > MAX_REQUEST_BYTES) throw new IllegalArgumentException("Requisição grande demais");
            XmlRpc.Call call = XmlRpc.readRequest(body);
            response = XmlRpc.response(dispatch(call.method, call.arguments));
        } catch (Exception error) {
            response = XmlRpc.fault(1, error.getMessage() == null ? "Requisição inválida" : error.getMessage());
        }
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/xml; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static Object dispatch(String method, List<Object> args) {
        switch (method) {
            case "soma":
                require(args, 2);
                return number(args.get(0)) + number(args.get(1));
            case "subtracao":
                require(args, 2);
                return number(args.get(0)) - number(args.get(1));
            case "maiuscula":
                require(args, 1);
                return string(args.get(0)).toUpperCase(Locale.ROOT);
            case "inverter":
                require(args, 1);
                return new StringBuilder(string(args.get(0))).reverse().toString();
            case "contar_palavras":
                require(args, 1);
                String text = string(args.get(0)).strip();
                return text.isEmpty() ? 0 : text.split("\\s+").length;
            default:
                throw new IllegalArgumentException("Método desconhecido: " + method);
        }
    }

    private static void require(List<Object> args, int expected) {
        if (args.size() != expected) throw new IllegalArgumentException("Esperados " + expected + " argumentos");
    }

    private static double number(Object value) {
        if (!(value instanceof Number)) throw new IllegalArgumentException("Esperado número");
        return ((Number) value).doubleValue();
    }

    private static String string(Object value) {
        if (!(value instanceof String)) throw new IllegalArgumentException("Esperado texto");
        return (String) value;
    }
}
