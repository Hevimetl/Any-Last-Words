package server.utility;

import CommonTypes.AuthResult;
import CommonTypes.UserRole;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();


    private SessionManager() {

    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    private static class SessionData {
        final String username;
        final UserRole role;


        SessionData(String username, UserRole role) {
            this.username = username;
            this.role = role;
        }
    }

    // Create new session
    public AuthResult createSession(String username, UserRole role) {
        synchronized (activeSessions) {
            for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
                SessionData data = entry.getValue();
                if (data.username.equals(username)) {
                    // Return existing token
                    AuthResult result = new AuthResult();
                    result.token = entry.getKey();
                    result.userName = username;
                    result.role = data.role;
                    System.out.println(result.token);
                    return result;
                }
            }
        }

        // Create new session if no existing valid one found
        String token = UUID.randomUUID().toString();
        SessionData sessionData = new SessionData(username, role);

        System.out.println(token);

        synchronized (activeSessions) {
            activeSessions.put(token, sessionData);
        }

        AuthResult result = new AuthResult();
        result.token = token;
        result.userName = username;
        result.role = role;

        return result;
    }

    // Invalidate session
    public void invalidateSession(String token) {
        activeSessions.remove(token);
    }



    public String getUsername(String token) {
        SessionData data = activeSessions.get(token);
        return (data != null) ? data.username : null;
    }

    public boolean isUserLoggedIn(String username) {
         return activeSessions.values().stream()
                       .anyMatch(sd -> sd.username.equals(username));
    }

    public Map<String, SessionData> getActiveSessions() {
        return activeSessions;
    }

    public String getTokenByUsername(String username) {
        synchronized (activeSessions) {
            for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
                if (entry.getValue().username.equals(username)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public boolean isUserConnected(String token) {
        // Check if the token exists and is still valid
        SessionData data = activeSessions.get(token);
        return data != null;
        // Verify the session hasn't expired
    }
}