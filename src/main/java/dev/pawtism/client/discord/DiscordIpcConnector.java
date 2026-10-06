package dev.pawtism.client.discord;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Discovers only Discord's local desktop IPC endpoints. */
final class DiscordIpcConnector implements DiscordTransport.Connector {
    private final List<Path> paths;
    private final boolean windows;

    DiscordIpcConnector(List<Path> paths) { this.paths = List.copyOf(paths); this.windows = false; }
    private DiscordIpcConnector(List<Path> paths, boolean windows) { this.paths = List.copyOf(paths); this.windows = windows; }

    static DiscordIpcConnector system() {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
        return new DiscordIpcConnector(windows ? List.of() : unixPaths(System.getenv()), windows);
    }

    static List<Path> unixPaths(Map<String, String> environment) {
        LinkedHashSet<Path> directories = new LinkedHashSet<>();
        for (String key : List.of("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")) {
            String value = environment.get(key);
            if (value != null && !value.isBlank()) {
                try { directories.add(Path.of(value).toAbsolutePath().normalize()); }
                catch (RuntimeException ignored) { }
            }
        }
        directories.add(Path.of("/tmp"));
        List<Path> direct = List.copyOf(directories);
        for (Path directory : direct) {
            directories.add(directory.resolve("app/com.discordapp.Discord"));
            directories.add(directory.resolve("app/com.discordapp.DiscordCanary"));
            directories.add(directory.resolve("app/com.discordapp.DiscordPTB"));
            directories.add(directory.resolve("snap.discord"));
        }
        List<Path> paths = new ArrayList<>();
        for (Path directory : directories) for (int index = 0; index < 10; index++) paths.add(directory.resolve("discord-ipc-" + index));
        return List.copyOf(paths);
    }

    @Override public DiscordTransport connect() throws IOException {
        if (windows) return WindowsDiscordTransport.connect();
        for (Path path : paths) {
            interrupted();
            if (!Files.exists(path)) continue;
            try { return UnixTransport.open(path, Duration.ofMillis(300)); }
            catch (InterruptedIOException interrupted) { throw interrupted; }
            catch (IOException unavailable) { }
        }
        throw new IOException("Discord desktop IPC is unavailable");
    }

    private static void interrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("IPC operation was cancelled");
    }

    static final class UnixTransport implements DiscordTransport {
        private final SocketChannel socket;
        private final Selector selector;
        private final SelectionKey key;
        private final ByteBuffer header = ByteBuffer.allocate(8);
        private ByteBuffer payload;
        private boolean partial;
        private long partialStarted;

        private UnixTransport(SocketChannel socket, Selector selector, SelectionKey key) {
            this.socket = socket; this.selector = selector; this.key = key;
        }

        static UnixTransport open(Path path, Duration timeout) throws IOException {
            SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX);
            Selector selector = null;
            try {
                socket.configureBlocking(false);
                boolean connected = socket.connect(UnixDomainSocketAddress.of(path));
                selector = Selector.open();
                SelectionKey key = socket.register(selector, connected ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT);
                long deadline = System.nanoTime() + timeout.toNanos();
                while (!connected) {
                    interrupted();
                    if (System.nanoTime() - deadline >= 0) throw new IOException("IPC connection timed out");
                    selector.select(25); selector.selectedKeys().clear(); connected = socket.finishConnect();
                }
                key.interestOps(SelectionKey.OP_READ);
                return new UnixTransport(socket, selector, key);
            } catch (IOException | RuntimeException failure) {
                try { socket.close(); } catch (IOException ignored) { }
                if (selector != null) try { selector.close(); } catch (IOException ignored) { }
                throw failure;
            }
        }

        @Override public void send(DiscordProtocol.Frame frame, Duration timeout) throws IOException {
            ByteBuffer bytes = ByteBuffer.wrap(DiscordProtocol.encode(frame));
            long deadline = System.nanoTime() + timeout.toNanos();
            key.interestOps(SelectionKey.OP_WRITE);
            try {
                while (bytes.hasRemaining()) {
                    interrupted();
                    if (System.nanoTime() - deadline >= 0) throw new IOException("IPC write timed out");
                    if (socket.write(bytes) == 0) await(deadline);
                }
            } finally {
                if (key.isValid()) key.interestOps(SelectionKey.OP_READ);
            }
        }

        @Override public DiscordProtocol.Frame poll(Duration timeout) throws IOException {
            long deadline = System.nanoTime() + timeout.toNanos();
            while (true) {
                interrupted();
                ByteBuffer bytes = payload == null ? header : payload;
                int count = socket.read(bytes);
                if (count < 0) throw new EOFException("Discord closed its IPC connection");
                if (count > 0 && !partial) { partial = true; partialStarted = System.nanoTime(); }
                if (payload == null && !header.hasRemaining()) payload = ByteBuffer.allocate(DiscordProtocol.payloadLength(header.array()));
                if (payload != null && !payload.hasRemaining()) {
                    DiscordProtocol.Frame frame = DiscordProtocol.fromHeader(header.array(), payload.array());
                    header.clear(); payload = null; partial = false;
                    return frame;
                }
                if (partial && System.nanoTime() - partialStarted >= Duration.ofSeconds(3).toNanos()) {
                    throw new IOException("Incomplete IPC frame timed out");
                }
                if (System.nanoTime() - deadline >= 0) return null;
                if (count == 0) {
                    await(deadline);
                }
            }
        }

        private void await(long deadline) throws IOException {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) return;
            selector.select(Math.max(1, Math.min(25, (remaining + 999_999L) / 1_000_000L)));
            selector.selectedKeys().clear();
        }

        @Override public void close() throws IOException {
            try { socket.close(); } finally { selector.close(); }
        }
    }
}
