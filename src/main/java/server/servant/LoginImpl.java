package server.servant;

import CommonTypes.*;
import GameNotifications.GameCallback;
import server.utility.GameSessionManager;
import server.utility.SessionManager;
import server.utility.objects.GameSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginImpl extends LoginPOA {
    private Connection con;
    private static Map<String, GameCallback> callbackMap = new HashMap<>();

    public LoginImpl(Connection con) {
        this.con = con;
    }

    @Override
    public AuthResult authenticate(String userName, String password)
            throws InvalidCredentialsException,
            UserDoesNotExistException,
            UserAlreadyLoggedInException {
        String sql = "SELECT password, isadmin FROM users WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new UserDoesNotExistException(
                            "User " + userName + " does not exist."
                    );
                }

                String storedPassword = rs.getString("password");
                boolean isAdmin = rs.getBoolean("isadmin");

                if (!storedPassword.equals(password)) {
                    throw new InvalidCredentialsException(
                            "Invalid password for user: " + userName
                    );
                }

                SessionManager sm = SessionManager.getInstance();
                if (sm.isUserLoggedIn(userName)) {
                    GameCallback callback = callbackMap.get(userName);
                    if (callback != null) {
                        callback.notifyReLogin(userName);
                    }
                }

                UserRole role = isAdmin ? UserRole.ADMIN : UserRole.PLAYER;
                return sm.createSession(userName, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error occurred.", e);
        }
    }

    @Override
    public void logoutAccount(String token) {
        SessionManager sm = SessionManager.getInstance();
        String username = sm.getUsername(token);
        unregisterCallback(username);
    }

    @Override
    public String isLoggedIn(String token) {
        String userName = SessionManager.getInstance().getUsername(token);

        GameSessionManager gameManager = GameSessionManager.getInstance();
        for (GameSession session : gameManager.listAllSessions()) {
            String creatorToken = session.getCreatorToken();
            if (creatorToken.equals(token)) {
                return  session.getGameID();
            } else if (session.getPlayerStates().containsKey(token)) {
                return  session.getGameID();
            }
        }
        return "Not Joined";
    }

    @Override
    public void registerCallback(String username, GameCallback callback) {
        callbackMap.put(username, callback);
    }

    @Override
    public void unregisterCallback(String username) {
        callbackMap.remove(username);
    }

    @Override
    public void forceLogout(String username)
            throws UserNotFoundException, OperationNotAllowedException {
        SessionManager sm = SessionManager.getInstance();
        String token = sm.getTokenByUsername(username); // Use new method
        if (token == null) {
            throw new UserNotFoundException("User not found: " + username);
        }
        logoutAccount(token); // This calls sm.invalidateSession(token)
    }


}
