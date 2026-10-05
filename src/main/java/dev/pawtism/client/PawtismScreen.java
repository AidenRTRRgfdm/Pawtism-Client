package dev.pawtism.client;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;

/** MaLiLib supplies the editable toggles, sliders, colors and reset buttons. */
public final class PawtismScreen extends GuiConfigsBase {
    public PawtismScreen(Screen parent) {
        this(parent, PawtismConfig.OPTIONS);
    }
    private final List<fi.dy.masa.malilib.config.IConfigBase> options;
    public PawtismScreen(Screen parent, List<fi.dy.masa.malilib.config.IConfigBase> options) {
        super(10, 42, "pawtism", parent, "Pawtism Client — Settings");
        this.options = options;
        setConfigWidth(180);
    }
    @Override public List<ConfigOptionWrapper> getConfigs() {
        return ConfigOptionWrapper.createFor(options);
    }
    @Override public void removed() {
        super.removed();
        PawtismConfig.INSTANCE.save();
    }
}
