package dev.pawtism.client.ui;

/** Responsive desktop positions, expressed in Minecraft's scaled GUI pixels. */
public record XpDesktopLayout(Rect window, Rect launch, Rect multiplayer, Rect modules, Rect start,
                              int contentX, int contentWidth, int primaryY, int modulesY,
                              int optionsY, int toolsY, int creditsY, int taskbarY) {
    public static XpDesktopLayout forScreen(int width, int height) {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Screen dimensions must be positive");
        int taskbarY = Math.max(0, height - 24);
        int windowWidth = Math.max(1, Math.min(340, width - 96));
        int windowHeight = Math.max(1, Math.min(188, taskbarY - 24));
        int windowX = Math.max(0, Math.min((width + 76 - windowWidth) / 2, width - windowWidth - 6));
        int windowY = Math.max(4, (taskbarY - 18 - windowHeight) / 2);
        Rect window = new Rect(windowX, windowY, windowWidth, windowHeight);
        return new XpDesktopLayout(window,
            new Rect(6, 9, 68, 48), new Rect(6, 66, 68, 48), new Rect(6, 123, 68, 48),
            new Rect(0, taskbarY, Math.min(66, width), Math.min(24, height)),
            windowX + 12, Math.max(1, windowWidth - 24), windowY + 44,
            windowY + 110, windowY + 134, windowY + 158,
            Math.max(0, taskbarY - 12), taskbarY);
    }

    public Rect primary(int index) {
        return new Rect(contentX, primaryY + index * 22, contentWidth, 20);
    }

    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
    }
}
