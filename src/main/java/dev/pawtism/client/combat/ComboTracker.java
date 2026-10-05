package dev.pawtism.client.combat;

import java.util.LinkedHashMap;
import java.util.Map;

/** Attempts cannot increment a combo. Only an attributed damage confirmation can. */
public final class ComboTracker {
    private static final long CONFIRM_WINDOW = 1_500_000_000L;
    private static final long COMBO_WINDOW = 3_000_000_000L;
    private final Map<Integer, Attempt> attempts = new LinkedHashMap<>();
    private int combo;
    private int targetId;
    private long lastConfirmed;
    private boolean hasConfirmed;
    private double hitDistance = Double.NaN;

    public void attempted(int target, double distance, long now) {
        if (!Double.isFinite(distance) || distance < 0) return;
        attempts.put(target, new Attempt(distance, now));
        while (attempts.size() > 32) attempts.remove(attempts.keySet().iterator().next());
    }

    public boolean confirmed(int target, long now) {
        Attempt attempt = attempts.remove(target);
        if (attempt == null || now - attempt.time() < 0 || now - attempt.time() > CONFIRM_WINDOW) return false;
        if (!hasConfirmed || target != targetId || now - lastConfirmed >= COMBO_WINDOW || combo == 0) combo = 0;
        combo++;
        targetId = target;
        lastConfirmed = now;
        hasConfirmed = true;
        hitDistance = attempt.distance();
        return true;
    }

    public int combo(long now) {
        if (hasConfirmed && now - lastConfirmed >= COMBO_WINDOW) combo = 0;
        return combo;
    }
    public double lastHitDistance() { return hitDistance; }
    public void receivedDamage() { combo = 0; attempts.clear(); }
    public void clear() {
        combo = 0; hasConfirmed = false; hitDistance = Double.NaN; attempts.clear();
    }
    private record Attempt(double distance, long time) {}
}
