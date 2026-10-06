package dev.pawtism.client.discord;

import java.math.BigInteger;

/** Only these generic states can enter the IPC payload. */
record PresenceData(boolean enabled, String applicationId, ScreenState state) {
    enum ScreenState {
        MAIN_MENU("Main menu"), SINGLEPLAYER("Singleplayer"), MULTIPLAYER("Multiplayer");
        private final String label;
        ScreenState(String label) { this.label = label; }
        String label() { return label; }
    }

    PresenceData {
        applicationId = applicationId == null ? "" : applicationId.strip();
        state = state == null ? ScreenState.MAIN_MENU : state;
    }

    static PresenceData disabled() { return new PresenceData(false, "", ScreenState.MAIN_MENU); }

    /** Saved blank IDs use the current application default; nonblank overrides keep their meaning. */
    static String resolveApplicationId(String configured, String fallback) {
        String override = configured == null ? "" : configured.strip();
        return override.isEmpty() ? (fallback == null ? "" : fallback.strip()) : override;
    }

    static boolean validApplicationId(String value) {
        if (value == null || !value.matches("[1-9][0-9]{16,19}")) return false;
        return new BigInteger(value).bitLength() <= 64;
    }

    boolean usable() { return enabled && validApplicationId(applicationId); }
    boolean sameApplication(PresenceData other) {
        return enabled == other.enabled && applicationId.equals(other.applicationId);
    }
}
