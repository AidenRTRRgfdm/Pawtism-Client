package dev.pawtism.client.combat;

import java.util.ArrayDeque;

/** A rolling one-second count, rather than an average since the game started. */
public final class SlidingClickCounter {
    private static final long WINDOW = 1_000_000_000L;
    private final ArrayDeque<Long> clicks = new ArrayDeque<>();

    public void click(long now) { prune(now); clicks.addLast(now); }
    public int count(long now) { prune(now); return clicks.size(); }
    public void clear() { clicks.clear(); }
    private void prune(long now) {
        while (!clicks.isEmpty() && now - clicks.getFirst() >= WINDOW) clicks.removeFirst();
    }
}
