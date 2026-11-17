package server.servant;
import CommonTypes.*;


import GameNotifications.GameCallback;
import PlayerService.PlayerPOA;
import server.utility.GameSessionManager;
import server.utility.SessionManager;
import server.utility.objects.GameSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerImpl extends PlayerPOA {
    private static Connection con;
    private final GameSessionManager gameManager = GameSessionManager.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public PlayerImpl(Connection con) {
        this.con = con;
        // Schedule periodic cleanup of abandoned games
        scheduler.scheduleAtFixedRate(() -> {
            try {
                gameManager.cleanupAbandonedGames();
            } catch (Exception e) {
                System.err.println("Game cleanup failed: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }


    @Override
    public GameInfo[] getJoinableGames(String token) {
        List<GameSession> sessions = new ArrayList<>(gameManager.listAllSessions());

        // Remove abandoned games
        sessions.removeIf(session ->
                session.getPlayerStates().isEmpty() ||
                        !sessionManager.isUserConnected(session.getCreatorToken())
        );

        List<GameSession> waiting = sessions.stream()
                .filter(s -> s.getGameState() == GameSession.GameState.WAITING)
                .filter(s -> !s.getCreatorToken().equals(token))
                .filter(s -> s.getPlayerStates().size() < 5)
                .filter(s -> sessionManager.isUserConnected(s.getCreatorToken()))
                .collect(Collectors.toList());

        GameInfo[] dto = new GameInfo[waiting.size()];
        for (int i = 0; i < waiting.size(); i++) {
            GameSession s = waiting.get(i);
            GameInfo g = new GameInfo();
            g.gameState = true;
            g.gameId = s.getGameID();
            String creatorToken = s.getCreatorToken();
            String hostName = sessionManager.getUsername(creatorToken);
            g.hostName = (hostName != null ? hostName : "<disconnected>");
            g.waitingTime = s.getCurrentWaitingTime();
            g.roundTime = s.getCurrentRoundTime();
            g.players = s.getPlayerStates().values().toArray(new PlayerGameState[0]);
            g.wordLength = s.getCurrentWord().length();
            dto[i] = g;
        }
        return dto;
    }


    @Override
    public boolean cancelGameRequest(String token, String gameId) {
        try {
            GameSession session = gameManager.getGame(gameId);
            synchronized (session) {
                if (session.getCreatorToken().equals(token)) {
                    session.getPlayerStates().clear();
                    gameManager.removeGame(gameId);
                    return true;
                }
                return false;
            }
        } catch (GameNotFoundException | SQLException e) {
            return false;
        }
    }

    @Override
    public boolean leaveGame(String token, String gameId) {
        try {
            GameSession session = gameManager.getGame(gameId);
            synchronized (session) {
                session.getPlayerStates().remove(token);

                // Remove game if empty and player wasn't host
                if (session.getPlayerStates().isEmpty() &&
                        !session.getCreatorToken().equals(token)) {
                    gameManager.removeGame(gameId);
                }
                return true;
            }
        } catch (GameNotFoundException | SQLException e) {
            return false;
        }
    }

    @Override
    public GameInfo getGameInfo(String token, String gameID) {
        GameSession session = null;
        try {
            session = GameSessionManager.getInstance().getGame(gameID);
        } catch (GameNotFoundException e) {
            throw new RuntimeException(e);
        }


        GameInfo gameInfo = new GameInfo();
        gameInfo.gameId = session.getGameID();
        if (session.getGameState() == GameSession.GameState.WAITING){
            gameInfo.gameState = false;
        } else
            gameInfo.gameState = true;

        gameInfo.hostName = SessionManager.getInstance().getUsername(session.getCreatorToken());
        gameInfo.waitingTime = session.getCurrentWaitingTime();
        gameInfo.roundTime = session.getCurrentRoundTime();
        gameInfo.players = session.getPlayerStates().values().toArray(new PlayerGameState[0]);
        gameInfo.wordLength = session.getCurrentWord().length();


        return gameInfo;


    }

    @Override
    public String joinGame(String token, String gameId) throws NoPlayersFoundException {
        GameSession game;
        try {
            game = GameSessionManager.getInstance().getGame(gameId);
        } catch (GameNotFoundException e) {
            throw new NoPlayersFoundException("Game not found: " + gameId);
        }

        synchronized (game) {
            if (game.getGameState() != GameSession.GameState.WAITING) {
                throw new IllegalStateException("Game is not in waiting state");
            }

            // If player already in the game, return current info
            if (game.getPlayerStates().containsKey(token)) {
                int waitingCount = game.getPlayerStates().size();
                return String.format(
                        "Already in game %s. %d players waiting.",
                        gameId, waitingCount
                );
            }

            // Check max players only for new joiners
            if (game.getPlayerStates().size() >= 5) {
                throw new IllegalStateException("Maximum number of players (5) already joined");
            }

            game.createDefaultPlayerGameState(token);
            int waitingCount = game.getPlayerStates().size();

            return String.format(
                    "Successfully joined game %s. %d players waiting.",
                    gameId, waitingCount
            );
        }
    }






    @Override
    public String createGame(String token) {
       return gameManager.createGame(token);
    }

    @Override
    public void markReady(String token, String gameID) {
        GameSession game;
        try {
            game = GameSessionManager.getInstance().getGame(gameID);
            System.out.println("All tokens in game " + gameID + ": " + game.getPlayerStates().keySet());
        } catch (GameNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (!game.getPlayerStates().containsKey(token)) {
            throw new IllegalStateException("Player not in this game");
        }

        PlayerGameState playerState = game.getPlayerStates().get(token);
        playerState.isReady = !playerState.isReady; // toggle readiness

        System.out.printf("Player %s is now %s\n",
                token,
                playerState.isReady ? "READY" : "NOT READY"
        );
    }

    @Override
    public int getWaitingTime(String token, String gameID)
            throws GameNotFoundException, NoPlayersFoundException {
        GameSession session = GameSessionManager.getInstance().getGame(gameID);
        int timeLeft = session.getCurrentWaitingTime();
        // if time’s up and fewer than 2 players, cancel
        if (timeLeft <= 1 && session.getPlayerStates().size() < 2) {
            // clean up the session so it can’t be joined later
            throw new NoPlayersFoundException(
                    //stop thread
                    "No other player joined within the waiting time."
            );
        }
        return timeLeft;
    }

    @Override
    public void waitForOtherPlayers(String token, String gameId) throws GameNotFoundException {
        GameSession session = gameManager.getGame(gameId);
        synchronized (session) {
            if (session.getGameState() != GameSession.GameState.WAITING) {
                throw new IllegalStateException("Game is not in waiting state");
            }
            // Restart the waiting timer logic here
            session.restartWaitingTimer(); // Assuming you have a method to restart the timer
        }
    }
    @Override
    public int getRoundTime(String token, String gameID) throws GameNotFoundException {
        GameSession session = GameSessionManager.getInstance().getGame(gameID);
        return session.getCurrentRoundTime();
    }

    @Override
    public boolean rejoinGame(String token, String gameId) throws GameNotFoundException {
        return false;
    }

    @Override
    public boolean isGameOver(String token, String gameId) {
        return true;
    }

    @Override
    public boolean isRoundWinner(String token, String gameId) {
        return false;
    }

    @Override
    public boolean isGameWinner(String token, String gameId) {
        return false;
    }

    @Override
    public String getWordProgress(String token, String gameId) {
        return "";
    }

    @Override
    public String getFullWord(String token, String gameId) {
        try {
            return GameSessionManager.getInstance().getGame(gameId).getCurrentWord();
        } catch (GameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getRoundWinner(String token, String gameId) {
        try {
            GameSession session = GameSessionManager.getInstance().getGame(gameId);
            return session.getPlayerStates().values().stream()
                    .max(Comparator.comparingInt(p -> p.roundWins))
                    .map(p -> p.userName)
                    .orElse("No winner yet");
        } catch (Exception e) {
            return "Error determining winner";
        }
    }

    @Override
    public int[] sendGuess(String token, String gameId, char guess)
            throws InvalidGuessException, LetterAlreadyGuessedException, GuessLimitReachedException {

        GameSession session;
        try {
            session = gameManager.getGame(gameId);
        } catch (GameNotFoundException e) {
            throw new RuntimeException("Game not found", e);
        }

        int[] result;

        synchronized (session) {
            validateGuess(token, session, guess);

            String currentWord = session.getCurrentWord().toLowerCase();
            guess = Character.toLowerCase(guess);
            List<Integer> positions = new ArrayList<>();
            for (int i = 0; i < currentWord.length(); i++) {
                if (currentWord.charAt(i) == guess) {
                    positions.add(i);
                }
            }

            PlayerGameState playerState = session.getPlayerStates().get(token);
            String[] newGuessedLetters = Arrays.copyOf(
                    playerState.guessedLetters,
                    playerState.guessedLetters.length + 1
            );
            newGuessedLetters[newGuessedLetters.length - 1] = String.valueOf(guess);
            playerState.guessedLetters = newGuessedLetters;

            if (positions.isEmpty()) {
                playerState.remainingAttempts--;
            }

            session.checkRoundCompletion();

            result = positions.stream().mapToInt(i -> i).toArray();
        }

        System.out.println(session.getCurrentWord());
        return result;
    }

    private void validateGuess(String token, GameSession session, char guess)
            throws InvalidGuessException, LetterAlreadyGuessedException, GuessLimitReachedException {


        if (!Character.isLetter(guess)) {
            throw new InvalidGuessException("Invalid character: " + guess);
        }


        PlayerGameState state = session.getPlayerStates().get(token);
        if (Arrays.asList(state.guessedLetters).contains(String.valueOf(guess))) {
            throw new LetterAlreadyGuessedException("Already guessed: " + guess);
        }

        // Check remaining attempts
        if (state.remainingAttempts <= 0) {
            throw new GuessLimitReachedException("No guesses remaining");
        }
    }
    @Override
    public int getRemainingAttempts(String token, String gameId) {
        try {
            GameSession session = GameSessionManager.getInstance().getGame(gameId);
            return session.getPlayerStates().get(token).remainingAttempts;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getWrongGuessCount(String token, String gameId) {
        return 0;
    }

    @Override
    public int getWordLength(String token, String gameId) {
        return 0;
    }

    @Override
    public String[] getTopPlayers(int count) {
        List<String> topPlayers = new ArrayList<>();
        String query = "SELECT u.userid, u.username, COUNT(*) AS wins " +
                "FROM users u " +
                "JOIN gameplayers gp ON u.userid = gp.playerid " +
                "JOIN games g ON gp.gameid = g.gameid " +
                "WHERE gp.playerid = g.gamewinner " +
                "GROUP BY u.userid, u.username " +
                "ORDER BY wins DESC " +
                "LIMIT ?;";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, count);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long userId = rs.getLong("userid");
                String username = rs.getString("username");
                long wins = rs.getLong("wins");

                // Format: userId,username,wins
                topPlayers.add(String.format("%d,%s,%d", userId, username, wins));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert the List to an array
        return topPlayers.toArray(new String[0]);
    }


    @Override
    public void registerCallback(String gameId, String playerToken, GameCallback callback) {
        GameSessionManager.getInstance().registerCallback(gameId, playerToken, callback);
    }

    public static String getSelectedCharacter(String username) throws SQLException {
        String sql = "SELECT selectedCharacter FROM users WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Bangs";
                }

                String character = rs.getString("selectedCharacter");
                return rs.wasNull() ? "Bangs" : character;
            }
        }
    }

    @Override
    public void changeCharacter(String token, String character) {
        String username = SessionManager.getInstance().getUsername(token);
        String sql = "UPDATE users SET selectedCharacter = ? WHERE username = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, character);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update character: " + e.getMessage());
        }
    }
}
