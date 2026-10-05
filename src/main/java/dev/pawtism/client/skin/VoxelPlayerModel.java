package dev.pawtism.client.skin;

import net.minecraft.resources.Identifier;

/** Keeps hand rendering and normal avatar rendering on the same mesh selection. */
public interface VoxelPlayerModel {
    void pawtism$useSkin(Identifier texture, boolean enabled);
}
