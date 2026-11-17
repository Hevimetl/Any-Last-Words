package client.utility;

import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static Stage primaryStage;
    private static Map<String, Parent> screens = new HashMap<>();

    private static MediaPlayer bgMusicPlayer;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void addScreen(String name, Parent screen) {
        screens.put(name, screen);
    }

    public static void showScreen(String name) {
        if (screens.containsKey(name)) {
            primaryStage.getScene().setRoot(screens.get(name));
            if (name.equals("PlayerMenu")){
                playMenuBackgroundMusic();
            } else if (name.equals("PlayerGame")){
                if (bgMusicPlayer != null) {
                    bgMusicPlayer.stop();
                    bgMusicPlayer = null;

                }
                playGameBackgroundMusic();

            }
        } else {
            System.err.println("Screen not found: " + name);
        }
    }

    public static void showScreenWithDelay(String name, double seconds) {
        if (screens.containsKey(name)) {
            PauseTransition delay = new PauseTransition(Duration.seconds(seconds));
            delay.setOnFinished(event -> primaryStage.getScene().setRoot(screens.get(name)));
            delay.play();
        } else {
            System.err.println("Screen not found: " + name);
        }
    }

    public static void switchToScreen(String screenName) {
        if (screens.containsKey(screenName)) {
            primaryStage.getScene().setRoot(screens.get(screenName));
        } else {
            System.err.println("Screen not found: " + screenName);
        }
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public static void playMenuBackgroundMusic() {

        if (bgMusicPlayer != null && bgMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }


        if (bgMusicPlayer != null) {
            bgMusicPlayer.stop();
            bgMusicPlayer.dispose();
        }

        URL musicUrl = SceneManager.class.getResource("/sounds/menu_music.mp3");
        if (musicUrl == null) {
            System.err.println("Menu music not found!");
            return;
        }


        Media media = new Media(musicUrl.toExternalForm());
        bgMusicPlayer = new MediaPlayer(media);
        bgMusicPlayer.setVolume(0.3);
        bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgMusicPlayer.play();
    }

    public static void playGameBackgroundMusic() {

        if (bgMusicPlayer != null && bgMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }


        if (bgMusicPlayer != null) {
            bgMusicPlayer.stop();
            bgMusicPlayer.dispose();
        }

        URL musicUrl = SceneManager.class.getResource("/sounds/game_music.mp3");
        if (musicUrl == null) {
            System.err.println("Menu music not found!");
            return;
        }


        Media media = new Media(musicUrl.toExternalForm());
        bgMusicPlayer = new MediaPlayer(media);
        bgMusicPlayer.setVolume(0.3);
        bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgMusicPlayer.play();
    }

    public static void speedUpMusic(){
        bgMusicPlayer.setRate(2.5);
    }

    public static void resetMusic(){
        bgMusicPlayer.setRate(1.0);
    }
}