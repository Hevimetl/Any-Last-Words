package server.utility.objects;

import CommonTypes.PlayerGameState;
import CommonTypes.Settings;
import GameNotifications.GameCallback;
import server.servant.PlayerImpl;
import server.utility.GameSessionManager;
import server.utility.SessionManager;
import server.utility.SettingsManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;


public class GameSession {
    private String gameID;
    private String creatorToken;
    GameState gameState;

    private final Map<String, PlayerGameState> playerStates = new ConcurrentHashMap<>();
    private final List<String> usedWords = new CopyOnWriteArrayList<>();
    private int currentRound = 1;
    private String currentWord;
    private volatile boolean roundEnded = false;
    private volatile int currentRoundTime;
    private volatile int currentWaitingTime;
    private String roundWinnerName = null;
    private Map<String, GameCallback> playerCallbacks = new ConcurrentHashMap<>();

    private Settings gameSettings;

    //Word Randomization Attributes
    private static final List<String> ALL_WORDS;
    static {
        try {
            ALL_WORDS = Collections.unmodifiableList(
                    Files.readAllLines(Paths.get("src/main/resources/words/words.txt"))
            );
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Could not load words.txt: " + e);
        }
    }

    private final Random rng = new Random();

    public void restartWaitingTimer() {
        // Reset the current waiting time to the initial waiting time
        currentWaitingTime = SettingsManager.getInstance().getWaitingTime();

        // If a countdown task is already running, cancel it
        if (countdownTask != null) {
            countdownTask.cancel(false);
        }

        // Start a new countdown task
        countdownTask = timerExecutor.scheduleAtFixedRate(() -> {
            currentWaitingTime--;
            // Only check conditions when time expires
            if (currentWaitingTime <= 0) {
                long readyCount = playerStates.values().stream()
                        .filter(p -> p.isReady)
                        .count();

                if (readyCount >= 2) {
                    System.out.println("Ready!");
                    countdownTask.cancel(false);
                    startRoundCountdown();
                    gameState = GameState.STARTED;
                } else {
                    System.out.println("not ready");
                    startTimer();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }



    //Enum for Game State
    public enum GameState {
        WAITING("Waiting"),
        STARTED("Started"),
        FINISHED("Finished");


        private final String displayName;

        GameState(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    //Attributes for Countdown
    private final ScheduledExecutorService timerExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> countdownTask;
    private ScheduledFuture<?> roundCountdownTask;
    public GameSession(String gameID, String creatorToken) {
        this.gameID = gameID;
        this.creatorToken = creatorToken;
        this.gameState = GameState.WAITING;
        this.currentWord = pickNextWord();
        this.currentRound = 1;
        this.currentRoundTime = SettingsManager.getInstance().getRoundTime();
        this.currentWaitingTime = SettingsManager.getInstance().getWaitingTime();
        startTimer();
        createDefaultPlayerGameState(creatorToken);

    }

    private String pickNextWord() {
        // Build a list of candidates
        List<String> candidates = new ArrayList<>(ALL_WORDS);
        candidates.removeAll(usedWords);

        // Pick and record
        String word = candidates.get(rng.nextInt(candidates.size()));
        usedWords.add(word);
        System.out.println(word);
        return word;
    }


    private void startTimer (){
        if (countdownTask != null) {
            countdownTask.cancel(false);
        }

        currentWaitingTime = SettingsManager.getInstance().getWaitingTime();

        countdownTask = timerExecutor.scheduleAtFixedRate(() -> {
            currentWaitingTime--;
            // Only check conditions when time expires
            if (currentWaitingTime <= 0) {
                long readyCount = playerStates.values().stream()
                        .filter(p -> p.isReady)
                        .count();

                if (readyCount >= 2) {
                    System.out.println("Ready!");
                    countdownTask.cancel(false);
                    startRoundCountdown();
                    gameState = GameState.STARTED;
                } else {
                    System.out.println("not rady");
                    startTimer();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void startRoundCountdown() {
        if (roundCountdownTask != null) {
            roundCountdownTask.cancel(false);
        }

        currentRoundTime = SettingsManager.getInstance().getRoundTime();

        roundCountdownTask = timerExecutor.scheduleAtFixedRate(() -> {
            currentRoundTime--;
            if (currentRoundTime <= 0) {
                roundCountdownTask.cancel(false);
                checkRoundCompletion();
                roundWinnerName =null;
                endRound();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public PlayerGameState createDefaultPlayerGameState(String token) {
        String username = SessionManager.getInstance().getUsername(token);
        if (username == null) return null;

        if (playerStates.containsKey(token)) {
            return playerStates.get(token);
        }

        PlayerGameState state = new PlayerGameState();
        state.userName = username;
        state.remainingAttempts = 5;
        state.guessedLetters = new ArrayList<>().toArray(new String[0]);
        state.roundWins = 0;
        state.isReady = false;
        try {
            state.selectedCharacter = PlayerImpl.getSelectedCharacter(SessionManager.getInstance().getUsername(token));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        state.userName = SessionManager.getInstance().getUsername(token);

        playerStates.put(token, state);
        return state;
    }

    public void checkRoundCompletion() {
        // Check if someone has guessed the word
        boolean allDead = playerStates.values().stream()
                .allMatch(state1 -> state1.remainingAttempts <= 0);
        if (allDead) {
            roundEnded     = true;
            roundWinnerName = null;
            endRound();
            return;
        }

        for (Map.Entry<String, PlayerGameState> entry : playerStates.entrySet()) {
            String token = entry.getKey();
            PlayerGameState state = entry.getValue();

            // Check if player has guessed all letters
            if (hasGuessedWord(state)) {
                System.out.println("test");

                roundEnded = true;
                state.roundWins++;
                checkGameCompletion();
                System.out.println(state.userName + " " + state.roundWins);
                getCurrentRound();
                roundWinnerName = token;
                endRound();
                return;
            }



            // Check if time expired
            if (!roundEnded && currentWaitingTime <= 0) {
                roundEnded = true;
            }
        }
        }

        public void checkGameCompletion(){
            for (Map.Entry<String, PlayerGameState> entry : playerStates.entrySet()) {
                String token = entry.getKey();
                PlayerGameState state = entry.getValue();
                if (state.roundWins == 3) {
                    System.out.println("Win");
                    endGame(SessionManager.getInstance().getUsername(token));
                }
            }
        }

    private boolean hasGuessedWord(PlayerGameState state) {
        String word = currentWord.toLowerCase();
        for (char c : word.toCharArray()) {
            if (!Arrays.asList(state.guessedLetters).contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    private void endRound() {
        if (gameState == GameState.FINISHED) {
            return;
        }
        String winnerName = roundWinnerName != null ?
                playerStates.get(roundWinnerName).userName : "No winner";

        notifyPlayers("roundEnd", winnerName);

        timerExecutor.schedule(() -> {
            if (gameState != GameState.FINISHED) {
                startNewRound();
            }
        }, 4, TimeUnit.SECONDS);
    }

    private void endGame(String gameWinner){
        notifyPlayers("gameEnd", gameWinner);
        setGameState(GameState.FINISHED);
        stopAllTimers();
        try {
            GameSessionManager.getInstance().removeGame(getGameID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void startNewRound() {
        currentRound++;
        currentWord = pickNextWord();
        currentRoundTime = SettingsManager.getInstance().getRoundTime();

        // Reset player states
        playerStates.values().forEach(state -> {
            state.remainingAttempts = 5;
            state.guessedLetters = new String[0];
        });

        startRoundCountdown();
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String getGameID() {
        return gameID;
    }

    public String getCreatorToken() {
        return creatorToken;
    }

    public Map<String, PlayerGameState> getPlayerStates() {
        return playerStates;
    }

    public int getCurrentRoundTime() {
        return currentRoundTime;
    }

    public int getCurrentWaitingTime() {
        return currentWaitingTime;
    }




    public void registerCallback(String playerToken, GameCallback callback) {
        System.out.println("Added to Callback");
        playerCallbacks.put(playerToken, callback);
    }

    private void notifyPlayers(String method, String winnerName) {
        System.out.println("Trying to Notify");
        playerCallbacks.forEach((token, callback) -> {
            try {
                switch (method) {
                    case "roundEnd" :
                        System.out.println("trying to notify round end");
                        callback.onRoundEnd(getGameID(), winnerName);
                        break;
                    case "gameEnd" :
                        System.out.println("trying to notify game end");
                        callback.onGameEnd(getGameID(), winnerName);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Failed to notify player " + token + ": " + e.getMessage());
                playerCallbacks.remove(token);
            }
        });
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public String getRoundWinnerName() {
        return roundWinnerName;
    }

    private void stopAllTimers() {
        if (countdownTask != null) {
            countdownTask.cancel(false);
            countdownTask = null;
        }
        if (roundCountdownTask != null) {
            roundCountdownTask.cancel(false);
            roundCountdownTask = null;
        }
    }
}