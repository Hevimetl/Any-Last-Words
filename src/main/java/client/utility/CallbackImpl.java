package client.utility;

import CommonTypes.AuthResult;
import client.Client;
import client.player.controller.PlayerGameController;
import GameNotifications.GameCallback;
import GameNotifications.GameCallbackHelper;
import GameNotifications.GameCallbackPOA;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class CallbackImpl extends GameCallbackPOA {
    private final PlayerGameController controller;
    private final ORB orb;
    private AuthResult user;

    public CallbackImpl(PlayerGameController controller, ORB orb, AuthResult user) {
        this.controller = controller;
        this.orb = orb;
        this.user = user;
    }

    @Override
    public void onRoundEnd(String gameId, String winnerName) {
        Platform.runLater(() -> {
                controller.handleRoundEnd(winnerName);

        });
    }

    @Override
    public void onGameEnd(String gameId, String winnerName) {
        Platform.runLater(() -> {
            controller.handleGameEnd(winnerName);

        });
    }

    @Override
    public void notifyReLogin(String username) {
        Platform.runLater(() -> {
            Client.getInstance().handleForceLogout(user.userName);
        });
    }

    public GameCallback getCorbaReference() {
        try {
            // Resolve the POA you activated at startup
            POA poa = CorbaHelper.getRootPoa();
            org.omg.CORBA.Object ref = poa.servant_to_reference(this);
            // Narrow it to GameCallback
            return GameCallbackHelper.narrow(ref);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get CORBA reference", e);
        }
    }

    public AuthResult getUser() {
        return user;
    }
}