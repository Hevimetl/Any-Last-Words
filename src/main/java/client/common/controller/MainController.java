package client.common.controller;

import client.admin.controller.*;
import client.player.controller.PlayerGameSelectionController;
import client.player.controller.PlayerMenuController;
import client.player.controller.PlayerStartController;
import client.player.controller.*;
import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.UserRole;
import PlayerService.Player;
import client.utility.SceneManager;
import javafx.event.ActionEvent;
import org.omg.CORBA.ORB;

public class MainController {
    public static AdminUserController adminUserController;
    public static AdminUserDetailsController adminUserDetailsController;
    public MainController(AuthResult user, Player playerStub, Admin adminStub, ORB orb){


        if (user.role == UserRole.PLAYER){
            PlayerMenuController playerMenuController = new PlayerMenuController(playerStub, user);
            PlayerStartController playerStartController = new PlayerStartController(playerStub, user, orb);
            PlayerGameSelectionController playerGameSelectionController = new PlayerGameSelectionController(playerStub, user, orb);
            PlayerCharactersController playerCharactersController = new PlayerCharactersController(playerStub, user);
            PlayerLeaderboardController playerLeaderboardController = new PlayerLeaderboardController(playerStub, user);

            //PlayerLobbyController playerLobbyController = new PlayerLobbyController(playerStub, user);
        } else if (user.role == UserRole.ADMIN) {
            AdminSettingsController adminSettingsController = new AdminSettingsController(adminStub, user);
            adminUserController = new AdminUserController(adminStub, user);
            AdminCreateUserController adminCreateUserController = new AdminCreateUserController(adminStub, user);
            adminUserDetailsController = new AdminUserDetailsController(adminStub, user);

            adminCreateUserController.getView().setUserButtonHandler(this::handleUserButton);
            adminCreateUserController.getView().setBackButtonHandler(this::handleBackButton);
            adminUserDetailsController.getView().setUserButtonHandler(this::handleUserButton);

        }


    }

    private void handleUserButton(ActionEvent event) {
        SceneManager.showScreen("AdminUser");
        adminUserController.initialize();
    }

    public static void updateViews(){
        adminUserController.initialize();
        adminUserDetailsController.initialize();
    }

    private void handleBackButton(ActionEvent event) {
        SceneManager.showScreen("AdminUserDetails");
        adminUserDetailsController.initialize();
    }







}
