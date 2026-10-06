package dev.pawtism.client.discord;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import static dev.pawtism.client.discord.DiscordStatus.State.*;

/** A single daemon actor owns IPC. Game ticks replace one desired snapshot, never queue I/O. */
final class DiscordRpcService implements AutoCloseable {
    static final long MIN_UPDATE_NANOS = Duration.ofSeconds(15).toNanos();
    private static final long HANDSHAKE_NANOS = Duration.ofSeconds(3).toNanos();
    private static final long ACK_NANOS = Duration.ofSeconds(10).toNanos();
    private static final Duration POLL = Duration.ofMillis(100), WRITE = Duration.ofMillis(500);
    private final DiscordTransport.Connector connector;
    private final LongSupplier clock;
    private final long pid, startSeconds;
    private final Object signal = new Object();
    private volatile PresenceData desired = PresenceData.disabled();
    private volatile DiscordStatus status = new DiscordStatus(DISABLED, "Discord Rich Presence is off.");
    private volatile boolean stopping;
    private volatile Thread worker;

    // Everything below belongs only to the actor thread.
    private DiscordTransport transport;
    private String sessionId = "", attemptedId = "", pendingNonce;
    private PresenceData.ScreenState lastSentState;
    private boolean ready, activityWritten, hasSentActivity;
    private long handshakeStarted, commandStarted, lastActivity, retryAt;
    private int failures;

    DiscordRpcService(DiscordTransport.Connector connector, LongSupplier clock, long pid, long startSeconds) {
        this.connector = Objects.requireNonNull(connector); this.clock = Objects.requireNonNull(clock);
        this.pid = pid; this.startSeconds = startSeconds;
    }

    void submit(PresenceData next) {
        Objects.requireNonNull(next);
        synchronized (signal) {
            if (stopping || next.equals(desired)) return;
            desired = next;
            if (worker == null) {
                if (next.usable()) {
                    worker = new Thread(this::run, "Pawtism-Discord-IPC");
                    worker.setDaemon(true); worker.start();
                } else status = inactive(next);
            }
            signal.notifyAll();
        }
    }

    DiscordStatus status() { return status; }
    boolean workerAlive() { Thread thread = worker; return thread != null && thread.isAlive(); }

    /** Test clock advancement can wake the actor without changing the desired state. */
    void wake() { synchronized (signal) { signal.notifyAll(); } }

    @Override public void close() {
        synchronized (signal) {
            if (stopping) return;
            stopping = true; desired = PresenceData.disabled();
            if (worker == null) status = new DiscordStatus(STOPPED, "Discord Rich Presence stopped.");
            signal.notifyAll();
        }
    }

    /** Used by isolated tests only; the Minecraft facade never waits for its worker. */
    boolean awaitStopped(Duration timeout) throws InterruptedException {
        Thread thread = worker;
        if (thread != null) thread.join(Math.max(1, timeout.toMillis()));
        return !workerAlive();
    }

    private void run() {
        try {
            while (!stopping) {
                try {
                    step();
                } catch (IOException | RuntimeException unavailable) {
                    failed(false, 0);
                }
            }
        } finally {
            Thread.interrupted(); disconnect(true);
            status = new DiscordStatus(STOPPED, "Discord Rich Presence stopped.");
        }
    }

    private void step() throws IOException {
        Thread.interrupted();
        PresenceData request = desired;
        if (!request.usable()) {
            disconnect(true); status = inactive(request); failures = 0; retryAt = 0;
            idle(); return;
        }
        if (!attemptedId.equals(request.applicationId())) {
            disconnect(true); failures = 0; retryAt = 0; attemptedId = request.applicationId();
        }
        if (transport == null) {
            if (retryAt != 0 && clock.getAsLong() - retryAt < 0) { idle(); return; }
            connect(request);
            if (transport == null) return;
        }
        if (!matchesSession()) return;
        long now = clock.getAsLong();
        if (ready && pendingNonce == null && (lastSentState != desired.state() || !activityWritten)
            && (!hasSentActivity || now - lastActivity >= MIN_UPDATE_NANOS)) sendActivity();
        if (!ready && now - handshakeStarted >= HANDSHAKE_NANOS) throw new IOException("Discord handshake timed out");
        if (pendingNonce != null && now - commandStarted >= ACK_NANOS) throw new IOException("Discord activity acknowledgement timed out");
        DiscordProtocol.Frame frame = transport.poll(POLL);
        if (frame != null) receive(frame);
    }

