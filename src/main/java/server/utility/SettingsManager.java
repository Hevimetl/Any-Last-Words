package server.utility;

import CommonTypes.Settings;

public class SettingsManager {
    private final Settings globalSettings;

    private static final SettingsManager INSTANCE = new SettingsManager();

    private SettingsManager() {
        // initialize defaults
        Settings s = new Settings();
        s.waitingTime = 10;  // seconds
        s.roundTime   = 30;  // seconds
        globalSettings = s;
    }

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    public synchronized Settings getSettings() {
        // return defensive copy
        Settings copy = new Settings();
        copy.waitingTime = globalSettings.waitingTime;
        copy.roundTime = globalSettings.roundTime;
        return copy;
    }

    public synchronized void updateSettings(Settings newSettings) {
        globalSettings.waitingTime = newSettings.waitingTime;
        globalSettings.roundTime = newSettings.roundTime;
    }

    public int getWaitingTime() {
        return globalSettings.waitingTime;
    }

    public int getRoundTime() {
        return globalSettings.roundTime;
    }
}