package dev.pawtism.client.discord;

import java.io.IOException;
import java.time.Duration;

/** All calls, including discovery and close, belong to a background worker. */
interface DiscordTransport extends AutoCloseable {
    void send(DiscordProtocol.Frame frame, Duration timeout) throws IOException;
    /** Returns null when no complete frame is available before this polling deadline. */
    DiscordProtocol.Frame poll(Duration timeout) throws IOException;
    @Override void close() throws IOException;

    @FunctionalInterface
    interface Connector { DiscordTransport connect() throws IOException; }
}
