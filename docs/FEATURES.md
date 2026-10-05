# Feature reference

Pawtism Client **1.1.0** for Minecraft **26.2** has **49 feature toggles** across four categories and **22 movable HUD panels**. Open the browser with **Right Shift** to search, enable or disable modules.

## Core — 11 features

- **Zoom:** hold Z for optical zoom. Magnification is adjustable from 2× to 16×, with 4× as the default.
- **Toggle sprint:** use Minecraft's native toggle sprint control.
- **Status HUD:** show Pawtism, FPS, ping, server address and sprint state.
- **Armor HUD:** show equipped armor and remaining durability.
- **Custom crosshair:** adjust its color, arm length and center gap.
- **Waypoints:** save named locations per world or server; create, edit, hide or delete them with N.
- **3D skin layers:** add depth to the outer skin layer, including first-person sleeves.
- **Flat dropped items:** make ground items lie flat.
- **Saturation display:** show saturation and the hunger/saturation bonus of held food.
- **Shulker box preview:** show the item's stored contents in a tooltip grid.
- **Performance preset:** apply a reversible set of lighter video settings.

## HUD — 16 features

- **Coordinates:** position and dimension.
- **Direction / compass:** facing direction and compass heading.
- **Biome:** the biome at your position.
- **Movement speed:** horizontal travel speed in blocks per second; large teleports are excluded.
- **Potion timers:** active effects, strength and remaining time.
- **Held item / durability:** item name, stack count or remaining durability, plus the offhand item.
- **Inventory preview:** the 27 inventory slots above the hotbar.
- **Local clock:** your computer's time.
- **Session timer:** elapsed time in the current connection.
- **Memory usage:** Minecraft's Java memory usage and limit.
- **Online players:** the number of entries in the server's player list.
- **Light level:** block and sky light at your position.
- **Game day:** the overworld day counter and time.
- **Resource packs:** active resource pack names.
- **Arrow / totem counter:** arrows and totems in your inventory and offhand.
- **FPS / frame graph:** FPS and timing over the last 90 rendered frames.

## PvP — 10 features

- **Left / right CPS:** physical mouse presses during gameplay, counted over the last second.
- **Keystrokes:** movement keys, mouse buttons and space indicators.
- **Confirmed combo counter:** consecutive server-confirmed melee hits on the same target.
- **Confirmed hit distance:** an approximate eye-to-hit distance for the last confirmed melee attempt.
- **Attack cooldown HUD:** your vanilla attack charge.
- **Lower fire overlay:** move the first-person fire overlay down.
- **Stable hurt camera:** suppress the camera tilt after taking damage.
- **Compact held items:** adjust the cosmetic size of first-person items.
- **Crosshair center dot:** add a small dot to the custom crosshair.
- **Crosshair target color:** change its color over a visible living entity.

### Combo and distance behavior

- A combo advances when a matching local melee attempt receives a server damage event attributed to you.
- Taking damage, changing targets or going three seconds without a hit resets it.
- Distance is marked as approximate. Latency affects the measurement.
- These displays leave attack range, damage and cooldowns at their normal values.

## Quality of life — 12 features

- **Death location:** report your death coordinates locally in chat.
- **Death waypoints:** save an automatic marker at your death location.
- **Held item drop guard:** require a second drop-key press for protected items.
- **Reconnect button:** start a manual, cancellable reconnect countdown after disconnection.
- **Chat timestamps:** add local time to new incoming messages.
- **Toggle sneak:** use Minecraft's native toggle sneak control.
- **Cosmetic sky clock:** choose the visible sun, moon and star time.
- **Local clear weather:** hide local rain, snow and related ambient effects.
- **Hide scoreboard:** hide the sidebar.
- **Hide boss bars:** hide boss-bar overlays.
- **Hide screen titles:** hide large titles and subtitles.
- **Hide toast cards:** hide toast graphics.

### Drop guard

- Tools and armor, enchanted items and valuable items are protected by default.
- Press the drop key again within two seconds to confirm one matching drop.
- Changing items resets confirmation.
- Each protected item category and the confirmation window are configurable.
- Dropping items through inventory screens keeps its usual behavior.

### Death markers and reconnect

- Automatic death markers are stored per world or server.
- The default limit is three automatic death markers; manually saved waypoints are separate.
- Reconnect starts only after clicking its button and makes one connection attempt.

## HUD layout and settings

The **22 movable panels** are the status panel, armor panel, 16 HUD-category panels, CPS, keystrokes, combo/hit distance and attack charge. Combo and hit distance share one panel.

- Enable a panel in the module browser before arranging it.
- Use **HUD Editor** to drag panels or nudge the selected panel with arrow keys.
- Use **Save & Done** to keep the layout, or **Reset layout** to start over.
- Open **Module settings** for detailed values, colors and reset controls.

The default layout keeps most extra information panels and visual overrides disabled. Settings also cover zoom magnification, crosshair size/gap/color, fire offset, held-item scale, sky time, reconnect delay and the death-marker limit.

## Display limits

### Food and containers

- Singleplayer saturation comes from the integrated server.
- Vanilla multiplayer does not synchronize exact saturation, so the multiplayer display is labeled as an estimate.
- Shulker previews use the contents already stored on the item.

### Skins and visual effects

- 3D layers support standard **64×64** skins.
- Ordinary skin rendering is used beyond 16 blocks or while a texture is unavailable.
- Flat item rendering changes appearance while keeping normal collision, movement and pickup.
- Cosmetic sky time leaves world clocks and terrain lighting unchanged.
- Local weather controls affect presentation; server weather and gameplay continue normally.
- Hidden toast graphics may still play their normal sounds.

### Performance preset

- View distance is capped at 12 and simulation distance at 8; smaller existing values are preserved.
- Particles are reduced, and clouds and entity shadows are disabled.
- Previous settings are restored when the preset is disabled or the client exits normally.
- Performance gains depend on the computer, world and current settings.

## Saved files

- Settings and HUD layout: `config/pawtism/client.json`
- Per-world and per-server waypoints: `config/pawtism/waypoints/`

[Back to installation and controls](../README.md)
