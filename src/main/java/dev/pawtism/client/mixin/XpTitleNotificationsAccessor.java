package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTitleScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TitleScreen.class)
public interface XpTitleNotificationsAccessor extends XpTitleScreen.NativeNotificationsAccess {
    @Override @Accessor("realmsNotificationsScreen")
    RealmsNotificationsScreen pawtism$realmsNotifications();
}
