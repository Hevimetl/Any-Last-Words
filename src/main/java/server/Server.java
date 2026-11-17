package server;

import AdminService.Admin;
import AdminService.AdminHelper;
import CommonTypes.Login;
import CommonTypes.LoginHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import PlayerService.Player;
import PlayerService.PlayerHelper;
import server.servant.AdminImpl;
import server.servant.LoginImpl;
import server.servant.PlayerImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Server {
    private static Connection con;
    private static ORB orb;
    private static boolean isServerRunning = false;
    private static JLabel serverStatus;
    private static JLabel dbStatus;
    private static JButton startServerButton;
    private static JButton stopServerButton;
    private static JTextArea logArea;
    private static JTextField portField;
    private static JTextField orbHostField; // Field for ORB host
    private static String[] orbArgs; // Store command-line args for ORB initialization

    public static void main(String[] args) {
        orbArgs = args; // Store command-line arguments
        SwingUtilities.invokeLater(Server::createGUI);
    }

    private static void createGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame("Server Control Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 550);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout(10, 10));
            frame.getContentPane().setBackground(new Color(30, 30, 30));
            frame.setResizable(true);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(new Color(30, 30, 30));
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel dbPanel = new JPanel(new GridBagLayout());
            dbPanel.setBackground(new Color(40, 40, 40));
            dbPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100)),
                    "Database & ORB Configuration",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            JLabel usernameLabel = new JLabel("DB Username:");
            usernameLabel.setForeground(Color.WHITE);
            dbPanel.add(usernameLabel, gbc);
            gbc.gridx = 1;
            JTextField usernameField = new JTextField("root", 15);
            usernameField.setBackground(new Color(50, 50, 50));
            usernameField.setForeground(Color.WHITE);
            usernameField.setCaretColor(Color.WHITE);
            dbPanel.add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            JLabel passwordLabel = new JLabel("DB Password:");
            passwordLabel.setForeground(Color.WHITE);
            dbPanel.add(passwordLabel, gbc);
            gbc.gridx = 1;
            JPasswordField passwordField = new JPasswordField(15);
            passwordField.setBackground(new Color(50, 50, 50));
            passwordField.setForeground(Color.WHITE);
            passwordField.setCaretColor(Color.WHITE);
            dbPanel.add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            JLabel schemaLabel = new JLabel("DB Schema Name:");
            schemaLabel.setForeground(Color.WHITE);
            dbPanel.add(schemaLabel, gbc);
            gbc.gridx = 1;
            JTextField schemaField = new JTextField("hangman", 15);
            schemaField.setBackground(new Color(50, 50, 50));
            schemaField.setForeground(Color.WHITE);
            schemaField.setCaretColor(Color.WHITE);
            dbPanel.add(schemaField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            JLabel dbHostLabel = new JLabel("DB Host IP:");
            dbHostLabel.setForeground(Color.WHITE);
            dbPanel.add(dbHostLabel, gbc);
            gbc.gridx = 1;
            JTextField dbHostField = new JTextField("localhost", 15);
            dbHostField.setBackground(new Color(50, 50, 50));
            dbHostField.setForeground(Color.WHITE);
            dbHostField.setCaretColor(Color.WHITE);
            dbPanel.add(dbHostField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            JLabel orbHostLabel = new JLabel("ORB Host IP:");
            orbHostLabel.setForeground(Color.WHITE);
            dbPanel.add(orbHostLabel, gbc);
            gbc.gridx = 1;
            orbHostField = new JTextField("localhost", 15);
            orbHostField.setBackground(new Color(50, 50, 50));
            orbHostField.setForeground(Color.WHITE);
            orbHostField.setCaretColor(Color.WHITE);
            dbPanel.add(orbHostField, gbc);

            gbc.gridx = 0; gbc.gridy = 5;
            JLabel portLabel = new JLabel("ORB Port:");
            portLabel.setForeground(Color.WHITE);
            dbPanel.add(portLabel, gbc);
            gbc.gridx = 1;
            portField = new JTextField("900", 15); // Changed default to 900
            portField.setBackground(new Color(50, 50, 50));
            portField.setForeground(Color.WHITE);
            portField.setCaretColor(Color.WHITE);
            dbPanel.add(portField, gbc);

            JPanel controlPanel = new JPanel(new GridLayout(1, 3, 3, 1));
            controlPanel.setBackground(new Color(40, 40, 40));
            controlPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100)),
                    "Server Controls",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));
            controlPanel.setPreferredSize(new Dimension(0, 35));

            JButton connectBtn = new JButton("Connect to Database");
            connectBtn.setBackground(new Color(60, 120, 60));
            connectBtn.setForeground(Color.BLACK);
            connectBtn.setFocusPainted(false);
            connectBtn.setToolTipText("Connect to the MySQL database");
            connectBtn.setPreferredSize(new Dimension(70, 18));
            connectBtn.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
            connectBtn.setFont(new Font("Arial", Font.PLAIN, 9));
            connectBtn.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String schema = schemaField.getText();
                String dbHost = dbHostField.getText();

                new SwingWorker<Connection, Void>() {
                    @Override
                    protected Connection doInBackground() {
                        try {
                            String url = "jdbc:mysql://" + dbHost + ":3306/" + schema +
                                    "?user=" + username +
                                    (password.isEmpty() ? "" : "&password=" + password) +
                                    "&connectTimeout=5000";
                            logArea.append("Attempting to connect to database at: " + dbHost + "\n");
                            return DriverManager.getConnection(url);
                        } catch (SQLException ex) {
                            return null;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            con = get();
                            if (con != null) {
                                dbStatus.setText("Database: Connected");
                                dbStatus.setForeground(new Color(0, 255, 0));
                                logArea.append("Database connected successfully at " + dbHostField.getText() + ".\n");
                                startServerButton.setEnabled(true);
                            } else {
                                dbStatus.setText("Database: Disconnected - Connection failed");
                                dbStatus.setForeground(Color.RED);
                                logArea.append("Database connection failed at " + dbHostField.getText() + ".\n");
                            }
                        } catch (Exception ex) {
                            dbStatus.setText("Database: Disconnected - " + ex.getMessage());
                            dbStatus.setForeground(Color.RED);
                            logArea.append("Database connection error: " + ex.getMessage() + "\n");
                        }
                    }
                }.execute();
            });
            controlPanel.add(connectBtn);

            startServerButton = new JButton("Start Server");
            startServerButton.setBackground(new Color(60, 120, 60));
            startServerButton.setForeground(Color.BLACK);
            startServerButton.setFocusPainted(false);
            startServerButton.setEnabled(false);
            startServerButton.setToolTipText("Start the CORBA server");
            startServerButton.setPreferredSize(new Dimension(70, 18));
            startServerButton.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
            startServerButton.setFont(new Font("Arial", Font.PLAIN, 9));
            startServerButton.addActionListener(e -> {
                if (!isServerRunning) {
                    isServerRunning = true;
                    startServerButton.setEnabled(false);
                    stopServerButton.setEnabled(true);
                    serverStatus.setText("Server: Running");
                    serverStatus.setForeground(new Color(0, 255, 0));
                    logArea.append("Starting CORBA services...\n");
                    startCorbaServices();
                }
            });
            controlPanel.add(startServerButton);

            stopServerButton = new JButton("Stop Server");
            stopServerButton.setBackground(new Color(120, 60, 60));
            stopServerButton.setForeground(Color.BLACK);
            stopServerButton.setFocusPainted(false);
            stopServerButton.setEnabled(false);
            stopServerButton.setToolTipText("Stop the CORBA server");
            stopServerButton.setPreferredSize(new Dimension(70, 18));
            stopServerButton.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
            stopServerButton.setFont(new Font("Arial", Font.PLAIN, 9));
            stopServerButton.addActionListener(e -> {
                if (isServerRunning) {
                    isServerRunning = false;
                    startServerButton.setEnabled(true);
                    stopServerButton.setEnabled(false);
                    serverStatus.setText("Server: Stopped");
                    serverStatus.setForeground(Color.RED);
                    logArea.append("Stopping CORBA services...\n");
                    stopCorbaServices();
                }
            });
            controlPanel.add(stopServerButton);

            JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            statusPanel.setBackground(new Color(40, 40, 40));
            statusPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100)),
                    "Status",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));

            serverStatus = new JLabel("Server: Stopped");
            serverStatus.setForeground(Color.RED);
            serverStatus.setFont(new Font("Arial", Font.PLAIN, 12));
            dbStatus = new JLabel("Database: Disconnected");
            dbStatus.setForeground(Color.RED);
            dbStatus.setFont(new Font("Arial", Font.PLAIN, 12));
            statusPanel.add(serverStatus);
            statusPanel.add(dbStatus);

            JPanel logPanel = new JPanel(new BorderLayout());
            logPanel.setBackground(new Color(40, 40, 40));
            logPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100)),
                    "Server Log",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));

            logArea = new JTextArea(6, 50);
            logArea.setEditable(false);
            logArea.setBackground(new Color(50, 50, 50));
            logArea.setForeground(Color.WHITE);
            logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            logArea.setLineWrap(true);
            logArea.setWrapStyleWord(true);
            JScrollPane logScroll = new JScrollPane(logArea);
            logPanel.add(logScroll, BorderLayout.CENTER);

            mainPanel.add(dbPanel, BorderLayout.NORTH);
            mainPanel.add(controlPanel, BorderLayout.CENTER);
            mainPanel.add(logPanel, BorderLayout.SOUTH);
            frame.add(mainPanel, BorderLayout.CENTER);
            frame.add(statusPanel, BorderLayout.SOUTH);

            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startCorbaServices() {
        new Thread(() -> {
            try {
                // Validate and get ORB port from GUI
                String port = portField.getText();
                int portNum = 900; // Changed default to 900
                try {
                    portNum = Integer.parseInt(port);
                    if (portNum < 0 || portNum > 65535) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    logArea.append("Invalid ORB port number. Using default: 900\n"); // Updated log message
                    portNum = 900;
                }

                // Get ORB host (priority: command-line > GUI > default)
                String orbHost = "localhost";
                boolean cmdLineHost = false;
                for (int i = 0; i < orbArgs.length - 1; i++) {
                    if (orbArgs[i].equals("-ORBInitialHost")) {
                        orbHost = orbArgs[i + 1];
                        cmdLineHost = true;
                        break;
                    }
                }
                if (!cmdLineHost && !orbHostField.getText().isEmpty()) {
                    orbHost = orbHostField.getText();
                }

                // Prepare ORB arguments
                String[] initArgs = new String[4];
                initArgs[0] = "-ORBInitialPort";
                initArgs[1] = String.valueOf(portNum);
                initArgs[2] = "-ORBInitialHost";
                initArgs[3] = orbHost;

                // Log ORB configuration
                logArea.append("Initializing ORB with host: " + orbHost + ", port: " + portNum + "\n");

                // Initialize ORB
                Properties props = new Properties();
                props.put("org.omg.CORBA.ORBInitialHost", orbHost);
                props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(portNum));
                orb = ORB.init(initArgs, props);

                // Get reference to rootpoa & activate the POAManager
                POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
                rootpoa.the_POAManager().activate();

                // Register Login service
                LoginImpl loginImpl = new LoginImpl(con);
                org.omg.CORBA.Object loginRef = rootpoa.servant_to_reference(loginImpl);
                Login loginHref = LoginHelper.narrow(loginRef);

                org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

                NameComponent[] loginPath = ncRef.to_name("Login");
                ncRef.rebind(loginPath, loginHref);
                logArea.append("Login service registered: " + orb.object_to_string(loginHref) + "\n");

                // Register Player service
                PlayerImpl playerImpl = new PlayerImpl(con);
                org.omg.CORBA.Object playerRef = rootpoa.servant_to_reference(playerImpl);
                Player playerHref = PlayerHelper.narrow(playerRef);

                NameComponent[] playerPath = ncRef.to_name("Player");
                ncRef.rebind(playerPath, playerHref);
                logArea.append("Player service registered: " + orb.object_to_string(playerHref) + "\n");

                // Register Admin service
                AdminImpl adminImpl = new AdminImpl(con);
                org.omg.CORBA.Object adminRef = rootpoa.servant_to_reference(adminImpl);
                Admin adminHref = AdminHelper.narrow(adminRef);

                NameComponent[] adminPath = ncRef.to_name("Admin");
                ncRef.rebind(adminPath, adminHref);
                logArea.append("Admin service registered.\n");

                logArea.append("Server ready and waiting on " + orbHost + ":" + portNum + "...\n");
                orb.run();
            } catch (Exception e) {
                logArea.append("Error starting CORBA services: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }).start();
    }

    private static void stopCorbaServices() {
        if (orb != null) {
            try {
                orb.shutdown(true);
                orb.destroy();
                orb = null;
                logArea.append("CORBA services stopped.\n");
            } catch (Exception e) {
                logArea.append("Error stopping CORBA services: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        return con;
    }
}