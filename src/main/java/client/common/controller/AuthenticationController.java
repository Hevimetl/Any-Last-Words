package client.common.controller;

import client.Client;
import client.common.model.Authentication;
import client.common.view.AuthenticationView;
import CommonTypes.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class AuthenticationController  {
    private Authentication model;
    private AuthenticationView view;    
    private Client client;


    public AuthenticationController(Login loginService, Client client){
        model = new Authentication(loginService);
        view = new AuthenticationView();
        this.client = client;
        attachListeners();
    }



    public void attachListeners(){
        view.setLoginHandler(this::handleLogin);
    }

    private void handleLogin(ActionEvent evt) {
        try {
            AuthResult authResult = model.authenticate(
                    view.getUsername(),
                    view.getPassword()
            );
            client.handleLogin(authResult);
            view.clearFields();

        } catch (InvalidCredentialsException e) {
            view.showError("Invalid Credentials",
                    "The username or password you entered is incorrect.");
        } catch (UserDoesNotExistException e) {
            view.showError("User Not Found",
                    "No user exists with the name: " + view.getUsername());
        } catch (UserAlreadyLoggedInException e) {
            Platform.runLater(() ->
                    client.showForceLogoutDialog(view.getUsername(), view.getPassword())
            );
        }
    }
    public AuthenticationView getView() {
        return view;
    }

}
