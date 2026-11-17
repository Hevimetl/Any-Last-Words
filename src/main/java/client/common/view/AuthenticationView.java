package client.common.view;

import client.utility.SceneManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthenticationView {
    @FXML private TextField userName;
    @FXML private TextField password;
    @FXML private Button loginButton;
    @FXML private ImageView loginImage;

    private final Parent root;

    public AuthenticationView() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/yoster.ttf"), 14);
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/Authentication.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("auth", root);



            //
            loginButton.setOnMouseEntered (e -> loginImage.setImage(new Image("/images/SignIn_Hover.png")));
            loginButton.setOnMouseExited (e -> loginImage.setImage(new Image ("/images/SignIn_Base.png")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setLoginHandler(EventHandler<ActionEvent> handler) {
        loginButton.setOnAction(handler);
    }

    public String getUsername() {
        return userName.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void clearFields(){
        password.setText("");
        userName.setText("");
    }





}
