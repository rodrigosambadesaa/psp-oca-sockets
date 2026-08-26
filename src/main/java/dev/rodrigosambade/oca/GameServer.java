package dev.rodrigosambade.oca;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class GameServer implements AutoCloseable {

    private static final int DEFAULT_PORT = 5_000;
    private static final int DEFAULT_PLAYERS = 2;

    private final ServerSocket serverSocket;
    private final int expectedPlayers;
    private final List<Player> players = new ArrayList<>();
    private final GooseGame game = new GooseGame();
    private final RandomGenerator random = RandomGenerator.getDefault();

    private volatile boolean closed;

    public GameServer(int port, int expectedPlayers) throws IOException {
        if (expectedPlayers < 2) {
            throw new IllegalArgumentException("At least two players are required");
        }

        this.expectedPlayers = expectedPlayers;
        this.serverSocket = new ServerSocket(port);
    }

    public int port() {
        return serverSocket.getLocalPort();
    }

    public void run() throws IOException {
        acceptPlayers();
        broadcastPlayerList();
        playGame();
    }

    private void acceptPlayers() throws IOException {
        while (players.size() < expectedPlayers) {
            Socket socket = serverSocket.accept();
            registerPlayer(socket);
        }
    }

    private void registerPlayer(Socket socket) throws IOException {
        try {
            Player player = new Player(socket);
            if (nicknameExists(player.nickname())) {
                player.send("ERROR|duplicate nick");
                socket.close();
                return;
            }

            players.add(player);
            player.send("WELCOME|" + (players.size() - 1));
            broadcast("JOIN|" + player.nickname());
        } catch (IOException exception) {
            socket.close();
            throw exception;
        }
    }

    private boolean nicknameExists(String nickname) {
        return players.stream()
                .anyMatch(player -> player.nickname().equalsIgnoreCase(nickname));
    }

    private void broadcastPlayerList() throws IOException {
        String nicknames = String.join(",", players.stream()
                .map(Player::nickname)
                .toList());
        broadcast("PLAYERS|" + nicknames);
    }

    private void playGame() throws IOException {
        int turn = 0;

        while (!closed) {
            Player player = players.get(turn);

            if (skipTurnIfRequired(player)) {
                turn = nextTurn(turn);
                continue;
            }

            TurnResult result = playTurn(player);
            if (result.gameFinished()) {
                return;
            }
            if (result.advanceTurn()) {
                turn = nextTurn(turn);
            }
        }
    }

    private boolean skipTurnIfRequired(Player player) throws IOException {
        if (player.skipTurns() <= 0) {
            return false;
        }

        player.decrementSkipTurns();
        broadcast("SKIP|" + player.nickname() + "|" + player.skipTurns());
        return true;
    }

    private TurnResult playTurn(Player player) throws IOException {
        player.send("TURN");
        String command = player.readCommand();

        if (command == null) {
            broadcast("LEAVE|" + player.nickname());
            return new TurnResult(true, false);
        }

        if (!command.equals("ROLL")) {
            player.send("ERROR|expected ROLL");
            return new TurnResult(false, false);
        }

        int roll = random.nextInt(1, 7);
        GooseGame.Move move = game.apply(player.position(), roll);
        player.apply(move);
        broadcastMove(player, roll, move);

        if (move.won()) {
            broadcast("WIN|" + player.nickname());
            return new TurnResult(true, false);
        }

        return new TurnResult(false, !move.extraTurn());
    }

    private void broadcastMove(Player player, int roll, GooseGame.Move move)
            throws IOException {
        broadcast("MOVE|"
                + player.nickname()
                + "|" + roll
                + "|" + move.to()
                + "|" + move.message());
    }

    private int nextTurn(int turn) {
        return (turn + 1) % players.size();
    }

    private void broadcast(String message) throws IOException {
        for (Player player : players) {
            player.send(message);
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        IOException failure = null;

        for (Player player : players) {
            try {
                player.close();
            } catch (IOException exception) {
                failure = exception;
            }
        }

        try {
            serverSocket.close();
        } catch (IOException exception) {
            failure = exception;
        }

        if (failure != null) {
            throw failure;
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0
                ? Integer.parseInt(args[0])
                : DEFAULT_PORT;
        int players = args.length > 1
                ? Integer.parseInt(args[1])
                : DEFAULT_PLAYERS;

        try (GameServer server = new GameServer(port, players)) {
            server.run();
        }
    }

    private record TurnResult(boolean gameFinished, boolean advanceTurn) {
    }

    private static final class Player implements AutoCloseable {

        private final Socket socket;
        private final BufferedReader input;
        private final BufferedWriter output;
        private final String nickname;

        private int position;
        private int skipTurns;

        private Player(Socket socket) throws IOException {
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(),
                    StandardCharsets.UTF_8));
            this.output = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(),
                    StandardCharsets.UTF_8));

            String receivedNickname = input.readLine();
            if (receivedNickname == null || receivedNickname.isBlank()) {
                throw new IOException("nick required");
            }
            this.nickname = receivedNickname.trim();
        }

        private String nickname() {
            return nickname;
        }

        private int position() {
            return position;
        }

        private int skipTurns() {
            return skipTurns;
        }

        private void decrementSkipTurns() {
            skipTurns--;
        }

        private String readCommand() throws IOException {
            return input.readLine();
        }

        private void apply(GooseGame.Move move) {
            position = move.to();
            skipTurns = move.skipTurns();
        }

        private void send(String message) throws IOException {
            output.write(message);
            output.newLine();
            output.flush();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
