package dev.pawtism.client.discord;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/** Local Windows pipes, using the native bridge already shipped with Minecraft. */
final class WindowsDiscordTransport implements DiscordTransport {
    interface Pipe extends AutoCloseable {
        int available() throws IOException;
        int read(byte[] buffer, int length) throws IOException;
        int write(byte[] buffer) throws IOException;
        @Override void close() throws IOException;
    }

    private final Pipe pipe;
    private final byte[] header = new byte[8];
    private int headerRead, payloadRead;
    private byte[] payload;
    private boolean closed;

    WindowsDiscordTransport(Pipe pipe) { this.pipe = pipe; }

    static DiscordTransport connect() throws IOException {
        Kernel32 kernel = Kernel32.INSTANCE;
        for (int index = 0; index < 10; index++) {
            WinNT.HANDLE handle = kernel.CreateFile("\\\\?\\pipe\\discord-ipc-" + index,
                WinNT.GENERIC_READ | WinNT.GENERIC_WRITE, 0, null, 3, 0, null);
            if (handle == null || WinBase.INVALID_HANDLE_VALUE.equals(handle)) continue;
            // Each native read/write returns immediately; one worker owns this handle.
            if (!kernel.SetNamedPipeHandleState(handle,
                    new IntByReference(WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_NOWAIT), null, null)) {
                kernel.CloseHandle(handle);
                continue;
            }
            return new WindowsDiscordTransport(new NativePipe(kernel, handle));
        }
        throw new IOException("Discord desktop IPC is unavailable");
    }

    @Override public void send(DiscordProtocol.Frame frame, Duration timeout) throws IOException {
        ensureOpen();
        byte[] bytes = DiscordProtocol.encode(frame);
        long start = System.nanoTime(), budget = nanos(timeout);
        int offset = 0;
        while (offset < bytes.length) {
            checkInterrupted();
            int written = pipe.write(offset == 0 ? bytes : Arrays.copyOfRange(bytes, offset, bytes.length));
            if (written < 0 || written > bytes.length - offset) throw new IOException("Invalid pipe write count");
            offset += written;
            if (offset == bytes.length) return;
            if (System.nanoTime() - start >= budget) throw new IOException("Discord IPC write timed out");
            pause();
        }
    }

    @Override public DiscordProtocol.Frame poll(Duration timeout) throws IOException {
        ensureOpen();
        long start = System.nanoTime(), budget = nanos(timeout);
        do {
            checkInterrupted();
            int available = pipe.available();
            if (available < 0) throw new IOException("Invalid pipe byte count");
            if (available > 0) {
                int needed = payload == null ? header.length - headerRead : payload.length - payloadRead;
                byte[] chunk = new byte[Math.min(available, needed)];
                int read = pipe.read(chunk, chunk.length);
                if (read < 0 || read > chunk.length) throw new IOException("Invalid pipe read count");
                if (payload == null) {
                    System.arraycopy(chunk, 0, header, headerRead, read);
                    headerRead += read;
                    if (headerRead == header.length) payload = new byte[DiscordProtocol.payloadLength(header)];
                } else {
                    System.arraycopy(chunk, 0, payload, payloadRead, read);
                    payloadRead += read;
                }
                if (payload != null && payloadRead == payload.length) {
                    DiscordProtocol.Frame frame = DiscordProtocol.fromHeader(header, payload);
                    headerRead = payloadRead = 0;
                    payload = null;
                    return frame;
                }
            }
            if (System.nanoTime() - start >= budget) return null;
            pause();
        } while (true);
    }

    @Override public void close() throws IOException {
        if (!closed) { closed = true; pipe.close(); }
    }

    private void ensureOpen() throws IOException {
        if (closed) throw new IOException("Discord IPC is closed");
    }
    private static long nanos(Duration timeout) {
        if (timeout.isNegative()) throw new IllegalArgumentException("Negative IPC timeout");
        try { return timeout.toNanos(); }
        catch (ArithmeticException tooLong) { return Long.MAX_VALUE; }
    }
    private static void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Discord IPC interrupted");
    }
    private static void pause() { LockSupport.parkNanos(1_000_000L); }

    private static final class NativePipe implements Pipe {
        private static final int ERROR_NO_DATA = 232;
        private final Kernel32 kernel;
        private final WinNT.HANDLE handle;
        private NativePipe(Kernel32 kernel, WinNT.HANDLE handle) { this.kernel = kernel; this.handle = handle; }
        @Override public int available() throws IOException {
            IntByReference total = new IntByReference();
            if (!kernel.PeekNamedPipe(handle, null, 0, null, total, null)) throw failure("peek");
            return total.getValue();
        }
        @Override public int read(byte[] bytes, int length) throws IOException {
            IntByReference count = new IntByReference();
            if (!kernel.ReadFile(handle, bytes, length, count, null)) {
                if (kernel.GetLastError() == ERROR_NO_DATA) return 0;
                throw failure("read");
            }
            return count.getValue();
        }
        @Override public int write(byte[] bytes) throws IOException {
            IntByReference count = new IntByReference();
            if (!kernel.WriteFile(handle, bytes, bytes.length, count, null)) {
                if (kernel.GetLastError() == ERROR_NO_DATA) return count.getValue();
                throw failure("write");
            }
            return count.getValue();
        }
        @Override public void close() throws IOException {
            if (!kernel.CloseHandle(handle)) throw failure("close");
        }
        private IOException failure(String operation) {
            return new IOException("Discord IPC " + operation + " failed (" + kernel.GetLastError() + ")");
        }
    }
}
