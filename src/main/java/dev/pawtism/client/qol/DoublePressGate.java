package dev.pawtism.client.qol;

/** A monotonic-time confirmation window; consumes authorization after one confirmed action. */
public final class DoublePressGate {
    private boolean armed;
    private long armedAt;

    public boolean confirm(boolean sameAction, long nowNanos, long windowNanos) {
        long elapsed = nowNanos - armedAt;
        if (armed && sameAction && elapsed >= 0 && elapsed <= Math.max(0, windowNanos)) {
            clear();
            return true;
        }
        armed = true;
        armedAt = nowNanos;
        return false;
    }

    public void clear() { armed = false; }
}
