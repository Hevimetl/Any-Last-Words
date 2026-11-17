package client.admin.controller;

import CommonTypes.AdminPrivilegeRequiredException;
import CommonTypes.UserAlreadyExistsException;
import client.admin.model.AdminCreateUser;
import client.admin.view.AdminCreateUserView;
import client.utility.SceneManager;
import AdminService.Admin;
import CommonTypes.UserRole;
import CommonTypes.AuthResult;
import javafx.event.ActionEvent;

public class AdminCreateUserController {
    private final AdminCreateUser model;
    private final AdminCreateUserView view;

    public AdminCreateUserController(Admin adminStub, AuthResult user) {
        model = new AdminCreateUser(adminStub, user);
        view = new AdminCreateUserView();
        initialize();
    }

    private void initialize() {
        view.setSettingsButtonHandler(this::handleSettingsButton);
        view.setManageButtonHandler(this::handleManageButton);
        view.setCreateUserHandler(this::handleCreateUser);
    }

    // Navigation handlers

    private void handleSettingsButton(ActionEvent event) {
        SceneManager.showScreen("AdminSettings");
    }

    private void handleManageButton(ActionEvent event) {
        SceneManager.showScreen("AdminUserDetails");
    }



    // User creation handler
    private void handleCreateUser() {
        String username = view.getUsername();
        String password = view.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            view.showAlert("Error", "All fields are required");
            return;
        }

        try {
            model.createUser(username, password, UserRole.PLAYER);
            view.showAlert("Success", "User created successfully");
            view.clearFields();
        } catch (AdminPrivilegeRequiredException e) {
            view.showAlert("Error", "Admin privileges required");
        } catch (UserAlreadyExistsException e) {
            view.showAlert("Error", "Username already exists");
        } catch (Exception e) {
            view.showAlert("Error", "Creation failed: " + e.getMessage());
        }
    }

    public AdminCreateUserView getView() {
        return view;
    }
}