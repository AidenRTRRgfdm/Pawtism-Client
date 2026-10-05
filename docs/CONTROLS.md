# Controls

Pawtism's shortcuts appear under **Pawtism Client** in Minecraft's Controls settings. You can rebind each shortcut there.

| Default key | Action |
| --- | --- |
| Right Shift | Open the module browser. |
| Hold Z | Zoom while playing. Release Z to return to the normal view. |
| N | Open the waypoint manager. |
| B | Show or hide waypoint markers. |

Close other menus before using these shortcuts. Zoom and waypoint shortcuts require a loaded world. Zoom must also be enabled in Pawtism's settings; its magnification is adjustable under **Module settings**.

## Modules and settings

Open Pawtism with **Right Shift**, or use its configuration button in **Mod Menu**.

- Choose **All**, **Core**, **HUD**, **PvP**, **QoL**, or **UI** to filter the module cards.
- Type in **Search modules** to find a module.
- Click a card's **ON/OFF** button to toggle it.
- Use **<** and **>** to change pages.
- Open **Module settings** to adjust the selected category's toggles, values, colors, and reset controls.
- Choose **Done** to save and close the browser.

## Desktop and theme

The **XP interface** is enabled by default in version 1.2.0.

- **Launch Minecraft** opens Minecraft's singleplayer menu.
- **Multiplayer** opens Minecraft's multiplayer menu.
- **Modules** and the taskbar's **start** button open Pawtism's module browser.
- **Classic menu** disables the theme and opens the ordinary title screen.

To change the theme while playing, press **Right Shift**, choose **UI**, and toggle **XP interface**. To turn it back on from the ordinary title screen, use **Mods → Pawtism Client → settings → UI**.

The styled controls use Minecraft's normal mouse, keyboard, focus and text-editing behavior. Back, Done, Escape and inventory interactions retain their usual actions.

## Sprint and sneak

**Toggle sprint** uses Minecraft's assigned Sprint key. Press that key to toggle sprinting when movement permits it. The status HUD shows the current sprint state.

The optional **Toggle sneak** module uses Minecraft's assigned Sneak key. Disabling it restores the sneak control mode that was active before enabling the module.

## HUD editor

Choose **HUD Editor** in the module browser while in a world.

- Left-click an enabled HUD panel to select it, then drag it into position.
- Use the arrow keys to move the selected panel one pixel at a time.
- Choose **Reset layout** to restore the default positions.
- Choose **Save & Done** to save the layout and return to the browser.

Enable a HUD module in the browser before arranging its panel. Panel positions also save when you release a drag or close the editor.

## Waypoints

Press **N**, or choose **Waypoints** in the module browser.

1. Enter a name and X, Y, and Z coordinates. **Use current position** fills in your current coordinates.
2. Enter a six-digit color such as `F5A9CA` in the **Color** field.
3. Choose **Save waypoint**.

Each saved waypoint has **Show/Hide**, **Edit**, and **Delete** buttons. After editing its fields, choose **Save changes**. Use **<** and **>** to page through the list and **Done** to close it.

Waypoints are saved separately for each world or server. Markers show direction and distance for visible waypoints in your current dimension. **B** controls the overall marker display. Automatic death waypoints can be enabled and their retained count adjusted in the QoL settings.

## Drop protection

With **Held item drop guard** enabled, dropping a protected held item requires two deliberate presses of Minecraft's assigned Drop key:

1. Press Drop once to see the confirmation message.
2. Release the key, then press it again within the displayed confirmation period.

Holding the key down does not confirm the drop. Whole-stack drops require the same whole-stack shortcut on both presses. Protection categories and the confirmation period are adjustable in **Module settings**. Dropping items through an inventory screen uses Minecraft's usual behavior.

## Reconnect

With **Reconnect button** enabled, choose **Reconnect** on the disconnected screen to start the configured countdown. Choose **Cancel reconnect**, or press Escape, to cancel. Each button click starts one connection attempt.

## Automatic displays

Hover over a shulker box in an inventory to see its contents grid when **Shulker box preview** is enabled. Other HUDs, cosmetic options, chat timestamps, and overlay visibility are controlled through the module browser and **Module settings**.
