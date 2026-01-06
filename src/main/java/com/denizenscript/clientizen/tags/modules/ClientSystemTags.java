package com.denizenscript.clientizen.tags.modules;

import com.denizenscript.clientizen.objects.ModTag;
import com.denizenscript.clientizen.tags.ClientTagBase;
import com.denizenscript.clientizen.util.FpsMonitor;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.TagManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class ClientSystemTags {

    public static void register(TagManager.TagProcessor<ClientTagBase> tagProcessor) {

        // <--[tag]
        // @attribute <client.mods>
        // @returns ListTag(ModTag)
        // @description
        // Returns a list of all currently loaded Fabric mods (this doesn't include things like mods-within-mods or built-in mods).
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "mods", (attribute, object) -> {
            return new ListTag(FabricLoader.getInstance().getAllMods(),
                    modContainer -> modContainer.getContainingMod().isEmpty() && modContainer.getMetadata().getType().equals("fabric")
                            && !modContainer.getMetadata().getId().equals("fabricloader"),
                    ModTag::new);
        });

        // <--[tag]
        // @attribute <client.all_mods>
        // @returns ListTag(ModTag)
        // @description
        // Returns a list of all currently loaded Fabric mods, including mods-within-mods and built-in mods.
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "all_mods", (attribute, object) -> {
            return new ListTag(FabricLoader.getInstance().getAllMods(), ModTag::new);
        });

        // <--[tag]
        // @attribute <client.is_focused>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the Minecraft window is currently focused (active), or false if minimized/background.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_focused", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().isWindowActive());
        });

        // <--[tags]
        // @attribute <client.fps> & <client.min_fps> & <client.max_fps> & <client.average_fps>
        // @returns ElementTag(Boolean)
        // @description
        // Returns the FPS (Frames Per Second) of the client.
        // -->

        tagProcessor.registerTag(ElementTag.class, "fps", (attribute, object) -> {
            return new ElementTag(FpsMonitor.getCurrentGameFps());
        });

        tagProcessor.registerTag(ElementTag.class, "min_fps", (attribute, object) -> {
            int fps = FpsMonitor.minFps == Integer.MAX_VALUE ? FpsMonitor.getCurrentGameFps() : FpsMonitor.minFps;
            return new ElementTag(fps);
        });

        tagProcessor.registerTag(ElementTag.class, "max_fps", (attribute, object) -> {
            return new ElementTag(FpsMonitor.maxFps);
        });

        tagProcessor.registerTag(ElementTag.class, "average_fps", (attribute, object) -> {
            return new ElementTag(FpsMonitor.getAverage());
        });
    }
}
