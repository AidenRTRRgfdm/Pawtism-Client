package dev.pawtism.client.discord;

/** Safe local feedback, with no socket paths or profile information. */
public record DiscordStatus(State state, String message) {
    public enum State {
        DISABLED, NEEDS_APPLICATION_ID, CONNECTING, WAITING_FOR_DISCORD, ACTIVE, REJECTED, STOPPED
    }
}
