package dev.pawtism.client.discord;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigString;
import java.util.List;

/** Optional local Rich Presence settings. No login credentials are stored. */
public final class DiscordOptions {
    public static final ConfigBoolean RICH_PRESENCE = new ConfigBoolean("discordRichPresence", false,
        "Show Pawtism Client and a generic Main menu, Singleplayer or Multiplayer state in your running Discord desktop app. Uses Pawtism's application unless you enter another public application ID.");
    public static final ConfigString APPLICATION_ID = new ConfigString("discordApplicationId", "1556850603345576057",
        "The public numeric Application ID used for Discord Rich Presence. Leave blank to use Pawtism's application, or enter another application's ID. This is not a token. Disable Discord Rich Presence to disconnect.");
    public static final List<IConfigBase> OPTIONS = List.of(RICH_PRESENCE, APPLICATION_ID);

    static {
        RICH_PRESENCE.setPrettyName("Discord Rich Presence");
        RICH_PRESENCE.setTranslatedName("Discord Rich Presence");
        APPLICATION_ID.setPrettyName("Discord application ID");
        APPLICATION_ID.setTranslatedName("Discord application ID");
    }

    private DiscordOptions() {}
}
