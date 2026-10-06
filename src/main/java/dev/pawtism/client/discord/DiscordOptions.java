package dev.pawtism.client.discord;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigString;
import java.util.List;

/** Optional local Rich Presence settings. No login credentials are stored. */
public final class DiscordOptions {
    public static final ConfigBoolean RICH_PRESENCE = new ConfigBoolean("discordRichPresence", false,
        "Show Pawtism Client and a generic Main menu, Singleplayer or Multiplayer state in your running Discord desktop app. Requires your own Discord application ID.");
    public static final ConfigString APPLICATION_ID = new ConfigString("discordApplicationId", "",
        "The numeric Application ID of your own Discord developer application. This is a public ID, not a token. Leave empty to keep Rich Presence disconnected.");
    public static final List<IConfigBase> OPTIONS = List.of(RICH_PRESENCE, APPLICATION_ID);

    static {
        RICH_PRESENCE.setPrettyName("Discord Rich Presence");
        RICH_PRESENCE.setTranslatedName("Discord Rich Presence");
        APPLICATION_ID.setPrettyName("Discord application ID");
        APPLICATION_ID.setTranslatedName("Discord application ID");
    }

    private DiscordOptions() {}
}
