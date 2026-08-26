package dev.rodrigosambade.oca;

import dev.rodrigosambade.net.ConnectivityAndInternetAccess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class GameClient {

    private static final Duration CONNECTIVITY_TIMEOUT = Duration.ofSeconds(2);
    private static final int CONNECT_TIMEOUT_MS = 3_000;

    private GameClient() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: host port nick");
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String nickname = args[2];

        ConnectivityAndInternetAccess.requireInternetForRemoteHost(
                host,
                CONNECTIVITY_TIMEOUT);

        runClient(host, port, nickname);
    }

    private static void runClient(String host, int port, String nickname)
            throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);

            try (BufferedReader input = reader(socket);
                 BufferedWriter output = writer(socket);
                 BufferedReader console = new BufferedReader(new InputStreamReader(
                         System.in,
                         StandardCharsets.UTF_8))) {
                send(output, nickname);
                processServerMessages(input, output, console);
            }
        }
    }

    private static void processServerMessages(
            BufferedReader input,
            BufferedWriter output,
            BufferedReader console) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);

            if (line.equals("TURN")) {
                System.out.print("ENTER para tirar…");
                console.readLine();
                send(output, "ROLL");
            }

            if (line.startsWith("WIN|")) {
                return;
            }
        }
    }

    private static BufferedReader reader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(
                socket.getInputStream(),
                StandardCharsets.UTF_8));
    }

    private static BufferedWriter writer(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(),
                StandardCharsets.UTF_8));
    }

    private static void send(BufferedWriter output, String message) throws IOException {
        output.write(message);
        output.newLine();
        output.flush();
    }
}
