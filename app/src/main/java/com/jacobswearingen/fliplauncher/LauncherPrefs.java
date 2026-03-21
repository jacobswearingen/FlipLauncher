package com.jacobswearingen.fliplauncher;

/**
 * Central place for all SharedPreferences keys and sentinel values
 * used across the launcher.
 */
final class LauncherPrefs {

    // SharedPreferences file name
    static final String PREFS = "launcher_prefs";

    // Keys
    static final String KEY_HOTKEY_DPAD_UP    = "hotkey_dpad_up";
    static final String KEY_HOTKEY_DPAD_RIGHT = "hotkey_dpad_right";
    static final String KEY_HOTKEY_DPAD_LEFT  = "hotkey_dpad_left";

    // Sentinel: hotkey slot has no app assigned
    static final String DEST_NONE = "none";

    private LauncherPrefs() {}
}
