import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class ServidorAnimacao {

    private static final int PORTA = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer servidor;
        try {
            servidor = HttpServer.create(new InetSocketAddress(PORTA), 0);
        } catch (BindException e) {
            System.err.println("ERRO: A porta " + PORTA + " ja esta em uso por outro programa!");
            System.err.println("Feche o outro programa ou mude PORTA no codigo Java.");
            return;
        }

        servidor.setExecutor(Executors.newCachedThreadPool());

        // Rota principal: entrega o arquivo index.html
        servidor.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                adicionarCors(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }

                String caminho = exchange.getRequestURI().getPath();
                if (!"/".equals(caminho) && !"/index.html".equals(caminho)) {
                    byte[] msg = "404 - Nao encontrado".getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(404, msg.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg);
                    }
                    return;
                }

                Path arquivoHtml = localizarHtml();
                if (arquivoHtml == null) {
                    byte[] erro = ("Arquivo index.html nao encontrado na pasta: "
                            + Paths.get("").toAbsolutePath())
                            .getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(404, erro.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(erro);
                    }
                    return;
                }

                byte[] htmlBytes = Files.readAllBytes(arquivoHtml);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(htmlBytes);
                }
            }
        });

        // Rota de streaming (SSE): o Java controla e envia a animacao de texto
        servidor.createContext("/animacao", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                adicionarCors(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }

                String texto = extrairParametroTexto(exchange.getRequestURI().getRawQuery());
                if (texto == null || texto.trim().isEmpty()) {
                    texto = "Anima\u00e7\u00e3o de texto controlada pelo Java!";
                }
                if (texto.codePointCount(0, texto.length()) > 200) {
                    texto = texto.substring(0, texto.offsetByCodePoints(0, 200));
                }

                exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=UTF-8");
                exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-transform");
                exchange.getResponseHeaders().set("X-Accel-Buffering", "no");
                exchange.sendResponseHeaders(200, 0);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("retry: 2000\n\n".getBytes(StandardCharsets.UTF_8));
                    os.flush();

                    while (true) {
                        int i = 0;
                        while (i < texto.length()) {
                            int proximo = texto.offsetByCodePoints(i, 1);
                            enviarEvento(os, texto.substring(0, proximo));
                            Thread.sleep(90);
                            i = proximo;
                        }

                        Thread.sleep(1500);

                        i = texto.length();
                        while (i > 0) {
                            int anterior = texto.offsetByCodePoints(i, -1);
                            enviarEvento(os, texto.substring(0, anterior));
                            Thread.sleep(45);
                            i = anterior;
                        }

                        Thread.sleep(400);
                    }
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        servidor.start();
        System.out.println("Servidor Java iniciado com sucesso!");
        System.out.println("Acesse no navegador: http://localhost:" + PORTA);
        System.out.println("Mantenha este terminal aberto enquanto usa a pagina.");
    }

    private static Path localizarHtml() {
        String[] caminhos = {
            "index.html",
            "src/index.html",
            "src/main/resources/index.html",
            "public/index.html"
        };
        for (String c : caminhos) {
            Path p = Paths.get(c);
            if (Files.exists(p)) {
                return p;
            }
        }
        return null;
    }

    private static void adicionarCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
    }

    private static void enviarEvento(OutputStream os, String texto) throws IOException {
        String json = "{\"texto\":\"" + escaparJson(texto) + "\"}";
        String evento = "data: " + json + "\n\n";
        os.write(evento.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }

    private static String extrairParametroTexto(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] pares = query.split("&");
        for (String par : pares) {
            int idx = par.indexOf('=');
            if (idx > 0) {
                String chave = par.substring(0, idx);
                if ("texto".equals(chave)) {
                    try {
                        return URLDecoder.decode(par.substring(idx + 1), "UTF-8");
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static String escaparJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}