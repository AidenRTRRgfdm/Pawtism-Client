package dev.pawtism.client;

import dev.pawtism.client.combat.CombatOptions;
import dev.pawtism.client.hud.HudOptions;
import dev.pawtism.client.qol.QolOptions;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ModuleCatalog {
    public record Module(String category, ConfigBoolean option) {}
    public static final List<String> CATEGORIES = List.of("All", "Core", "HUD", "PvP", "QoL");
    private ModuleCatalog() {}
    public static List<IConfigBase> options(String category) {
        return switch (category) {
            case "Core" -> PawtismConfig.CORE_OPTIONS;
            case "HUD" -> HudOptions.OPTIONS;
            case "PvP" -> CombatOptions.OPTIONS;
            case "QoL" -> QolOptions.OPTIONS;
            default -> PawtismConfig.OPTIONS;
        };
    }
    public static List<Module> modules(String category, String query) {
        List<Module> result = new ArrayList<>();
        String needle = query.strip().toLowerCase(Locale.ROOT);
        for (String group : CATEGORIES.subList(1, CATEGORIES.size())) {
            if (!category.equals("All") && !category.equals(group)) continue;
            for (IConfigBase option : options(group)) if (option instanceof ConfigBoolean toggle
                && toggle != QolOptions.PROTECT_DURABLE && toggle != QolOptions.PROTECT_ENCHANTED && toggle != QolOptions.PROTECT_VALUABLE
                && (toggle.getConfigGuiDisplayName().toLowerCase(Locale.ROOT).contains(needle)
                    || toggle.getComment().toLowerCase(Locale.ROOT).contains(needle))) result.add(new Module(group, toggle));
        }
        result.sort(Comparator.comparing(module -> module.option().getConfigGuiDisplayName()));
        return result;
    }
}
