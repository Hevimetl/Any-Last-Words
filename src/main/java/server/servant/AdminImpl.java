package server.servant;

import AdminService.AdminPOA;
import CommonTypes.*;
import server.utility.SettingsManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminImpl extends AdminPOA {
    private Connection con;

    public AdminImpl(Connection con) {
        this.con = con;
    }

    @Override
    public void createUser(String adminToken, String username, String password, UserRole role)
            throws AdminPrivilegeRequiredException, UserAlreadyExistsException, AuthenticationException {

        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }

        // Check if user exists
        String checkQuery = "SELECT username FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new UserAlreadyExistsException();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }


        // isAdmin = 1 means admin, 0 means player
        String insertQuery = "INSERT INTO users (username, password, isAdmin) VALUES (?, ?,  ?)";
        try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setInt(3, (role == UserRole.ADMIN) ? 1 : 0);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Override
    public UserInfo getUser(String adminToken, String username)
            throws AdminPrivilegeRequiredException, UserNotFoundException {
        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }
        String query = "SELECT u.userid, u.username, password, COUNT(*) AS wins " +
                "FROM users u " +
                "JOIN gameplayers gp ON u.userid = gp.playerid " +
                "JOIN games g ON gp.gameid = g.gameid " +
                "WHERE gp.playerid = g.gamewinner " +
                "GROUP BY u.userid, u.username " +
                "ORDER BY wins " +
                "LIMIT ?;";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserInfo user = new UserInfo();
                user.userId = rs.getString("userid");
                user.username = rs.getString("username");
                user.password = rs.getString("password");
                int wins = rs.getInt("wins");
                user.winCount = wins;

                // Correct interpretation: isAdmin = 1 means ADMIN, else PLAYER
                int isAdmin = rs.getInt("isAdmin");
                user.role = (isAdmin == 1) ? UserRole.ADMIN : UserRole.PLAYER;

                return user;
            } else {
                throw new UserNotFoundException();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Override
    public UserInfo[] getAllUsers(String adminToken) throws AdminPrivilegeRequiredException {
        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }

        List<UserInfo> users = new ArrayList<>();
        String query = "SELECT u.userid, u.username, u.password, u.isAdmin, " +
                "COUNT(g.gameid) AS wins " +
                "FROM users u " +
                "LEFT JOIN gameplayers gp ON u.userid = gp.playerid " +
                "LEFT JOIN games g ON gp.gameid = g.gameid AND gp.playerid = g.gamewinner " +
                "WHERE u.isAdmin = 0 " +
                "GROUP BY u.userid, u.username, u.password, u.isAdmin";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                UserInfo user = new UserInfo();
                user.userId = rs.getString("userid");
                user.username = rs.getString("username");
                user.password = rs.getString("password");
                user.winCount = rs.getInt("wins");
                user.role = UserRole.PLAYER;  // Simplified since we're only getting players now

                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
        return users.toArray(new UserInfo[0]);
    }

    @Override
    public void updateUser(String adminToken, String currentUsername, String newUsername, String newPassword)
            throws AdminPrivilegeRequiredException, UserNotFoundException {

        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }

        try {
            con.setAutoCommit(false);

            // Check if user exists
            try (PreparedStatement checkStmt = con.prepareStatement(
                    "SELECT username FROM users WHERE username = ?")) {
                checkStmt.setString(1, currentUsername);
                if (!checkStmt.executeQuery().next()) {
                    throw new UserNotFoundException();
                }
            }

            // Build dynamic update query
            StringBuilder queryBuilder = new StringBuilder("UPDATE users SET ");
            boolean needsComma = false;

            if (newUsername != null && !newUsername.isEmpty()) {
                queryBuilder.append("username = ?");
                needsComma = true;
            }

            if (newPassword != null && !newPassword.isEmpty()) {
                if (needsComma) queryBuilder.append(", ");
                queryBuilder.append("password = ?");
            }

            queryBuilder.append(" WHERE username = ?");

            try (PreparedStatement updateStmt = con.prepareStatement(queryBuilder.toString())) {
                int paramIndex = 1;

                if (newUsername != null && !newUsername.isEmpty()) {
                    updateStmt.setString(paramIndex++, newUsername);
                }

                if (newPassword != null && !newPassword.isEmpty()) {
                    updateStmt.setString(paramIndex++, newPassword);
                }

                updateStmt.setString(paramIndex, currentUsername);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    con.rollback();
                    throw new RuntimeException("No rows were updated");
                }
                con.commit();
            }
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("Database error: " + e.getMessage());
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    @Override
    public void deleteUser(String adminToken, String username)
            throws AdminPrivilegeRequiredException, UserNotFoundException, OperationNotAllowedException {

        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }

        // Prevent admin from deleting themselves
        String currentUserQuery = "SELECT username FROM users WHERE userid = ?";
        try (PreparedStatement currentUserStmt = con.prepareStatement(currentUserQuery)) {
            currentUserStmt.setString(1, adminToken);
            ResultSet rs = currentUserStmt.executeQuery();
            if (rs.next() && rs.getString("username").equals(username)) {
                throw new OperationNotAllowedException("Cannot delete your own account");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }

        // Check if user exists
        String checkQuery = "SELECT username FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                throw new UserNotFoundException();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }

        // Delete user
        String deleteQuery = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement deleteStmt = con.prepareStatement(deleteQuery)) {
            deleteStmt.setString(1, username);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Override
    public String[] searchUsers(String adminToken, String searchQuery) throws AdminPrivilegeRequiredException {
        // Implementation needed
        return new String[0];
    }

    @Override
    public Settings getTimeSettings(String adminToken) throws AdminPrivilegeRequiredException {
        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }
        return SettingsManager.getInstance().getSettings();
    }

    @Override
    public void updateTimeSettings(String adminToken, Settings newSettings)
            throws AdminPrivilegeRequiredException, InvalidTimeSettingsException {
        if (!validateAdmin(adminToken)) {
            throw new AdminPrivilegeRequiredException();
        }
        if (newSettings.waitingTime <= 0 || newSettings.roundTime <= 0) {
            throw new InvalidTimeSettingsException();
        }
        SettingsManager.getInstance().updateSettings(newSettings);
    }

    @Override
    public String[] getAllActiveGames(String adminToken) throws AdminPrivilegeRequiredException {
        // Implementation needed
        return new String[0];
    }

    private boolean validateAdmin(String adminToken) {
        // Simple stub - you should implement your real validation here
        return true;
    }
}

