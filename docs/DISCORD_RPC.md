# Discord Rich Presence

Pawtism can display its name, Minecraft version, session timer and current activity in the Discord desktop app.

## Turn it on

1. Start the Discord desktop app.
2. Press **Right Shift**, choose **UI**, then open **Module settings**.
3. Set **Discord application ID** to the public numeric ID of a Discord developer application.
4. Enable **Discord Rich Presence**.

The feature is off by default. The status line below the settings shows whether it is disabled, connecting, waiting for Discord, accepted or rejected.

## Activity

- **Main menu** when no world is loaded.
- **Singleplayer** in a local world.
- **Multiplayer** when connected to a server.
- A timer measured from the current Minecraft session's start.

The application name shown by Discord comes from the application associated with the ID. Activity details show **Pawtism Client** and **Minecraft 26.2**.

## Application ID

An application ID is a public number. It is not a password, account token, bot token or client secret.

To use a separate application, create it in the [Discord Developer Portal](https://discord.com/developers/applications), give it your preferred display name and copy its **Application ID** from General Information. Creating a bot and authorizing a bot to a server are unnecessary for this feature.

Pawtism stores the ID with its other settings in `config/pawtism/client.json`.

## Updates and connections

- Presence uses Discord's local IPC connection on a background worker.
- It reconnects when the desktop app becomes available.
- State updates are coalesced and sent at least 15 seconds apart.
- Disabling the module clears the activity and closes the connection.
- Closing Minecraft requests the same cleanup.
- A rejected application ID is retried more slowly. Correct the ID or disable the module while troubleshooting.

Server addresses, world names, coordinates, player names and chat are excluded from the activity.

## Troubleshooting

- **Enter a numeric application ID:** copy the Application ID, then check for missing digits or extra text.
- **Waiting for Discord:** open the desktop app. The website and mobile app cannot provide the local IPC connection.
- **Rejected:** verify the application ID and check your Discord activity settings.
- **Accepted but not visible to others:** check Discord's activity sharing preferences and other running activities.

Minecraft continues running when Discord is closed or unavailable. Windows uses named pipes; macOS and Linux use local sockets.

## Validation

Protocol framing, activity content, acknowledgements, reconnection and cleanup have automated tests. The Windows transport is checked with a fake pipe on macOS; a native Windows run has not been performed.
