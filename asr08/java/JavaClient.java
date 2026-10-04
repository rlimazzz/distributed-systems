import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Cliente Java para o servidor Python. */
public final class JavaClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Uso: java JavaClient <host> <port> <método> [argumentos...]\n"
                    + "Exemplo: java JavaClient 127.0.0.1 8000 soma 3 4");
            System.exit(2);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String method = args[2];
        int expected = method.equals("soma") || method.equals("subtracao") ? 2 : 1;
        if (args.length != expected + 3) {
            System.err.println("O método " + method + " requer " + expected + " argumento(s).");
            System.exit(2);
        }
        Object[] values = new Object[expected];
        for (int i = 0; i < expected; i++) {
            if (expected == 2) {
                values[i] = Double.parseDouble(args[i + 3]);
            } else {
                values[i] = args[i + 3];
            }
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://" + host + ":" + port + "/RPC2"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(XmlRpc.request(method, values), StandardCharsets.UTF_8))
                .build();
        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) throw new IllegalStateException("HTTP " + response.statusCode());
        System.out.println(XmlRpc.readResponse(response.body()));
    }
}
