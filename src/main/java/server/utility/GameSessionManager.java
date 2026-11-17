package server.utility;

import CommonTypes.GameNotFoundException;
import GameNotifications.GameCallback;
import server.Server;
import server.utility.objects.GameSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameSessionManager {
    private static final GameSessionManager INSTANCE = new GameSessionManager();
    private final ConcurrentMap<String, GameSession> activeGames = new ConcurrentHashMap<>();

    private GameSessionManager() {}

    public static GameSessionManager getInstance() {
        return INSTANCE;
    }

    public String createGame(String creatorToken) {
        String gameId = UUID.randomUUID().toString();
        GameSession session = new GameSession(gameId, creatorToken);
        activeGames.put(gameId, session);
        return gameId;
    }

    public GameSession getGame(String gameId) throws GameNotFoundException {
        GameSession session = activeGames.get(gameId);
        if (session == null) throw new GameNotFoundException();
        return session;
    }

    public void registerCallback(String gameId, String playerToken, GameCallback callback) {
        GameSession game = activeGames.get(gameId);
        if (game != null) {
            game.registerCallback(playerToken, callback);
        }
    }


    public void cleanupAbandonedGames() {
        Iterator<Map.Entry<String, GameSession>> it = activeGames.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, GameSession> entry = it.next();
            GameSession session = entry.getValue();

            // Remove games where host is disconnected
            if (!SessionManager.getInstance().isUserConnected(session.getCreatorToken())) {
                it.remove();
                continue;
            }

            // Remove empty games
            if (session.getPlayerStates().isEmpty()) {
                it.remove();
            }
        }
    }

    public boolean removePlayerFromGame(String gameId, String playerToken) throws GameNotFoundException {
        GameSession game = getGame(gameId);
        synchronized (game) {
            if (game.getCreatorToken().equals(playerToken)) {
                activeGames.remove(gameId);
                return true;
            }
            game.getPlayerStates().remove(playerToken);

            if (game.getPlayerStates().isEmpty()) {
                activeGames.remove(gameId);
            }
            return true;
        }
    }

    public void removeGame(String gameId) throws SQLException {
        System.out.println("Adding TO SQL");
        GameSession game = activeGames.remove(gameId);
        if (game == null || game.getGameState() != GameSession.GameState.FINISHED) {
            return;
        }

        Connection con = Server.getConnection();
        try {
            con.setAutoCommit(false);

            String winnerToken = game.getRoundWinnerName();
            String winnerUsername = SessionManager.getInstance().getUsername(winnerToken);
            if (winnerUsername == null) {
                throw new IllegalStateException("No session found for winner token: " + winnerToken);
            }

            final String lookupUserSQL = "SELECT userid FROM users WHERE username = ?";
            int winnerUserId;
            try (PreparedStatement psLookup = con.prepareStatement(lookupUserSQL)) {
                psLookup.setString(1, winnerUsername);
                try (ResultSet rs = psLookup.executeQuery()) {
                    if (rs.next()) {
                        winnerUserId = rs.getInt("userid");
                    } else {
                        throw new SQLException("User not found: " + winnerUsername);
                    }
                }
            }

            final String insertGameSQL = "INSERT INTO games (gameid, roundcount, gamewinner) VALUES (?, ?, ?)";
            try (PreparedStatement psGame = con.prepareStatement(insertGameSQL)) {
                psGame.setString(1, game.getGameID());
                psGame.setInt(2, game.getCurrentRound());
                psGame.setInt(3, winnerUserId);
                psGame.executeUpdate();
            }

            final String insertPlayerSQL = "INSERT INTO gameplayers (gameid, playerid) VALUES (?, ?)";
            try (PreparedStatement psPlayer = con.prepareStatement(insertPlayerSQL)) {
                SessionManager sm = SessionManager.getInstance();
                for (String playerToken : game.getPlayerStates().keySet()) {
                    String username = sm.getUsername(playerToken);
                    if (username == null) continue;

                    try (PreparedStatement psLookup = con.prepareStatement(lookupUserSQL)) {
                        psLookup.setString(1, username);
                        try (ResultSet rs = psLookup.executeQuery()) {
                            if (rs.next()) {
                                int playerId = rs.getInt("userid");
                                psPlayer.setString(1, game.getGameID());
                                psPlayer.setInt(2, playerId);
                                psPlayer.addBatch();
                            }
                        }
                    }
                }
                psPlayer.executeBatch();
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(true);
        }

        System.out.println("DOne SQL");
    }

    public List<GameSession> listAllSessions() {
        return new ArrayList<>(activeGames.values());
    }

    public boolean isPlayerInAnyGame(String playerToken) {
        return activeGames.values().stream()
                .anyMatch(game -> game.getPlayerStates().containsKey(playerToken) ||
                        game.getCreatorToken().equals(playerToken));
    }
}