package server.utility.objects;

public class PlayerData {
    private final long userId;
    private final String username;
    private final long wins;

    public PlayerData(long userId, String username, long wins) {
        this.userId = userId;
        this.username = username;
        this.wins = wins;
    }

    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public long getWins() { return wins; }
}
