package client.player.model;

import PlayerService.Player;
import CommonTypes.AuthResult;

import java.util.Arrays;
import java.util.List;

public class PlayerLeaderboard {
    private final Player playerStub;
    private final AuthResult user;

    public PlayerLeaderboard(Player playerStub, AuthResult user) {
        this.playerStub = playerStub;
        this.user = user;
    }

    public String[] getTopPlayers() {
        try {
            List<String> players = Arrays.asList(playerStub.getTopPlayers(10));
            return players.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"Error loading leaderboard."};
        }
    }
}