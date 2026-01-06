package com.denizenscript.clientizen.tags.modules;

import com.denizenscript.clientizen.objects.EntityTag;
import com.denizenscript.clientizen.objects.LocationTag;
import com.denizenscript.clientizen.tags.ClientTagBase;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientEntityTags {

    public static void register(ObjectTagProcessor<ClientTagBase> tagProcessor) {
      
        // <--[tag]
        // @attribute <client.loaded_entities[(<matcher>)]>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of all entities currently loaded by the client.
        // Optionally specify an EntityTag matcher to filter by.
        // -->
        tagProcessor.registerTag(ListTag.class, "loaded_entities", (attribute, object) -> {
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            ListTag entities = new ListTag();
            if (LocationTag.getWorld() != null) {
                for (Entity entity : LocationTag.getWorld().entitiesForRendering()) {
                    EntityTag entityTag = new EntityTag(entity);
                    if (matcher == null || entityTag.advancedMatches(matcher, attribute.context)) {
                        entities.addObject(entityTag);
                    }
                }
            }
            return entities;
        });

        // <--[tag]
        // @attribute <client.target>
        // @returns EntityTag
        // @description
        // Returns the entity the client is currently looking at, if any.
        // -->
        tagProcessor.registerTag(EntityTag.class, "target", (attribute, object) -> {
            Entity target = Minecraft.getInstance().crosshairPickEntity;
            return target != null ? new EntityTag(target) : null;
        });

        // <--[tag]
        // @attribute <client.self_entity>
        // @returns EntityTag
        // @description
        // Returns an EntityTag of the client's own player entity.
        // -->
        tagProcessor.registerTag(EntityTag.class, "self_entity", (attribute, object) -> {
            return new EntityTag(Minecraft.getInstance().player);
        });
    }
}