    private void connect(PresenceData request) {
        status = new DiscordStatus(CONNECTING, "Connecting to your local Discord desktop app.");
        try {
            transport = connector.connect();
            sessionId = request.applicationId(); ready = false; activityWritten = false; lastSentState = null;
            if (!matchesSession()) return;
            transport.send(DiscordProtocol.handshake(sessionId), WRITE);
            handshakeStarted = clock.getAsLong();
        } catch (IOException | RuntimeException unavailable) {
            failed(false, 0);
        }
    }

    private void sendActivity() throws IOException {
        PresenceData request = desired;
        if (!matchesSession()) return;
        String nonce = UUID.randomUUID().toString();
        transport.send(DiscordProtocol.activity(pid, startSeconds, request.state(), nonce), WRITE);
        lastActivity = clock.getAsLong(); hasSentActivity = true; activityWritten = true; lastSentState = request.state();
        pendingNonce = nonce; commandStarted = clock.getAsLong();
    }

    private void receive(DiscordProtocol.Frame frame) throws IOException {
        if (!matchesSession()) return;
        if (frame.opcode() == DiscordProtocol.PING) {
            transport.send(new DiscordProtocol.Frame(DiscordProtocol.PONG, frame.payload()), WRITE); return;
        }
        if (frame.opcode() == DiscordProtocol.PONG) return;
        if (frame.opcode() == DiscordProtocol.CLOSE) throw new IOException("Discord closed its IPC session");
        if (frame.opcode() != DiscordProtocol.FRAME) throw new IOException("Unexpected Discord IPC opcode");
        JsonObject message = DiscordProtocol.json(frame);
        if (DiscordProtocol.error(message)) {
            if (!ready || pendingNonce != null && pendingNonce.equals(DiscordProtocol.string(message, "nonce"))) {
                failed(true, DiscordProtocol.errorCode(message));
            }
        } else if (!ready && DiscordProtocol.ready(message)) {
            ready = true; failures = 0;
        } else if (ready && DiscordProtocol.acknowledged(message, pendingNonce)) {
            pendingNonce = null;
            if (matchesSession()) status = new DiscordStatus(ACTIVE, "Discord accepted your Pawtism activity.");
        }
    }

    private boolean matchesSession() {
        PresenceData latest = desired;
        return !stopping && latest.usable() && sessionId.equals(latest.applicationId());
    }

    private void failed(boolean rejected, int code) {
        boolean changed = stopping || !desired.usable() || !attemptedId.equals(desired.applicationId());
        Thread.interrupted(); disconnect(changed);
        if (!desired.usable() || stopping) return;
        if (changed) { failures = 0; retryAt = 0; return; }
        failures = Math.min(6, failures + 1);
        long seconds = rejected ? 60 : Math.min(60, 1L << (failures - 1));
        retryAt = clock.getAsLong() + Duration.ofSeconds(seconds).toNanos();
        status = rejected
            ? new DiscordStatus(REJECTED, "Discord rejected the activity" + (code == 0 ? "." : " (code " + code + ").") + " Check your application ID.")
            : new DiscordStatus(WAITING_FOR_DISCORD, "Discord desktop is unavailable. Retrying in the background.");
    }

    private void disconnect(boolean clear) {
        DiscordTransport previous = transport;
        transport = null; pendingNonce = null; ready = false; sessionId = ""; lastSentState = null;
        if (previous == null) return;
        Thread.interrupted();
        try {
            if (clear && activityWritten) previous.send(DiscordProtocol.clear(pid, UUID.randomUUID().toString()), WRITE);
        } catch (IOException | RuntimeException ignored) {
            // Closing the IPC session also removes its activity.
        } finally {
            activityWritten = false;
            try { previous.close(); } catch (IOException | RuntimeException ignored) { }
        }
    }

    private void idle() {
        synchronized (signal) {
            if (stopping) return;
            try { signal.wait(100); } catch (InterruptedException changed) { }
        }
    }

    private static DiscordStatus inactive(PresenceData data) {
        return data.enabled() ? new DiscordStatus(NEEDS_APPLICATION_ID, "Invalid application ID. Clear it to use Pawtism's ID.")
            : new DiscordStatus(DISABLED, "Discord Rich Presence is off.");
    }
}
