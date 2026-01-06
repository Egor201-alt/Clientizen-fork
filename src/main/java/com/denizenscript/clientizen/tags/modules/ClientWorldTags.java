package com.denizenscript.clientizen.tags.modules;

import com.denizenscript.clientizen.objects.LocationTag;
import com.denizenscript.clientizen.objects.MaterialTag;
import com.denizenscript.clientizen.tags.ClientTagBase;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;

public class ClientWorldTags {

    public static double climbingSpeed = 0.2; // 0.2 is the vanilla default

    public static void register(TagManager.TagProcessor<ClientTagBase> tagProcessor) {
        
        // <--[tag]
        // @attribute <client.cursor_on>
        // @returns LocationTag
        // @description
        // Returns the location of the block the client is currently looking at, if any.
        // -->
        tagProcessor.registerTag(LocationTag.class, "cursor_on", (attribute, object) -> {
            return Minecraft.getInstance().hitResult instanceof BlockHitResult blockHit ? new LocationTag(blockHit.getBlockPos()) : null;
        });

        // <--[tag]
        // @attribute <client.cursor_on_precise>
        // @returns LocationTag
        // @description
        // Returns the precise location the client is currently looking at, if any.
        // -->
        tagProcessor.registerTag(LocationTag.class, "cursor_on_precise", (attribute, object) -> {
            return Minecraft.getInstance().hitResult instanceof BlockHitResult blockHit ? new LocationTag(blockHit.getLocation()) : null;
        });


        // <--[tag]
        // @attribute <client.climbing_speed>
        // @returns ElementTag(Decimal)
        // @mechanism client.climbing_speed
        // @description
        // Returns the client's climbing speed.
        // -->
        tagProcessor.registerTag(ElementTag.class, "climbing_speed", (attribute, object) -> {
            return new ElementTag(climbingSpeed);
        });

        // <--[mechanism]
        // @object client
        // @name climbing_speed
        // @input ElementTag(Decimal)
        // @description
        // Sets the client's climbing speed.
        // @tags
        // <client.climbing_speed>
        // -->
        tagProcessor.registerMechanism("climbing_speed", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireDouble()) {
                climbingSpeed = input.asDouble();
            }
        });

        // TODO this is temporary and is meant for testing only, should be replaced by a proper modifyblock command
        tagProcessor.registerMechanism("modifyblock", false, MaterialTag.class, (object, mechanism, input) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.hitResult instanceof BlockHitResult blockHitResult) {
                client.level.setBlockAndUpdate(blockHitResult.getBlockPos(), input.state);
            }
        });
    }
}
