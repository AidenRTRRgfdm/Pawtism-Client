package dev.pawtism.client;

import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.List;
import java.util.Optional;

/** Original 9 × 3 inventory preview backed only by data already on the hovered item. */
public final class ShulkerTooltip {
    private static boolean initialized;
    private ShulkerTooltip() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        ClientTooltipComponentCallback.EVENT.register(component -> component instanceof Preview preview
                ? new Grid(preview.items()) : null);
    }

    public static Optional<TooltipComponent> imageFor(ItemStack stack) {
        if (!PawtismConfig.SHULKER_TOOLTIP.getBooleanValue() || stack.isEmpty()
                || !(stack.getItem() instanceof BlockItem block) || !(block.getBlock() instanceof ShulkerBoxBlock))
            return Optional.empty();
        TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (display.hideTooltip() || !display.shows(DataComponents.CONTAINER)) return Optional.empty();
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        contents.copyInto(items);
        return Optional.of(new Preview(List.copyOf(items)));
    }

    public record Preview(List<ItemStack> items) implements TooltipComponent {}

    private record Grid(List<ItemStack> items) implements ClientTooltipComponent {
        @Override public int getHeight(Font font) { return 60; }
        @Override public int getWidth(Font font) { return 166; }

        @Override public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
            graphics.fill(x, y, x + 164, y + 58, 0xff211b2a);
            graphics.outline(x, y, 164, 58, PawtismConfig.ACCENT.getIntegerValue());
            for (int slot = 0; slot < 27; slot++) {
                int sx = x + 2 + (slot % 9) * 18;
                int sy = y + 2 + (slot / 9) * 18;
                graphics.fill(sx, sy, sx + 17, sy + 17, 0xff42394e);
                graphics.horizontalLine(sx, sx + 16, sy, 0xff151019);
                graphics.verticalLine(sx, sy, sy + 16, 0xff151019);
                ItemStack item = items.get(slot);
                if (!item.isEmpty()) {
                    graphics.item(item, sx + 1, sy + 1, slot);
                    graphics.itemDecorations(font, item, sx + 1, sy + 1);
                }
            }
        }
    }
}
