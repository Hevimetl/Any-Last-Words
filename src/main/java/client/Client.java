package client;

import CommonTypes.*;
import GameNotifications.GameCallback;
import client.common.controller.AuthenticationController;
import client.common.controller.MainController;
import client.player.controller.PlayerGameController;
import client.player.controller.PlayerLobbyController;
import client.utility.CallbackImpl;
import client.utility.CorbaHelper;
import client.utility.SceneManager;
import AdminService.Admin;
import AdminService.AdminHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import PlayerService.Player;
import PlayerService.PlayerHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Properties;

import static client.utility.SceneManager.showError;

public class Client extends Application {

    public static Login loginImpl;
    static Player playerStub;
    static Admin adminStub;
    static ORB orb;
    static AuthResult authResult;
    PlayerGameController playerGameController;

    private static Client instance;
    private static String[] orbArgs; // Store command-line arguments
    private static TextField orbHostField; // Field for ORB host IP
    private static TextField orbPortField; // Field for ORB port
    private static TextArea logArea; // Log area for initialization status
    private static boolean isConfigured = false; // Flag for configuration completion

    public static Client getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        orbArgs = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        showConfigPane(primaryStage);
    }

    private void showConfigPane(Stage primaryStage) {
        Stage configStage = new Stage();
        configStage.setTitle("ORB Configuration");
        configStage.setResizable(true); // Allow resizing to test ScrollPane

        // Main content VBox
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2e2e2e;");

        // Title
        Label titleLabel = new Label("Configure ORB Settings");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        titleLabel.setTooltip(new Tooltip("Enter ORB host and port to connect to the server"));

        // Input fields in GridPane
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);

        Label orbHostLabel = new Label("ORB Host IP:");
        orbHostLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        orbHostField = new TextField("localhost"); // Matches server default
        orbHostField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-font-size: 14px;");
        orbHostField.setPromptText("e.g., localhost");
        orbHostField.setTooltip(new Tooltip("Enter the server host IP (default: localhost)"));
        orbHostField.setPrefWidth(200);

        Label orbPortLabel = new Label("ORB Port:");
        orbPortLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        orbPortField = new TextField("900"); // Matches server default
        orbPortField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-font-size: 14px;");
        orbPortField.setPromptText("e.g., 900");
        orbPortField.setTooltip(new Tooltip("Enter the server port (default: 900)"));
        orbPortField.setPrefWidth(200);

        grid.add(orbHostLabel, 0, 0);
        grid.add(orbHostField, 1, 0);
        grid.add(orbPortLabel, 0, 1);
        grid.add(orbPortField, 1, 1);

        // Log area with ScrollPane
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setPrefHeight(150);
        logArea.setStyle("-fx-control-inner-background: #3c3c3c; -fx-text-fill: white; -fx-font-size: 13px;");
        logArea.setText("Enter ORB settings (default: localhost:900) and click Confirm.\n");
        logArea.setWrapText(true);

        ScrollPane logScrollPane = new ScrollPane(logArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setFitToHeight(true);
        logScrollPane.setPrefHeight(160);
        logScrollPane.setStyle("-fx-background: #3c3c3c; -fx-border-color: #555555;");
        logScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScrollPane.setTooltip(new Tooltip("Initialization status and error messages"));

        // Confirm button
        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #3c763d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-cursor: hand;");
        confirmButton.setTooltip(new Tooltip("Confirm ORB settings and connect"));
        confirmButton.setOnAction(e -> {
            logArea.appendText("Validating inputs...\n");
            if (validateInputs()) {
                initializeORB();
                if (orb != null) {
                    initializeServices();
                    if (loginImpl != null && playerStub != null && adminStub != null) {
                        SceneManager.setPrimaryStage(primaryStage);
                        try {
                            logArea.appendText("Loading authentication screen...\n");
                            AuthenticationController authenticationController = new AuthenticationController(loginImpl, this);
                            primaryStage.setScene(new Scene(authenticationController.getView().getRoot(), 1280, 910));
                            primaryStage.setTitle("Hangman");
                            primaryStage.setResizable(false);
                            primaryStage.show();
                            primaryStage.setOnCloseRequest(event -> {
                                // prevent default close until we clean up
                                event.consume();

                                // unregister any callback
                                if (loginImpl != null && authResult != null) {
                                    try {
                                        loginImpl.unregisterCallback(authResult.userName);
                                    } catch (Exception ex) {
                                        System.err.println("Failed to unregister callback: " + ex.getMessage());
                                    }
                                }

                                // now actually exit
                                Platform.exit();
                                System.exit(0);
                            });
                            configStage.close();
                            isConfigured = true;
                            logArea.appendText("Authentication screen loaded.\n");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            logArea.appendText("Failed to load authentication screen: " + ex.getMessage() + "\n");
                            showError("Initialization Error", "Failed to load authentication screen: " + ex.getMessage());
                        }
                    } else {
                        logArea.appendText("Service initialization failed. Check server status.\n");
                        showError("Service Error", "Failed to initialize services. Ensure the server is running.");
                    }
                } else {
                    logArea.appendText("ORB initialization failed. Check host/port settings and ensure orbd is running.\n");
                    showError("ORB Error", "Failed to initialize ORB. Ensure orbd and server are running on specified host/port.");
                }
            }
        });

        // Assemble UI
        root.getChildren().addAll(titleLabel, grid, logScrollPane, confirmButton);

        // Wrap root in ScrollPane for entire pane scrolling
        ScrollPane mainScrollPane = new ScrollPane(root);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setStyle("-fx-background: #2e2e2e; -fx-border-color: #2e2e2e;");
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(mainScrollPane, 450, 400);
        configStage.setScene(scene);
        configStage.show();
    }

    private boolean validateInputs() {
        String host = orbHostField.getText().trim();
        String port = orbPortField.getText().trim();


        if (host.isEmpty()) {
            logArea.appendText("Error: ORB Host IP cannot be empty.\n");
            return false;
        }

        try {
            int portNum = Integer.parseInt(port);
            if (portNum < 0 || portNum > 65535) {
                logArea.appendText("Error: ORB Port must be between 0 and 65535.\n");
                return false;
            }
        } catch (NumberFormatException e) {
            logArea.appendText("Error: ORB Port must be a valid number.\n");
            return false;
        }

        logArea.appendText("Inputs validated successfully.\n");
        return true;
    }

    private void initializeORB() {
        try {
            String orbHost = "localhost"; // Default to match server
            boolean cmdLineHost = false;
            for (int i = 0; i < orbArgs.length - 1; i++) {
                if (orbArgs[i].equals("-ORBInitialHost") && i + 1 < orbArgs.length) {
                    orbHost = orbArgs[i + 1];
                    cmdLineHost = true;
                }
            }
            if (!cmdLineHost && !orbHostField.getText().isEmpty()) {
                orbHost = orbHostField.getText();
            }

            String orbPort = "900"; // Default to match server
            boolean cmdLinePort = false;
            for (int i = 0; i < orbArgs.length - 1; i++) {
                if (orbArgs[i].equals("-ORBInitialPort") && i + 1 < orbArgs.length) {
                    orbPort = orbArgs[i + 1];
                    cmdLinePort = true;
                }
            }
            if (!cmdLinePort && !orbPortField.getText().isEmpty()) {
                orbPort = orbPortField.getText();
            }
            logArea.appendText("Initializing ORB with host: " + orbHost + ", port: " + orbPort + "\n");

            int portNum;
            try {
                portNum = Integer.parseInt(orbPort);
                if (portNum < 0 || portNum > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                portNum = 900;
                logArea.appendText("Invalid ORB port number. Using default: 900\n");
            }

            String[] initArgs = new String[] {
                    "-ORBInitialPort", String.valueOf(portNum),
                    "-ORBInitialHost", orbHost
            };

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialHost", orbHost);
            props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(portNum));
            orb = ORB.init(initArgs, props);

            logArea.appendText("ORB initialized successfully.\n");
        } catch (Exception e) {
            e.printStackTrace();
            logArea.appendText("ORB initialization failed: " + e.getMessage() + "\n");
            orb = null;
        }
    }

    private void initializeServices() {
        try {
            logArea.appendText("Initializing services...\n");
            POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPoa.the_POAManager().activate();
            CorbaHelper.init(orb, rootPoa);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            loginImpl = LoginHelper.narrow(ncRef.resolve_str("Login"));
            if (loginImpl == null) {
                throw new Exception("Failed to resolve Login service");
            }
            logArea.appendText("Login service resolved.\n");

            playerStub = PlayerHelper.narrow(ncRef.resolve_str("Player"));
            if (playerStub == null) {
                throw new Exception("Failed to resolve Player service");
            }
            logArea.appendText("Player service resolved.\n");

            adminStub = AdminHelper.narrow(ncRef.resolve_str("Admin"));
            if (adminStub == null) {
                throw new Exception("Failed to resolve Admin service");
            }
            logArea.appendText("Admin service resolved.\n");
        } catch (Exception e) {
            e.printStackTrace();
            logArea.appendText("Service initialization failed: " + e.getMessage() + "\n");
            loginImpl = null;
            playerStub = null;
            adminStub = null;
        }
    }

    public void handleLogin(AuthResult authResult) {
        try {
            this.authResult = authResult;
            MainController mainController = new MainController(authResult, playerStub, adminStub, orb);
            CallbackImpl callbackImpl = new CallbackImpl(playerGameController, orb, authResult);
            GameCallback callback = callbackImpl.getCorbaReference();
            loginImpl.registerCallback(authResult.userName, callback);
            String currentGameStatus = loginImpl.isLoggedIn(authResult.token);
            GameInfo gameInfo = null;
            if (!currentGameStatus.equals("Not Joined")) {
                gameInfo = playerStub.getGameInfo(authResult.token, currentGameStatus);
            }
            if ((authResult.role == UserRole.ADMIN || authResult.role == UserRole.PLAYER) && "Not Joined".equals(currentGameStatus)) {
                mainController = new MainController(authResult, playerStub, adminStub, orb);
                if (authResult.role == UserRole.PLAYER) {
                    SceneManager.showScreen("PlayerMenu");
                } else {
                    SceneManager.showScreen("AdminUser");
                }
            } else if (gameInfo != null && gameInfo.gameState) {
                PlayerGameController gameController = new PlayerGameController(playerStub, authResult, playerStub.getGameInfo(authResult.token, currentGameStatus), orb);
                SceneManager.showScreen("PlayerGame");
            } else {
                PlayerLobbyController playerLobbyController = new PlayerLobbyController(playerStub, authResult, currentGameStatus, orb, true);
                SceneManager.showScreen("PlayerLobby");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Login Error", "Failed to handle login session: " + e.getMessage());
        }
    }

    public void handleForceLogout(String username) {
        try {
            if (authResult != null) {
                loginImpl.unregisterCallback(username);
                SceneManager.showScreen("auth");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showForceLogoutDialog(String username, String password) {
        Platform.runLater(() -> {
            try {
                loginImpl.forceLogout(username);
                AuthResult result = loginImpl.authenticate(username, password);
                handleLogin(result);
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Force Logout Failed", ex.getMessage());
            }
        });
    }
}